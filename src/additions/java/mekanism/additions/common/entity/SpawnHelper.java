package mekanism.additions.common.entity;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import mekanism.additions.common.config.AdditionsCommonConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.item.AdditionsSpawnEggItem;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Direction;
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
        //Add the global entity type attributes for the entities
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_CREEPER.get(), CreeperEntity.registerAttributes().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_ENDERMAN.get(), EndermanEntity.func_234287_m_().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_SKELETON.get(), AbstractSkeletonEntity.registerAttributes().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_STRAY.get(), AbstractSkeletonEntity.registerAttributes().create());
        GlobalEntityTypeAttributes.put(AdditionsEntityTypes.BABY_WITHER_SKELETON.get(), AbstractSkeletonEntity.registerAttributes().create());
        //Add dispenser spawn egg behaviors
        registerDelayedDispenserBehavior(AdditionsItems.BABY_CREEPER_SPAWN_EGG, AdditionsItems.BABY_ENDERMAN_SPAWN_EGG, AdditionsItems.BABY_SKELETON_SPAWN_EGG,
              AdditionsItems.BABY_STRAY_SPAWN_EGG, AdditionsItems.BABY_WITHER_SKELETON_SPAWN_EGG);
        //Add parrot sound imitations for baby mobs
        //Note: There is no imitation sound for endermen
        ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_CREEPER.get(), SoundEvents.ENTITY_PARROT_IMITATE_CREEPER);
        ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_SKELETON.get(), SoundEvents.ENTITY_PARROT_IMITATE_SKELETON);
        ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_STRAY.get(), SoundEvents.ENTITY_PARROT_IMITATE_STRAY);
        ParrotEntity.IMITATION_SOUND_EVENTS.put(AdditionsEntityTypes.BABY_WITHER_SKELETON.get(), SoundEvents.ENTITY_PARROT_IMITATE_WITHER_SKELETON);
    }

    @SafeVarargs
    private static void registerSpawnControls(EntityTypeRegistryObject<? extends MonsterEntity>... entityTypeROs) {
        for (EntityTypeRegistryObject<? extends MonsterEntity> entityTypeRO : entityTypeROs) {
            EntitySpawnPlacementRegistry.register(entityTypeRO.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                  MonsterEntity::canMonsterSpawnInLight);
        }
    }

    @SafeVarargs
    private static void registerDelayedDispenserBehavior(ItemRegistryObject<AdditionsSpawnEggItem>... spawnEggs) {
        DefaultDispenseItemBehavior dispenseBehavior = new DefaultDispenseItemBehavior() {
            @Nonnull
            @Override
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                Direction direction = source.getBlockState().get(DispenserBlock.FACING);
                EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
                entityType.spawn(source.getWorld(), stack, null, source.getBlockPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
                stack.shrink(1);
                return stack;
            }
        };
        //TODO: Remove this when we can, for now just lazy add the dispense behavior
        for (ItemRegistryObject<AdditionsSpawnEggItem> spawnEgg : spawnEggs) {
            DispenserBlock.registerDispenseBehavior(spawnEgg, dispenseBehavior);
        }
    }

    private static MobSpawnInfo.Spawners getSpawner(AdditionsCommonConfig.SpawnConfig spawnConfig, MobSpawnInfo.Spawners parentEntry) {
        int weight = (int) Math.ceil(parentEntry.itemWeight * spawnConfig.weightPercentage.get());
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
            //Fail quick if no monsters can spawn in this biome anyways
            ResourceLocation biomeName = event.getName();
            getSpawnConfigs().filter(spawnConfig -> spawnConfig.shouldSpawn.get() && !spawnConfig.biomeBlackList.get().contains(biomeName)).forEach(spawnConfig -> {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                    //If the adult mob can spawn in this biome let the baby mob spawn in it
                    //Note: We adjust the mob's spawning based on the adult's spawn rates
                    MobSpawnInfo.Spawners spawner = getSpawner(spawnConfig, parentEntry);
                    spawns.withSpawner(EntityClassification.MONSTER, spawner);
                    MobSpawnInfo.SpawnCosts parentCost = spawns.getCost(parent);
                    if (parentCost == null) {
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}", spawner.type.getRegistryName(),
                              biomeName, spawner.itemWeight, spawner.minCount, spawner.maxCount);
                    } else {
                        double spawnCostPerEntity = parentCost.getEntitySpawnCost() * spawnConfig.spawnCostPerEntityPercentage.get();
                        double maxSpawnCost = parentCost.getMaxSpawnCost() * spawnConfig.maxSpawnCostPercentage.get();
                        spawns.withSpawnCost(spawner.type, spawnCostPerEntity, maxSpawnCost);
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                              spawner.type.getRegistryName(), biomeName, spawner.itemWeight, spawner.minCount, spawner.maxCount, spawnCostPerEntity, maxSpawnCost);
                    }
                });
            });
        }
    }

    public static void onStructureSpawnListGather(StructureSpawnListGatherEvent event) {
        //Add special spawns to any structures that have mob spawns for the "parent" types of our mobs
        List<MobSpawnInfo.Spawners> monsterSpawns = event.getEntitySpawns(EntityClassification.MONSTER);
        if (!monsterSpawns.isEmpty()) {
            //Fail quick if no monsters can spawn in this structure anyways
            ResourceLocation structureName = event.getStructure().getRegistryName();
            getSpawnConfigs().filter(spawnConfig -> spawnConfig.shouldSpawn.get() && !spawnConfig.structureBlackList.get().contains(structureName)).forEach(spawnConfig -> {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                monsterSpawns.stream().filter(monsterSpawn -> monsterSpawn.type == parent).findFirst().ifPresent(parentEntry -> {
                    //If the adult mob can spawn in this structure let the baby mob spawn in it
                    //Note: We adjust the mob's spawning based on the adult's spawn rates
                    MobSpawnInfo.Spawners spawner = getSpawner(spawnConfig, parentEntry);
                    event.addEntitySpawn(EntityClassification.MONSTER, spawner);
                    Mekanism.logger.debug("Adding spawn rate for '{}' in structure '{}', with weight: {}, minSize: {}, maxSize: {}", spawner.type.getRegistryName(),
                          structureName, spawner.itemWeight, spawner.minCount, spawner.maxCount);
                });
            });
        }
    }
}