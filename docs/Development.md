# Development

Mekanism is developed for Minecraft 1.6.2 using Minecraft Forge.

## Prerequisites

* Minecraft Forge for MC 1.6.2 <http://files.minecraftforge.net/>
* Dev build of CodeChickenCore for MC 1.6.2 <http://www.chickenbones.craftsaddle.org/Files/New_Versions/links.php>
* Dev build of NEI for MC 1.6.2 <http://www.chickenbones.craftsaddle.org/Files/New_Versions/links.php>

## Directory Structure

* /common - Source code
    - / _other_ _mod_ _apis_
    - /mekanism
        - /{api,client,common} - Core Mekanism mod
        - /generators - Generators Mod
        - /tools - Tools Mod
* /docs - Documentation
* /etc - logo and mcmod.info for each package
* /resources - Non-code assets for the core mod

## Packaging structure

### Mekanism.jar

* /assets - From /resources/assets
* / _other_ _mod_ _apis_ - .class files from /common/ _other_ _mod_ _apis_
* /mekanism/{api,client,common} - .class files from /common/mekanism/{api,client,common}
* logo.png - from /etc/core
* mcmod.info - from /etc/core

### MekanismGenerators.jar

* /mekanism/generators - .class files from /common/mekanism/generators
* logo.png - from /etc/generators
* mcmod.info - from /etc/generators

### MekanismTools.jar

* /mekanism/tools - .class files from /common/mekanism/tools
* logo.png - from /etc/tools
* mcmod.info - from /etc/tools
