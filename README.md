# Blabber

A library adding dialogues to Minecraft. Can also be used as a standalone mod, through the `/blabber` command.

This mod must be installed on both server and client to work properly.

## Adding Blabber to your project

You can add the library by inserting the following in your `build.gradle` :

```gradle
repositories {
	maven { 
        name = "Ladysnake Mods"
        url = "https://ladysnake.jfrog.io/artifactory/mods"
        content {
            includeGroup 'io.github.ladysnake'
            includeGroupByRegex 'io\\.github\\.onyxstudios.*'
        }
    }
    maven {
        name = "Nexus Repository Manager"
        url = 'https://oss.sonatype.org/content/repositories/snapshots'
    }
}

dependencies {
    modImplementation "io.github.ladysnake:blabber:${blabber_version}"
    include "io.github.ladysnake:blabber:${blabber_version}"
    // Blabber dependencies
    include "me.lucko:fabric-permissions-api:${fpa_version}"
    include "com.github.onyxstudios.Cardinal-Components-API:cardinal-components-base:${cca_version}"
    include "com.github.onyxstudios.Cardinal-Components-API:cardinal-components-entity:${cca_version}"
}
```

You can then add the library version to your `gradle.properties`file:

```properties
# Blabber
blabber_version = 0.x.y
# Fabric Permissions API
fpa_version = 0.1-SNAPSHOT
# Cardinal Components
cca_version = 2.x.y
```

You can find the current version of Blabber in the [releases](https://github.com/Ladysnake/Blabber/releases) tab of the repository on Github,
and the latest CCA version in the [appropriate repository](https://github.com/OnyxStudios/Cardinal-Components-API/releases).
