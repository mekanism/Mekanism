# Development

Mekanism is developed for Minecraft 1.21 using NeoForge.

## Prerequisites

* Gradle - Version 9.0 is known to work, your mileage with other versions may vary
* JDK - Version 21 is the target as that is what Mojang ships, but to run gradle you must use a minimum of JDK 17.

## Directory Structure

* /src - Source code
    - /additions
        - /java/mekanism/additions - Code for Mekanism: Additions
        - /resources - Resources for Mekanism: Additions
    - /api/java/mekanism/api - Our API
    - /datagen
        - /additions/java/mekanism/additions - Data generators for Mekanism: Additions
        - /defense/java/mekanism/defense - Data generators for Mekanism: Defense (Planned for V11, currently placeholder)
        - /generated
            - /mekanism - Generated resources for Mekanism
            - /mekanismadditions - Generated resources for Mekanism: Additions
            - /mekanismgenerators - Generated resources for Mekanism: Generators
            - /mekanismtools - Generated resources for Mekanism: Tools
        - /generators/java/mekanism/generators - Data generators for Mekanism: Generators
        - /main/java/mekanism - Data generators for Mekanism
        - /tools/java/mekanism/tools - Data generators for Mekanism: Tools
    - /defense (Planned for V11, currently placeholder)
        - /java/mekanism/defense - Code for Mekanism: Defense
        - /resources - Resources for Mekanism: Defense
    - /gameTest
      - /main/java/mekanism - Game Tests for Mekanism
    - /generators
        - /java/mekanism/generators - Code for Mekanism: Generators
        - /resources - Resources for Mekanism: Generators
    - /main
        - /java/mekanism - Code for Mekanism
        - /resources - Resources for Mekanism
    - /tools
        - /java/mekanism/tools - Code for Mekanism: Tools
        - /resources - Resources for Mekanism: Tools
    - /test/java/mekanism - Unit Tests
* /docs - Documentation / style information

## Packaging structure

### Mekanism.jar

* /assets - From /src/main/resources/assets and /src/datagen/generated/mekanism/assets
* /data - From /src/main/resources/data and /src/datagen/generated/mekanism/data
* /mekanism - .class files from /src/main/java and src/api/java
* /META-INF - From /src/main/resources/META-INF
* logo.png

### Mekanism-api.jar

This jar is for development purposes

* /mekanism - .class files from src/api/java

### MekanismAdditions.jar

* /assets - From /src/additions/resources/assets and /src/datagen/generated/mekanismadditions/assets
* /data - From /src/additions/resources/data and /src/datagen/generated/mekanismadditions/data
* /mekanism - .class files from /src/additions/java
* /META-INF - From /src/additions/resources/META-INF
* logo.png

### MekanismDefense.jar (Planned for V11, currently placeholder)

* /assets - From /src/defense/resources/assets and /src/datagen/generated/mekanismdefense/assets
* /data - From /src/defense/resources/data and /src/datagen/generated/mekanismdefense/data
* /mekanism - .class files from /src/defense/java
* /META-INF - From /src/defense/resources/META-INF
* logo.png

### MekanismGenerators.jar

* /assets - From /src/generators/resources/assets and /src/datagen/generated/mekanismgenerators/assets
* /data - From /src/generators/resources/data and /src/datagen/generated/mekanismgenerators/data
* /mekanism - .class files from /src/generators/java
* /META-INF - From /src/generators/resources/META-INF
* logo.png

### MekanismTools.jar

* /assets - From /src/tools/resources/assets and /src/datagen/generated/mekanismtools/assets
* /data - From /src/tools/resources/data and /src/datagen/generated/mekanismtools/data
* /mekanism - .class files from /src/tools/java
* /META-INF - From /src/tools/resources/META-INF
* logo.png

### Mekanism-all.jar

All in one jar.

* /assets - Assets of all above jars
* /data - Data of all above jars (duplicate tags are last tag wins)
* /mekanism - .class files from all above jars
* /META-INF - META-INF of all above jars
* logo.png

## Building

Mekanism is built in the same way that the vast majority of other mods are. If you are using an IDE such as Eclipse or IntelliJ IDEA, the normal ways of building/launching it work. If you are just trying to build the jars yourself without an IDE and are new to building mods, this can be done by downloading the sources from GitHub and in the root directory running `gradlew build` or `gradlew.bat build`. The built jars will be output into the `build/libs/` folder.
