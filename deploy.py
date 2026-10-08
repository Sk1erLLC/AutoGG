#!/usr/bin/env python3
"""
Sk1er.Club Mod Deployment Script

Uploads mod builds as drafts to the Sk1er.Club API for admin approval.

Usage:
    ./deploy.py -version <ver> [-beta] [-platforms <pattern>]

Examples:
    ./deploy.py -version 2.3
    ./deploy.py -version 2.3-beta5 -beta
    ./deploy.py -version 2.3 -platforms "1.21.*"
    ./deploy.py -version 2.3 -platforms "*fabric"

deploy-config.json:
    mod_id           (required) mod slug on sk1er.club
    platform_suffix  appended to versions/<dir> names that lack it, e.g. "-fabric" turns a
                     Stonecutter "versions/26.3" into platform key "26.3-fabric"
    aliases          {"<platform>": ["<extra platform>", ...]} upload one JAR under more keys
    dependencies     dependency slugs attached to every build
"""

import argparse
import json
import os
import sys
import hashlib
import glob
import uuid
import fnmatch
from pathlib import Path
from typing import List, Dict, Optional, Tuple
import requests
from requests_toolbelt import MultipartEncoder

# Configuration
API_BASE_URL = os.environ.get("DEPLOY_API_URL", "https://sk1er.club")
DEFAULT_TOKEN_PATHS = [
    ".deploy-token",
    os.path.expanduser("~/.sk1er-deploy-token")
]

# HTTP client with custom User-Agent
http_client = requests.Session()
http_client.headers["User-Agent"] = "Sk1erDeployCLI"


class DeploymentError(Exception):
    """Base exception for deployment errors"""
    pass


class DeploymentSession:
    """Manages a deployment session with rollback capability"""

    def __init__(self, version: str, session_id: str, token: str):
        self.version = version
        self.session_id = session_id
        self.token = token
        self.uploaded_drafts = []
        self.changelog_id: Optional[int] = None

    def add_uploaded_draft(self, draft_id: str):
        """Track a successfully uploaded draft"""
        self.uploaded_drafts.append(draft_id)

    def rollback(self):
        """Delete all uploaded drafts and changelog for this session"""
        if not self.uploaded_drafts and not self.changelog_id:
            print("No drafts or changelog to rollback")
            return

        print(f"\n❌ Rolling back deployment session {self.session_id}")

        # Delete uploaded drafts
        if self.uploaded_drafts:
            print(f"   Deleting {len(self.uploaded_drafts)} uploaded draft(s)...")
            try:
                response = http_client.delete(
                    f"{API_BASE_URL}/api/v1/admin/drafts/rollback/{self.session_id}",
                    headers={"Authorization": f"Bearer {self.token}"}
                )

                if response.status_code == 200:
                    print("   ✓ Drafts deleted successfully")
                else:
                    print(f"   ⚠ Failed to delete drafts: {response.text}")
            except Exception as e:
                print(f"   ⚠ Error deleting drafts: {e}")

        # Delete uploaded changelog
        if self.changelog_id:
            print(f"   Deleting uploaded changelog (ID: {self.changelog_id})...")
            try:
                response = http_client.delete(
                    f"{API_BASE_URL}/api/v1/changelogs/{self.changelog_id}",
                    headers={"Authorization": f"Bearer {self.token}"}
                )

                if response.status_code == 200:
                    print("   ✓ Changelog deleted successfully")
                else:
                    print(f"   ⚠ Failed to delete changelog: {response.text}")
            except Exception as e:
                print(f"   ⚠ Error deleting changelog: {e}")

        print("✓ Rollback completed")


