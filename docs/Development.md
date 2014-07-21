# Development

Mekanism is developed for Minecraft 1.7.10 using Minecraft Forge.

## Prerequisites

* Gradle - Version 1.12 is known to work, your mileage with other versions may vary

## Directory Structure

* /src - Source code
    - /api/java/_other_mod_apis_
    - /main - our code
        - /java/mekanism - Our code
                - /{api,client,common} - Core Mekanism mod
                - /generators - Generators Mod
                - /tools - Tools Mod
        - /resources/assets - Our textures and other non-code assets
* /docs - Documentation
* /etc - logo and mcmod.info for each package

## Packaging structure

### Mekanism.jar

* /assets - From /src/main/resources/assets
* /mekanism/{api,client,common} - .class files from /src/main/java/mekanism/{api,client,common}
* logo.png - from /etc/core
* mcmod.info - from /etc/core

### MekanismGenerators.jar

* /mekanism/generators - .class files from /src/main/java/mekanism/generators
* logo.png - from /etc/generators
* mcmod.info - from /etc/generators

### MekanismTools.jar

* /mekanism/tools - .class files from /src/main/java/mekanism/tools
* logo.png - from /etc/tools
* mcmod.info - from /etc/tools
