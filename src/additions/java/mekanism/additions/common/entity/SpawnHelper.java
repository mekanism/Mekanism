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
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MobSpawnInfo.SpawnCosts;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class SpawnHelper {

    public static void addSpawns() {
        //Add special spawns to the fortress for baby wither skeletons and skeletons
        List<MobSpawnInfo.Spawners> fortressSpawns = Structure.field_236378_n_.getSpawnList();
        addSpawn(AdditionsEntityTypes.BABY_WITHER_SKELETON, EntityType.WITHER_SKELETON, MekanismAdditionsConfig.common.babyWitherSkeleton, fortressSpawns);
        addSpawn(AdditionsEntityTypes.BABY_SKELETON, EntityType.SKELETON, MekanismAdditionsConfig.common.babySkeleton, fortressSpawns);

        //Register spawn controls for the baby entities based on the vanilla spawn controls
        registerSpawnControls(AdditionsEntityTypes.BABY_CREEPER, AdditionsEntityTypes.BABY_ENDERMAN, AdditionsEntityTypes.BABY_SKELETON,
              AdditionsEntityTypes.BABY_WITHER_SKELETON);
        //Slightly different restrictions for the baby stray, as strays have a slightly different spawn restriction
        EntitySpawnPlacementRegistry.register(AdditionsEntityTypes.BABY_STRAY.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
              Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityBabyStray::spawnRestrictions);
        //Add the global entity type attributes for the entities
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_CREEPER.getEntityType(), CreeperEntity.registerAttributes().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_ENDERMAN.getEntityType(), EndermanEntity.func_234287_m_().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_SKELETON.getEntityType(), AbstractSkeletonEntity.registerAttributes().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_STRAY.getEntityType(), AbstractSkeletonEntity.registerAttributes().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_WITHER_SKELETON.getEntityType(), AbstractSkeletonEntity.registerAttributes().create());
    }

    public static void onBiomeLoad(BiomeLoadingEvent event) {
        MobSpawnInfoBuilder spawns = event.getSpawns();
        List<MobSpawnInfo.Spawners> monsterSpawns = spawns.getSpawner(EntityClassification.MONSTER);
        //Fail quick if no monsters can spawn in this biome anyways
        if (!monsterSpawns.isEmpty()) {
            ResourceLocation biomeName = event.getName();
            addSpawn(spawns, AdditionsEntityTypes.BABY_CREEPER, EntityType.CREEPER, MekanismAdditionsConfig.common.babyCreeper, monsterSpawns, biomeName);
            addSpawn(spawns, AdditionsEntityTypes.BABY_ENDERMAN, EntityType.ENDERMAN, MekanismAdditionsConfig.common.babyEnderman, monsterSpawns, biomeName);
            addSpawn(spawns, AdditionsEntityTypes.BABY_SKELETON, EntityType.SKELETON, MekanismAdditionsConfig.common.babySkeleton, monsterSpawns, biomeName);
            addSpawn(spawns, AdditionsEntityTypes.BABY_STRAY, EntityType.STRAY, MekanismAdditionsConfig.common.babyStray, monsterSpawns, biomeName);
            addSpawn(spawns, AdditionsEntityTypes.BABY_WITHER_SKELETON, EntityType.WITHER_SKELETON, MekanismAdditionsConfig.common.babyWitherSkeleton, monsterSpawns, biomeName);
        }
    }

    private static void addSpawn(IEntityTypeProvider entityTypeProvider, EntityType<?> parent, AdditionsCommonConfig.SpawnConfig spawnConfig,
          List<MobSpawnInfo.Spawners> monsterSpawns) {
        if (spawnConfig.shouldSpawn.get()) {
            monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                //If the adult mob can spawn in this biome let the baby mob spawn in it
                //Note: We adjust the mob's spawning based on the adult's spawn rates
                EntityType<?> entityType = entityTypeProvider.getEntityType();
                int weight = (int) Math.ceil(parentEntry.itemWeight * spawnConfig.weightPercentage.get());
                int minSize = (int) Math.ceil(parentEntry.minCount * spawnConfig.minSizePercentage.get());
                int maxSize = (int) Math.ceil(parentEntry.maxCount * spawnConfig.maxSizePercentage.get());
                //TODO - 1.16.2: FIXME - monsterSpawns is immutable now so we are not actually able to add the spawns to the fortress
                //monsterSpawns.add(new MobSpawnInfo.Spawners(entityType, weight, minSize, Math.max(minSize, maxSize)));
                Mekanism.logger.debug("Adding spawn rate for {} to nether fortresses, with weight: {}, minSize: {}, maxSize: {}", entityType.getRegistryName(),
                      weight, minSize, maxSize);
            });
        }
    }

    private static void addSpawn(MobSpawnInfoBuilder spawns, IEntityTypeProvider entityTypeProvider, EntityType<?> parent, AdditionsCommonConfig.SpawnConfig spawnConfig,
          List<MobSpawnInfo.Spawners> monsterSpawns, ResourceLocation biomeName) {
        if (spawnConfig.shouldSpawn.get() && !spawnConfig.biomeBlackList.get().contains(biomeName)) {
            monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                //If the adult mob can spawn in this biome let the baby mob spawn in it
                //Note: We adjust the mob's spawning based on the adult's spawn rates
                EntityType<?> entityType = entityTypeProvider.getEntityType();
                int weight = (int) Math.ceil(parentEntry.itemWeight * spawnConfig.weightPercentage.get());
                int minSize = (int) Math.ceil(parentEntry.minCount * spawnConfig.minSizePercentage.get());
                int maxSize = (int) Math.ceil(parentEntry.maxCount * spawnConfig.maxSizePercentage.get());
                spawns.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(entityType, weight, minSize, Math.max(minSize, maxSize)));
                SpawnCosts parentCost = spawns.getCost(parent);
                if (parentCost != null) {
                    double spawnCostPerEntity = parentCost.getEntitySpawnCost() * spawnConfig.spawnCostPerEntityPercentage.get();
                    double maxSpawnCost = parentCost.getMaxSpawnCost() * spawnConfig.maxSpawnCostPercentage.get();
                    spawns.withSpawnCost(entityType, spawnCostPerEntity, maxSpawnCost);
                    Mekanism.logger.debug("Adding spawn rate for {} in biome {}, with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                          entityType.getRegistryName(), biomeName, weight, minSize, maxSize, spawnCostPerEntity, maxSpawnCost);
                } else {
                    Mekanism.logger.debug("Adding spawn rate for {} in biome {}, with weight: {}, minSize: {}, maxSize: {}", entityType.getRegistryName(),
                          biomeName, weight, minSize, maxSize);
                }
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