def parse_args():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(
        description="Deploy mod builds to Sk1er.Club",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__
    )

    parser.add_argument(
        "-version",
        required=True,
        help="Version to deploy (e.g., 2.3 or 2.3-beta5)"
    )

    parser.add_argument(
        "-beta",
        action="store_true",
        help="Automatically publish to beta channel (requires 'beta' in version)"
    )

    parser.add_argument(
        "-platforms",
        help="Platform filter with wildcards (e.g., '1.21.*' or '*fabric')"
    )

    parser.add_argument(
        "-api-url",
        default=API_BASE_URL,
        help=f"API base URL (default: {API_BASE_URL})"
    )

    return parser.parse_args()


def load_token() -> str:
    """Load deploy token from environment or file"""
    # Check environment variable
    token = os.environ.get("DEPLOY_TOKEN")
    if token:
        print("✓ Using token from $DEPLOY_TOKEN")
        return token

    # Check token files
    for token_path in DEFAULT_TOKEN_PATHS:
        if os.path.exists(token_path):
            with open(token_path, 'r') as f:
                token = f.read().strip()
                if token:
                    print(f"✓ Using token from {token_path}")
                    return token

    raise DeploymentError(
        "No deploy token found. Set $DEPLOY_TOKEN or create .deploy-token or ~/.sk1er-deploy-token"
    )


def load_deploy_config() -> Dict:
    """Load deploy-config.json"""
    config_path = "deploy-config.json"

    if not os.path.exists(config_path):
        raise DeploymentError(f"deploy-config.json not found in {os.getcwd()}")

    with open(config_path, 'r') as f:
        config = json.load(f)

    if "mod_id" not in config:
        raise DeploymentError("deploy-config.json missing required field 'mod_id'")

    print(f"✓ Loaded config for mod: {config['mod_id']}")
    return config


def load_changelog(version: str) -> str:
    """Load changelog from changelogs/<version>.txt"""
    changelog_path = f"changelogs/{version}.txt"

    if not os.path.exists(changelog_path):
        raise DeploymentError(
            f"Changelog not found: {changelog_path}\n"
            f"Create a changelog file before deploying."
        )

    with open(changelog_path, 'r', encoding='utf-8') as f:
        content = f.read().strip()

    if not content:
        raise DeploymentError(f"Changelog is empty: {changelog_path}")

    print(f"✓ Loaded changelog from {changelog_path} ({len(content)} chars)")
    return content


def discover_jars(version: str, platform_filter: Optional[str] = None, platform_suffix: str = "") -> List[Tuple[str, Path]]:
    """
    Discover JAR files in versions/*/build/libs/

    Returns: List of (platform_key, jar_path) tuples
    """
    jars = []
    versions_dir = Path("versions")

    if not versions_dir.exists():
        raise DeploymentError("versions/ directory not found")

    # Find all platform directories
    for platform_dir in versions_dir.iterdir():
        if not platform_dir.is_dir():
            continue

        platform_key = platform_dir.name
        if platform_suffix and not platform_key.endswith(platform_suffix):
            platform_key += platform_suffix

        # Apply platform filter if specified
        if platform_filter and not fnmatch.fnmatch(platform_key, platform_filter):
            continue

        # Look for JARs in build/libs/
        libs_dir = platform_dir / "build" / "libs"
        if not libs_dir.exists():
            print(f"⚠ No build/libs/ directory for {platform_key}, skipping")
            continue

        # Find JAR files (exclude -all, -sources and -dev jars)
        found_jar = False
        for jar_file in sorted(libs_dir.glob("*.jar")):
            if jar_file.name.endswith(("-all.jar", "-sources.jar", "-dev.jar")):
                continue

            jars.append((platform_key, jar_file))
            found_jar = True
            break  # Take first non-all JAR

        if not found_jar:
            print(f"⚠ No JAR found for {platform_key}")

    if not jars:
        raise DeploymentError("No JARs found. Did you build the mod first?")

    print(f"✓ Found {len(jars)} JAR(s)")
    for platform_key, jar_path in jars:
        print(f"  - {platform_key}: {jar_path.name}")

    return jars


