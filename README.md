# Mekanism 1.7.10 Community Edition
Originally made for Terralization Modpack with fixes from several different mods. Now with new features, bugfixes and performance enhancement.

#### Download at [curseforge](https://www.curseforge.com/minecraft/mc-mods/mekanism-ce)

#### Builds at our [maven](https://maven.thorfusion.com/ui/repos/tree/General/thorfusion/mekanism/Mekanism-1.7.10-Community-Edition)

### Building Mekanism 1.7.10 Community Edition

```bash
./gradlew fullBuild
```
You will find the files inside ./output/

## For modpacks
You need to include the [LICENSE](https://raw.githubusercontent.com/Thorfusion/Mekanism-1.7.10-Community-Edition/1.7.10/LICENSE.md) for Mekanism 1.7.10 Community Edition and Aidanbrady as author, if your system supports it indicate that this is an custom version and give appreciable credits

Mekanism CE has continued the use of the update notifier but changed the config name to v2. This is to notify people making the switch to CE of this feature. It is recommended for modpacks to disable this.

## Required Dependencies
Mekanism CE also has two dependencies that it requires, as a modpack creator DO NOT USE the automatic downloader to get those file. It puts alot of strain to my server network.

### Dependency list
+ [ForgeMultipart](https://files.thorfusion.com/mekanism/ForgeMultipart-1.7.10-1.2.0.347-universal.jar)
+ [CodeChickenLib](https://files.thorfusion.com/mekanism/CodeChickenLib-1.7.10-1.1.3.141-universal.jar)

## Modifications by

#### Clienthax : 
+ dupe bug with chemical washer and fluidtank, fixes issue with railcraft

#### draksterau : 
+ personal chest bug and a server crash bug

#### iKEVAREZ : 
+ and the turbine bug involving each fluid being sucked in.

#### kmecpp : 
+ dupe bug with factories

#### awesomely2002 : 
+ improvment for Entangloporters need to transport more

#### q1051278389 : 
+ Turbine and matrix bug
+ fix industrialTrubine can input Water or some liquid with another mod's pipe(now only accept water)
+ fix EnergyMatrix use IC2 cable(EnergyNetBug) connecting valve as a loop to fully charge the Matrix(now IC2Cable can't connect the MatrixValve which in "Input Mode")

#### [maggi373](https://github.com/maggi373) - Mekanism CE Team
+ fix for bin dupe bug, removed the ability to stack bins, quick solution but it works
+ added api.jar, mdk is now depricated
+ oredict switcher for osmium: you can chose either or both of osmium/platinum
+ fixed minetweaker not working problerly with mekanism
+ fixed osmium compressor taking incorrect amount of osmium Thorfusion#32
+ making enriched alloy now uses steel ingots instead of iron ingots
+ updated libraries and removed unused metallurgy compat
+ voiceserver is now disabled by default
+ oredict recipes for poor ores from railcraft (1x ingot from using purification chamber) Thorfusion#30
+ oredict recipe for dustQuartz to be enriched(mekanism) to quartz Thorfusion#30
+ oredict recipe for dustQuartz to be enriched(mekanism) from quartz ore Thorfusion#30
+ oredict recipe for gemDiamond to be enriched(mekanism) to compresseddiamonds Thorfusion#30
+ cleaned and upgraded gradle building
+ added autodownload for dependencies and missing dependency warning

#### [Pokemonplatin](https://github.com/Pokemonplatin) - Mekanism CE Team
+ Teleporter and Quantum Entangloporter now have a trusted channel for smp Thorfusion#22

#### [DrParadox7](https://github.com/DrParadox7) - Mekanism CE Team
+ fixed missing lang for teleporter Thorfusion#36
+ cardboxes are now single use Thorfusion#36
+ added methane gas Thorfusion#36
+ nerfed fusion reactor Thorfusion#36
+ added config for reducing particles for completed multiblocks Thorfusion#36
+ sawmill now outputs raw rubber instead of rubber Thorfusion#36
+ added methane to gas burner fuel list Thorfusion#36

## All contributors get capes

# License

[LICENSE](https://raw.githubusercontent.com/Thorfusion/Mekanism-1.7.10-Community-Edition/1.7.10/LICENSE.md)

[ORIGINAL MOD](https://github.com/mekanism/Mekanism)