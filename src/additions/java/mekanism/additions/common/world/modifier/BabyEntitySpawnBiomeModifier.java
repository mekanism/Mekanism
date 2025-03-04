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
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
            if (!biome.is(babyType.biomeBlacklist)) {
                MobSpawnSettingsBuilder mobSpawnSettings = builder.getMobSpawnSettings();
                List<MobSpawnSettings.SpawnerData> monsterSpawns = mobSpawnSettings.getSpawner(MobCategory.MONSTER);
                for (MobSpawnSettings.SpawnerData spawner : spawnConfig.getSpawnersToAdd(monsterSpawns)) {
                    mobSpawnSettings.addSpawn(MobCategory.MONSTER, spawner);
                    MobSpawnSettings.MobSpawnCost parentCost = mobSpawnSettings.getCost(spawnConfig.parentType);
                    if (parentCost == null) {
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}",
                              Util.getRegisteredName(BuiltInRegistries.ENTITY_TYPE, spawner.type), biome.getRegisteredName(), spawner.getWeight(), spawner.minCount,
                              spawner.maxCount);
                    } else {
                        double spawnCostPerEntity = parentCost.charge() * spawnConfig.spawnCostPerEntityPercentage.get();
                        double maxSpawnCost = parentCost.energyBudget() * spawnConfig.maxSpawnCostPercentage.get();
                        mobSpawnSettings.addMobCharge(spawner.type, spawnCostPerEntity, maxSpawnCost);
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                              Util.getRegisteredName(BuiltInRegistries.ENTITY_TYPE, spawner.type), biome.getRegisteredName(), spawner.getWeight(), spawner.minCount,
                              spawner.maxCount, spawnCostPerEntity, maxSpawnCost);
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