def calculate_file_hash(file_path: Path) -> str:
    """Calculate SHA-256 hash of a file"""
    sha256 = hashlib.sha256()

    with open(file_path, 'rb') as f:
        while chunk := f.read(8192):
            sha256.update(chunk)

    return sha256.hexdigest()


def upload_changelog(changelog_content: str, version: str, token: str) -> int:
    """
    Upload changelog to API and return changelog ID

    Returns: Changelog ID
    """
    print(f"\n📝 Uploading changelog for version {version}...")

    payload = {
        "title": f"Version {version}",
        "content": changelog_content,
        "format": "markdown"
    }

    response = http_client.post(
        f"{API_BASE_URL}/api/v1/changelogs",
        json=payload,
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
    )

    if response.status_code == 409:
        # Changelog already exists, prompt to override
        print("⚠ A changelog for this version already exists")
        answer = input("Override existing changelog? [Y/n]: ").strip().lower()

        if answer in ('', 'y', 'yes'):
            # Get existing changelog ID and update it
            # For now, we'll just create a new one
            # TODO: Implement update logic if needed
            print("Creating new changelog entry...")
            response = http_client.post(
                f"{API_BASE_URL}/api/v1/changelogs",
                json=payload,
                headers={
                    "Authorization": f"Bearer {token}",
                    "Content-Type": "application/json"
                }
            )
        else:
            print("Using existing changelog")
            # TODO: Fetch existing changelog ID
            return 0  # Placeholder

    if response.status_code not in (200, 201):
        raise DeploymentError(f"Failed to upload changelog: {response.text}")

    data = response.json()
    changelog_id = data.get("data", {}).get("id")

    if not changelog_id:
        raise DeploymentError("Changelog upload succeeded but no ID returned")

    print(f"✓ Changelog uploaded (ID: {changelog_id})")
    return changelog_id


def upload_draft(
    jar_path: Path,
    platform_key: str,
    mod_id: str,
    version: str,
    session_id: str,
    token: str,
    changelog_id: int = None,
    dependency_slugs: List[str] = None
) -> str:
    """
    Upload a draft build

    Returns: Draft ID
    """
    # Calculate hash and size
    print(f"\n📦 Preparing {platform_key}...")
    print(f"   Calculating hash...")
    file_hash = calculate_file_hash(jar_path)
    file_size = jar_path.stat().st_size

    print(f"   Hash: {file_hash}")
    print(f"   Size: {file_size:,} bytes")

    # Upload
    print(f"   Uploading...")
    if changelog_id is not None:
        print(f"   Changelog ID: {changelog_id}")
    else:
        print(f"   ⚠ No changelog ID (will upload without changelog)")

    if dependency_slugs:
        print(f"   Dependencies: {', '.join(dependency_slugs)}")

    with open(jar_path, 'rb') as f:
        fields = {
            'file': (jar_path.name, f, 'application/java-archive'),
            'modId': mod_id,
            'platformKey': platform_key,
            'version': version,
            'fileHash': file_hash,
            'fileSizeBytes': str(file_size),
            'deploymentSessionId': session_id
        }

        # Add changelog ID if provided
        if changelog_id is not None:
            fields['changelogId'] = str(changelog_id)
            print(f"   Including changelogId={changelog_id} in upload")

        # Add dependencies if provided
        if dependency_slugs:
            fields['dependencySlugs'] = json.dumps(dependency_slugs)
            print(f"   Including {len(dependency_slugs)} dependencies in upload")

        multipart = MultipartEncoder(fields=fields)

        response = http_client.post(
            f"{API_BASE_URL}/api/v1/admin/drafts/upload",
            data=multipart,
            headers={
                'Authorization': f'Bearer {token}',
                'Content-Type': multipart.content_type
            }
        )

    if response.status_code not in (200, 201):
        raise DeploymentError(f"Upload failed for {platform_key}: {response.text}")

    data = response.json()
    draft_id = data.get("data", {}).get("draftId")

    if not draft_id:
        raise DeploymentError(f"Upload succeeded but no draft ID returned for {platform_key}")

    print(f"✓ Uploaded (Draft ID: {draft_id})")
    return draft_id


