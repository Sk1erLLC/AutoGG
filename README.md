# AutoGG
AutoGG is a Minecraft Fabric mod developed by [Sk1er LLC](https://github.com/Sk1erLLC) that allows you to automatically say a selected phrase after a game has ended on supported servers.

Supported versions: Minecraft 26.1 (26.1-26.1.2), 26.2 and 26.3 on Fabric. Run `/autogg` in game to open its settings.

## Support
Join [our support Discord](https://discord.gg/d4KFR9H) for support.

## Developing
**Requirements:**
- JDK (Java Development Kit) 25
    * [Eclipse Temurin](https://adoptium.net/)
    * [Other OpenJDK distributions](https://en.wikipedia.org/wiki/OpenJDK#OpenJDK_builds)

- A Java IDE, we recommend using [IntelliJ IDEA](https://jetbrains.com/idea/)
 
## Building
**Unix-based systems (GNU/Linux, OSX, etc):**
```bash
$ ./gradlew build          # every version
$ ./gradlew :26.3:build    # one version
```

**Microsoft Windows:**
```batch
> gradlew.bat build
```

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) for multiple Minecraft versions. Jars are placed under `versions/<minecraft version>/build/libs`.

## License
AutoGG is licensed under **GNU GPLv3**, see: [LICENSE](LICENSE).
