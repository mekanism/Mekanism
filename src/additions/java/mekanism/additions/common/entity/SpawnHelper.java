package mekanism.additions.common.entity;

import java.util.List;
import java.util.stream.Stream;
import mekanism.additions.common.config.AdditionsCommonConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.StructureSpawnListGatherEvent;

public class SpawnHelper {

    private SpawnHelper() {
    }

    public static void setupEntities() {
        //Register spawn controls for the baby entities based on the vanilla spawn controls
        registerSpawnControls(AdditionsEntityTypes.BABY_CREEPER, AdditionsEntityTypes.BABY_ENDERMAN, AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_WITHER_SKELETON);
        //Slightly different restrictions for the baby stray, as strays have a slightly different spawn restriction
        SpawnPlacements.register(AdditionsEntityTypes.BABY_STRAY.get(), SpawnPlacements.Type.ON_GROUND,
              Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityBabyStray::spawnRestrictions);
        //Add parrot sound imitations for baby mobs
        //Note: There is no imitation sound for endermen
        Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_CREEPER.get(), SoundEvents.PARROT_IMITATE_CREEPER);
        Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_SKELETON.get(), SoundEvents.PARROT_IMITATE_SKELETON);
        Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_STRAY.get(), SoundEvents.PARROT_IMITATE_STRAY);
        Parrot.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_WITHER_SKELETON.get(), SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
    }

    @SafeVarargs
    private static void registerSpawnControls(EntityTypeRegistryObject<? extends Monster>... entityTypeROs) {
        for (EntityTypeRegistryObject<? extends Monster> entityTypeRO : entityTypeROs) {
            SpawnPlacements.register(entityTypeRO.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                  Monster::checkMonsterSpawnRules);
        }
    }

    private static MobSpawnSettings.SpawnerData getSpawner(AdditionsCommonConfig.SpawnConfig spawnConfig, MobSpawnSettings.SpawnerData parentEntry) {
        int weight = (int) Math.ceil(parentEntry.getWeight().asInt() * spawnConfig.weightPercentage.get());
        int minSize = (int) Math.ceil(parentEntry.minCount * spawnConfig.minSizePercentage.get());
        int maxSize = (int) Math.ceil(parentEntry.maxCount * spawnConfig.maxSizePercentage.get());
        return new MobSpawnSettings.SpawnerData(spawnConfig.entityTypeProvider.getEntityType(), weight, minSize, Math.max(minSize, maxSize));
    }

    private static Stream<AdditionsCommonConfig.SpawnConfig> getSpawnConfigs() {
        return Stream.of(MekanismAdditionsConfig.common.babyCreeper, MekanismAdditionsConfig.common.babyEnderman, MekanismAdditionsConfig.common.babySkeleton,
              MekanismAdditionsConfig.common.babyStray, MekanismAdditionsConfig.common.babyWitherSkeleton);
    }

    public static void onBiomeLoad(BiomeLoadingEvent event) {
        //Add spawns to any biomes that have mob spawns for the "parent" types of our mobs
        MobSpawnSettingsBuilder spawns = event.getSpawns();
        List<MobSpawnSettings.SpawnerData> monsterSpawns = spawns.getSpawner(MobCategory.MONSTER);
        if (!monsterSpawns.isEmpty()) {
            //Fail quick if no monsters can spawn in this biome anyway
            ResourceLocation biomeName = event.getName();
            getSpawnConfigs().filter(spawnConfig -> spawnConfig.shouldSpawn.get() && !spawnConfig.biomeBlackList.get().contains(biomeName)).forEach(spawnConfig -> {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                    //If the adult mob can spawn in this biome let the baby mob spawn in it
                    //Note: We adjust the mob's spawning based on the adult's spawn rates
                    MobSpawnSettings.SpawnerData spawner = getSpawner(spawnConfig, parentEntry);
                    spawns.addSpawn(MobCategory.MONSTER, spawner);
                    MobSpawnSettings.MobSpawnCost parentCost = spawns.getCost(parent);
                    if (parentCost == null) {
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}", spawner.type.getRegistryName(),
                              biomeName, spawner.getWeight(), spawner.minCount, spawner.maxCount);
                    } else {
                        double spawnCostPerEntity = parentCost.getCharge() * spawnConfig.spawnCostPerEntityPercentage.get();
                        double maxSpawnCost = parentCost.getEnergyBudget() * spawnConfig.maxSpawnCostPercentage.get();
                        spawns.addMobCharge(spawner.type, spawnCostPerEntity, maxSpawnCost);
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                              spawner.type.getRegistryName(), biomeName, spawner.getWeight(), spawner.minCount, spawner.maxCount, spawnCostPerEntity, maxSpawnCost);
                    }
                });
            });
        }
    }

    public static void onStructureSpawnListGather(StructureSpawnListGatherEvent event) {
        //Add special spawns to any structures that have mob spawns for the "parent" types of our mobs
        List<MobSpawnSettings.SpawnerData> monsterSpawns = event.getEntitySpawns(MobCategory.MONSTER);
        if (!monsterSpawns.isEmpty()) {
            //Fail quick if no monsters can spawn in this structure anyway
            ResourceLocation structureName = event.getStructure().getRegistryName();
            getSpawnConfigs().filter(spawnConfig -> spawnConfig.shouldSpawn.get() && !spawnConfig.structureBlackList.get().contains(structureName)).forEach(spawnConfig -> {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                    //If the adult mob can spawn in this structure let the baby mob spawn in it
                    //Note: We adjust the mob's spawning based on the adult's spawn rates
                    MobSpawnSettings.SpawnerData spawner = getSpawner(spawnConfig, parentEntry);
                    event.addEntitySpawn(MobCategory.MONSTER, spawner);
                    Mekanism.logger.debug("Adding spawn rate for '{}' in structure '{}', with weight: {}, minSize: {}, maxSize: {}", spawner.type.getRegistryName(),
                          structureName, spawner.getWeight(), spawner.minCount, spawner.maxCount);
                });
            });
        }
    }
}