def apply_platform_aliases(
    jars: List[Tuple[str, Path]],
    config: Dict
) -> List[Tuple[str, Path]]:
    """
    Apply platform aliases from config

    Aliases map source platform to target platform(s)
    Example: {"1.21.3_fabric": ["1.21.4_fabric", "1.21.5_fabric"]}
    """
    aliases = config.get("aliases", {})
    if not aliases:
        return jars

    expanded_jars = []

    for platform_key, jar_path in jars:
        # Add original
        expanded_jars.append((platform_key, jar_path))

        # Add aliases
        if platform_key in aliases:
            targets = aliases[platform_key]
            for target in targets:
                print(f"  📋 Aliasing {platform_key} -> {target}")
                expanded_jars.append((target, jar_path))

    return expanded_jars


def main():
    """Main deployment flow"""
    args = parse_args()

    print("=" * 60)
    print("Sk1er.Club Mod Deployment")
    print("=" * 60)
    print(f"Version: {args.version}")
    print(f"API: {args.api_url}")

    # Validate beta flag
    if args.beta and "beta" not in args.version.lower():
        print("\n❌ Error: -beta flag requires 'beta' in version string")
        sys.exit(1)

    try:
        # Load configuration
        token = load_token()
        config = load_deploy_config()
        mod_id = config["mod_id"]

        # Load changelog
        changelog_content = load_changelog(args.version)

        # Discover JARs
        jars = discover_jars(args.version, args.platforms, config.get("platform_suffix", ""))

        # Apply platform aliases
        jars = apply_platform_aliases(jars, config)

        # Create deployment session
        session_id = str(uuid.uuid4())
        session = DeploymentSession(args.version, session_id, token)

        print(f"\n🚀 Starting deployment session: {session_id}")

        # Upload changelog
        changelog_id = upload_changelog(changelog_content, args.version, token)
        session.changelog_id = changelog_id

        # Get dependencies from config
        dependency_slugs = config.get("dependencies", [])
        if dependency_slugs:
            print(f"\n📦 Build dependencies: {', '.join(dependency_slugs)}")

        # Upload all JARs
        print(f"\n📤 Uploading {len(jars)} build(s)...")

        for platform_key, jar_path in jars:
            try:
                draft_id = upload_draft(
                    jar_path=jar_path,
                    platform_key=platform_key,
                    mod_id=mod_id,
                    version=args.version,
                    session_id=session_id,
                    token=token,
                    changelog_id=changelog_id,
                    dependency_slugs=dependency_slugs
                )
                session.add_uploaded_draft(draft_id)
            except Exception as e:
                print(f"\n❌ Error uploading {platform_key}: {e}")
                session.rollback()
                sys.exit(1)

        # Success!
        print("\n" + "=" * 60)
        print("✅ Deployment Successful!")
        print("=" * 60)
        print(f"Session ID: {session_id}")
        print(f"Mod: {mod_id}")
        print(f"Version: {args.version}")
        print(f"Changelog ID: {changelog_id}")
        print(f"Drafts uploaded: {len(session.uploaded_drafts)}")
        print("\nNext steps:")
        print("1. Go to the admin UI at https://sk1er.club/modadmin/drafts")
        print("2. Review the draft builds")
        print("3. Publish to a channel (stable, beta, alpha)")

        if args.beta:
            print("\n💡 Tip: You specified -beta flag. Don't forget to publish to the 'beta' channel!")

    except DeploymentError as e:
        print(f"\n❌ Deployment failed: {e}")
        if 'session' in locals():
            session.rollback()
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n\n⚠ Deployment interrupted by user")
        if 'session' in locals():
            session.rollback()
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        if 'session' in locals():
            session.rollback()
        sys.exit(1)


if __name__ == "__main__":
    main()
