package mekanism.additions.common.world.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.additions.common.config.AdditionsConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsBiomeModifierSerializers;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList.Builder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo;

public record BabyEntitySpawnBiomeModifier(BabyType babyType, AdditionsConfig.SpawnConfig spawnConfig) implements BiomeModifier {

    public BabyEntitySpawnBiomeModifier(BabyType babyType) {
        this(babyType, MekanismAdditionsConfig.additions.getConfig(babyType));
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, BiomeInfo.Builder builder) {
        if (phase == Phase.REMOVE && spawnConfig.shouldSpawn.get()) {
            //Note: We need to run after addition in case we ran after any mods added their skeletons,
            // but we run before after everything to make it easier for another mod to remove us
            if (!biome.is(babyType.biomeBlacklist())) {
                MobSpawnSettingsBuilder mobSpawnSettings = builder.getMobSpawnSettings();
                Builder<SpawnerData> monsterSpawns = mobSpawnSettings.getSpawner(MobCategory.MONSTER);
                List<Weighted<MobSpawnSettings.SpawnerData>> spawnersToAdd = spawnConfig.getSpawnersToAdd(monsterSpawns.getList());
                if (!spawnersToAdd.isEmpty()) {
                    EntityType<?> parentType = BuiltInRegistries.ENTITY_TYPE.get(spawnConfig.parentType).map(Holder::value).orElse(null);
                    if (parentType == null) {
                        Mekanism.logger.warn("Parent entity type: '{}' was missing, when trying to add biome spawns for '{}' to '{}'. Spawns rates will not include a cost per entity.",
                              babyType.parentId(), babyType.id(), biome.getRegisteredName());
                    }
                    for (Weighted<SpawnerData> weightedSpawner : spawnersToAdd) {
                        SpawnerData spawner = weightedSpawner.value();
                        int weight = weightedSpawner.weight();
                        monsterSpawns.add(weightedSpawner);
                        MobSpawnSettings.MobSpawnCost parentCost = parentType == null ? null : mobSpawnSettings.getCost(parentType);
                        if (parentCost == null) {
                            Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}",
                                  babyType.id(), biome.getRegisteredName(), weight, spawner.minCount(), spawner.maxCount());
                        } else {
                            double spawnCostPerEntity = parentCost.charge() * spawnConfig.spawnCostPerEntityPercentage.get();
                            double maxSpawnCost = parentCost.energyBudget() * spawnConfig.maxSpawnCostPercentage.get();
                            mobSpawnSettings.addMobCharge(spawner.type(), spawnCostPerEntity, maxSpawnCost);
                            Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                                  babyType.id(), biome.getRegisteredName(), weight, spawner.minCount(), spawner.maxCount(), spawnCostPerEntity, maxSpawnCost);
                        }
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return AdditionsBiomeModifierSerializers.SPAWN_BABIES.get();
    }

    public static MapCodec<BabyEntitySpawnBiomeModifier> makeCodec() {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
              BabyType.CODEC.fieldOf(SerializationConstants.BABY_TYPE).forGetter(BabyEntitySpawnBiomeModifier::babyType)
        ).apply(builder, BabyEntitySpawnBiomeModifier::new));
    }
}