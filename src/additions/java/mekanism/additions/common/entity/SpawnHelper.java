package mekanism.additions.common.entity;

import java.util.List;
import mekanism.additions.common.config.AdditionsCommonConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.api.providers.IEntityTypeProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnHelper {

    public static void addSpawns() {
        for (Biome biome : ForgeRegistries.BIOMES) {
            List<SpawnListEntry> monsterSpawns = biome.getSpawns(EntityClassification.MONSTER);
            //Fail quick if no monsters can spawn in this biome anyways
            if (!monsterSpawns.isEmpty()) {
                ResourceLocation biomeName = biome.getRegistryName();
                addSpawn(AdditionsEntityTypes.BABY_CREEPER, EntityType.CREEPER, MekanismAdditionsConfig.common.babyCreeper, monsterSpawns, biomeName);
                addSpawn(AdditionsEntityTypes.BABY_ENDERMAN, EntityType.ENDERMAN, MekanismAdditionsConfig.common.babyEnderman, monsterSpawns, biomeName);
                addSpawn(AdditionsEntityTypes.BABY_SKELETON, EntityType.SKELETON, MekanismAdditionsConfig.common.babySkeleton, monsterSpawns, biomeName);
                addSpawn(AdditionsEntityTypes.BABY_STRAY, EntityType.STRAY, MekanismAdditionsConfig.common.babyStray, monsterSpawns, biomeName);
                addSpawn(AdditionsEntityTypes.BABY_WITHER_SKELETON, EntityType.WITHER_SKELETON, MekanismAdditionsConfig.common.babyWitherSkeleton, monsterSpawns, biomeName);
            }
        }
        addSpawn(AdditionsEntityTypes.BABY_WITHER_SKELETON, EntityType.WITHER_SKELETON, MekanismAdditionsConfig.common.babyWitherSkeleton, Feature.NETHER_BRIDGE.getSpawnList());

        //Register spawn controls for the baby entities based on the vanilla spawn controls
        registerSpawnControls(AdditionsEntityTypes.BABY_CREEPER, AdditionsEntityTypes.BABY_ENDERMAN, AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_WITHER_SKELETON);
        //Slightly different restrictions for the baby stray, as strays have a slightly different spawn restriction
        EntitySpawnPlacementRegistry.register(AdditionsEntityTypes.BABY_STRAY.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
              Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityBabyStray::spawnRestrictions);
    }

    private static void addSpawn(IEntityTypeProvider entityTypeProvider, EntityType<?> parent, AdditionsCommonConfig.SpawnConfig spawnConfig, List<SpawnListEntry> monsterSpawns) {
        if (spawnConfig.shouldSpawn.get()) {
            monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.entityType == parent).findFirst()
                  .ifPresent(parentEntry -> {
                      //If the adult mob can spawn in this biome let the baby mob spawn in it
                      //Note: We adjust the mob's spawning based on the adult's spawn rates
                      EntityType<?> entityType = entityTypeProvider.getEntityType();
                      int weight = (int) Math.ceil(parentEntry.itemWeight * spawnConfig.weightPercentage.get());
                      int minSize = (int) Math.ceil(parentEntry.minGroupCount * spawnConfig.minSizePercentage.get());
                      int maxSize = (int) Math.ceil(parentEntry.maxGroupCount * spawnConfig.maxSizePercentage.get());
                      monsterSpawns.add(new SpawnListEntry(entityType, weight, minSize, Math.max(minSize, maxSize)));
                      Mekanism.logger.debug("Adding spawn rate for {} to nether bridges, with weight: {}, minSize: {}, maxSize: {}", entityType.getRegistryName(), weight, minSize, maxSize);
                  });
        }
    }

    private static void addSpawn(IEntityTypeProvider entityTypeProvider, EntityType<?> parent, AdditionsCommonConfig.SpawnConfig spawnConfig, List<SpawnListEntry> monsterSpawns, ResourceLocation biomeName) {
        if (spawnConfig.shouldSpawn.get() && !spawnConfig.biomeBlackList.get().contains(biomeName)) {
            monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.entityType == parent).findFirst()
                  .ifPresent(parentEntry -> {
                      //If the adult mob can spawn in this biome let the baby mob spawn in it
                      //Note: We adjust the mob's spawning based on the adult's spawn rates
                      EntityType<?> entityType = entityTypeProvider.getEntityType();
                      int weight = (int) Math.ceil(parentEntry.itemWeight * spawnConfig.weightPercentage.get());
                      int minSize = (int) Math.ceil(parentEntry.minGroupCount * spawnConfig.minSizePercentage.get());
                      int maxSize = (int) Math.ceil(parentEntry.maxGroupCount * spawnConfig.maxSizePercentage.get());
                      monsterSpawns.add(new SpawnListEntry(entityType, weight, minSize, Math.max(minSize, maxSize)));
                      Mekanism.logger.debug("Adding spawn rate for {} in biome {}, with weight: {}, minSize: {}, maxSize: {}", entityType.getRegistryName(),
                            biomeName, weight, minSize, maxSize);
                  });
        }
    }

    @SafeVarargs
    private static void registerSpawnControls(EntityTypeRegistryObject<? extends MonsterEntity>... entityTypeROs) {
        for (EntityTypeRegistryObject<? extends MonsterEntity> entityTypeRO : entityTypeROs) {
            EntitySpawnPlacementRegistry.register(entityTypeRO.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                  MonsterEntity::canMonsterSpawnInLight);
        }
    }
}