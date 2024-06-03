# Blabber

A library adding dialogues to Minecraft. Can also be used as a standalone mod, through the `/blabber` command.

This mod must be installed on both server and client to work properly.

For more information, check out [the full description on the Ladysnake website](https://ladysnake.org/wiki/blabber).

## Adding Blabber to your project

You can add the library by inserting the following in your `build.gradle` :

```gradle
repositories {
	maven { 
        name = "Ladysnake Mods"
        url = "https://maven.ladysnake.org/releases"
        content {
            includeGroupByRegex '(org|io\\.github).ladysnake.*'
            includeGroupByRegex '(dev|io\\.github)\\.onyxstudios.*'
        }
    }
    maven {
        name = "Nexus Repository Manager"
        url = 'https://oss.sonatype.org/content/repositories/snapshots'
    }
}

dependencies {
    modImplementation include("org.ladysnake:blabber:${blabber_version}")
    // Blabber dependencies
    include "me.lucko:fabric-permissions-api:${fpa_version}"
    include "org.ladysnake.cardinal-components-api:cardinal-components-base:${cca_version}"
    include "org.ladysnake.cardinal-components-api:cardinal-components-entity:${cca_version}"
}
```

You can then add the library version to your `gradle.properties`file:

```properties
# Blabber
blabber_version = 1.x.y
# Fabric Permissions API
fpa_version = 0.1-SNAPSHOT
# Cardinal Components
cca_version = 4.x.y
```

You can find the current version of Blabber in the [releases](https://github.com/Ladysnake/Blabber/releases) tab of the repository on Github,
and the latest CCA version in the [appropriate repository](https://github.com/Ladysnake/Cardinal-Components-API/releases).
