import gg.essential.gradle.util.noServerRunConfigs

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
}

val modGroup: String by project
val modBaseName: String by project
group = modGroup
base.archivesName.set("$modBaseName-${platform.mcVersionStr}")

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

loom {
    noServerRunConfigs()
    runConfigs {
        getByName("client") {
            programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
}

val embed by configurations.creating
configurations.implementation.get().extendsFrom(embed)

dependencies {
    // Only Mixin itself: its transitive deps (launchwrapper, LWJGL, log4j 2.0-beta9, guava, gson, asm, ...)
    // ship with Minecraft 1.8.9 and must not be copied into the jar. 0.7.11 is the 1.8.9 standard; 0.7
    // becomes the game's Mixin when this jar loads first and breaks mods that need >= 0.7.11.
    embed("org.spongepowered:mixin:0.7.11-SNAPSHOT") { isTransitive = false }
    embed("gg.essential:vigilance:306")
    embed(modImplementation("gg.essential:universalcraft-1.8.9-forge:369")!!)
    modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
}

tasks.jar {
    from(embed.files.map { zipTree(it) }) {
        // Signature files of signed deps (Mixin's MUMFREY.SF/RSA) don't match the repacked jar: Forge
        // refuses to launch it ("Invalid signature file digest for Manifest main attributes").
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.EC")
        // Their manifests and loader metadata belong to them, not to this mod (duplicate fabric.mod.json
        // entries fail the build).
        exclude("META-INF/MANIFEST.MF", "fabric.mod.json", "mcmod.info", "module-info.class", "META-INF/versions/*/module-info.class")
    }

    manifest.attributes(
        mapOf(
            "ModSide" to "CLIENT",
            "FMLCorePluginContainsFMLMod" to "Yes, yes it does",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "TweakOrder" to "0",
            // A TweakClass jar without an FMLCorePlugin is skipped by FML's @Mod discovery; Mixin's tweaker
            // re-adds it to the mod candidates when this is set.
            "ForceLoadAsMod" to "true"
        )
    )
}