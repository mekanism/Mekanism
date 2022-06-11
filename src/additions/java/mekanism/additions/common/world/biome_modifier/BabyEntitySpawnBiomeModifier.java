package mekanism.additions.common.world.biome_modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import mekanism.additions.common.config.AdditionsCommonConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.SpawnHelper;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsBiomeModifierSerializers;
import mekanism.common.Mekanism;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

public record BabyEntitySpawnBiomeModifier(BabyType babyType, AdditionsCommonConfig.SpawnConfig spawnConfig) implements BiomeModifier {

    public BabyEntitySpawnBiomeModifier(BabyType babyType) {
        //TODO - 1.19: Should this technically be a server config so it syncs properly? It can't be quite yet just due to the structure blacklist
        this(babyType, MekanismAdditionsConfig.common.getConfig(babyType));
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, BiomeInfo.Builder builder) {
        if (phase == Phase.REMOVE) {
            //Note: We need to run after addition in case we ran after any mods added their skeletons,
            // but we run before after everything to make it easier for another mod to remove us
            addSpawns(biome, builder);
        }
    }

    private void addSpawns(Holder<Biome> biome, BiomeInfo.Builder builder) {
        //TODO - 1.19: Decide if we want to move parts of this and the base mekanism modifiers from configs to seriallizers
        if (spawnConfig.shouldSpawn.get()) {
            MobSpawnSettingsBuilder mobSpawnSettings = builder.getMobSpawnSettings();
            List<SpawnerData> monsterSpawns = mobSpawnSettings.getSpawner(MobCategory.MONSTER);
            ResourceLocation biomeName = ForgeRegistries.BIOMES.getKey(biome.get());
            if (!monsterSpawns.isEmpty() && !spawnConfig.biomeBlackList.get().contains(biomeName)) {
                EntityType<?> parent = spawnConfig.parentTypeProvider.getEntityType();
                List<SpawnerData> spawnersToAdd = new ArrayList<>();
                for (SpawnerData monsterSpawn : monsterSpawns) {
                    if (monsterSpawn.type == parent) {
                        //If the adult mob can spawn in this biome let the baby mob spawn in it
                        //Note: We adjust the mob's spawning based on the adult's spawn rates
                        spawnersToAdd.add(SpawnHelper.getSpawner(spawnConfig, monsterSpawn));
                    }
                }
                for (SpawnerData spawner : spawnersToAdd) {
                    mobSpawnSettings.addSpawn(MobCategory.MONSTER, spawner);
                    MobSpawnSettings.MobSpawnCost parentCost = mobSpawnSettings.getCost(parent);
                    if (parentCost == null) {
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}",
                              RegistryUtils.getName(spawner.type), biomeName, spawner.getWeight(), spawner.minCount, spawner.maxCount);
                    } else {
                        double spawnCostPerEntity = parentCost.getCharge() * spawnConfig.spawnCostPerEntityPercentage.get();
                        double maxSpawnCost = parentCost.getEnergyBudget() * spawnConfig.maxSpawnCostPercentage.get();
                        mobSpawnSettings.addMobCharge(spawner.type, spawnCostPerEntity, maxSpawnCost);
                        Mekanism.logger.debug("Adding spawn rate for '{}' in biome '{}', with weight: {}, minSize: {}, maxSize: {}, spawnCostPerEntity: {}, maxSpawnCost: {}",
                              RegistryUtils.getName(spawner.type), biomeName, spawner.getWeight(), spawner.minCount, spawner.maxCount, spawnCostPerEntity, maxSpawnCost);
                    }
                }
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return AdditionsBiomeModifierSerializers.SPAWN_BABIES.get();
    }

    public static Codec<BabyEntitySpawnBiomeModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
              BabyType.CODEC.fieldOf("babyType").forGetter(BabyEntitySpawnBiomeModifier::babyType)
        ).apply(builder, BabyEntitySpawnBiomeModifier::new));
    }
}