package mekanism.additions.common.entity;

import java.util.List;
import java.util.stream.Stream;
import mekanism.additions.common.config.AdditionsCommonConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
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
        EntitySpawnPlacementRegistry.register(AdditionsEntityTypes.BABY_STRAY.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
              Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityBabyStray::spawnRestrictions);
        //Add parrot sound imitations for baby mobs
        //Note: There is no imitation sound for endermen
        ParrotEntity.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_CREEPER.get(), SoundEvents.PARROT_IMITATE_CREEPER);
        ParrotEntity.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_SKELETON.get(), SoundEvents.PARROT_IMITATE_SKELETON);
        ParrotEntity.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_STRAY.get(), SoundEvents.PARROT_IMITATE_STRAY);
        ParrotEntity.MOB_SOUND_MAP.put(AdditionsEntityTypes.BABY_WITHER_SKELETON.get(), SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
    }

    @SafeVarargs
    private static void registerSpawnControls(EntityTypeRegistryObject<? extends MonsterEntity>... entityTypeROs) {
        for (EntityTypeRegistryObject<? extends MonsterEntity> entityTypeRO : entityTypeROs) {
            EntitySpawnPlacementRegistry.register(entityTypeRO.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                  MonsterEntity::checkMonsterSpawnRules);
        }
    }

    private static MobSpawnInfo.Spawners getSpawner(AdditionsCommonConfig.SpawnConfig spawnConfig, MobSpawnInfo.Spawners parentEntry) {
        int weight = (int) Math.ceil(parentEntry.weight * spawnConfig.weightPercentage.get());
        int minSize = (int) Math.ceil(parentEntry.minCount * spawnConfig.minSizePercentage.get());
        int maxSize = (int) Math.ceil(parentEntry.maxCount * spawnConfig.maxSizePercentage.get());
        return new MobSpawnInfo.Spawners(spawnConfig.entityTypeProvider.getEntityType(), weight, minSize, Math.max(minSize, maxSize));
    }

    private static Stream<AdditionsCommonConfig.SpawnConfig> getSpawnConfigs() {
        return Stream.of(MekanismAdditionsConfig.common.babyCreeper, MekanismAdditionsConfig.common.babyEnderman, MekanismAdditionsConfig.common.babySkeleton,
              MekanismAdditionsConfig.common.babyStray, MekanismAdditionsConfig.common.babyWitherSkeleton);
    }

    public static void onBiomeLoad(BiomeLoadingEvent event) {
        //Add spawns to any biomes that have mob spawns for the "parent" types of our mobs
        MobSpawnInfoBuilder spawns = event.getSpawns();
        List<MobSpawnInfo.Spawners> monsterSpawns = spawns.getSpawner(EntityClassification.MONSTER);
        if (!monsterSpawns.isEmpty()) {
            //Fail quick if no monsters can spawn in this biome anyway
            ResourceLocation biomeName = event.getName();
            getSpawnConfigs().filter(spawnConfig -> spawnConfig.shouldSpawn.get() && !spawnConfig.biomeBlackList.get().contains(biomeName)).forEach(spawnConfig -> {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                    //If the adult mob can spawn in this biome let the baby mob spawn in it
                    //Note: We adjust the mob's spawning based on the adult's spawn rates
                    MobSpawnInfo.Spawners spawner = getSpawner(spawnConfig, parentEntry);
                    spawns.addSpawn(EntityClassification.MONSTER, spawner);
                    MobSpawnInfo.SpawnCosts parentCost = spawns.getCost(parent);
                    if (parentCost == null) {
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}", spawner.type.getRegistryName(),
                              biomeName, spawner.weight, spawner.minCount, spawner.maxCount);
                    } else {
                        double spawnCostPerEntity = parentCost.getCharge() * spawnConfig.spawnCostPerEntityPercentage.get();
                        double maxSpawnCost = parentCost.getEnergyBudget() * spawnConfig.maxSpawnCostPercentage.get();
                        spawns.addMobCharge(spawner.type, spawnCostPerEntity, maxSpawnCost);
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                              spawner.type.getRegistryName(), biomeName, spawner.weight, spawner.minCount, spawner.maxCount, spawnCostPerEntity, maxSpawnCost);
                    }
                });
            });
        }
    }

    public static void onStructureSpawnListGather(StructureSpawnListGatherEvent event) {
        //Add special spawns to any structures that have mob spawns for the "parent" types of our mobs
        List<MobSpawnInfo.Spawners> monsterSpawns = event.getEntitySpawns(EntityClassification.MONSTER);
        if (!monsterSpawns.isEmpty()) {
            //Fail quick if no monsters can spawn in this structure anyway
            ResourceLocation structureName = event.getStructure().getRegistryName();
            getSpawnConfigs().filter(spawnConfig -> spawnConfig.shouldSpawn.get() && !spawnConfig.structureBlackList.get().contains(structureName)).forEach(spawnConfig -> {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                    //If the adult mob can spawn in this structure let the baby mob spawn in it
                    //Note: We adjust the mob's spawning based on the adult's spawn rates
                    MobSpawnInfo.Spawners spawner = getSpawner(spawnConfig, parentEntry);
                    event.addEntitySpawn(EntityClassification.MONSTER, spawner);
                    Mekanism.logger.debug("Adding spawn rate for '{}' in structure '{}', with weight: {}, minSize: {}, maxSize: {}", spawner.type.getRegistryName(),
                          structureName, spawner.weight, spawner.minCount, spawner.maxCount);
                });
            });
        }
    }
}