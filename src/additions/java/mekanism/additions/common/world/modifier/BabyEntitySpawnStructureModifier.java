package mekanism.additions.common.world.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.additions.common.config.AdditionsConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsStructureModifierSerializers;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo.StructureInfo;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.common.world.StructureSettingsBuilder;
import net.neoforged.neoforge.common.world.StructureSettingsBuilder.StructureSpawnOverrideBuilder;

public record BabyEntitySpawnStructureModifier(BabyType babyType, AdditionsConfig.SpawnConfig spawnConfig) implements StructureModifier {

    public BabyEntitySpawnStructureModifier(BabyType babyType) {
        this(babyType, MekanismAdditionsConfig.additions.getConfig(babyType));
    }

    @Override
    public void modify(Holder<Structure> structure, Phase phase, StructureInfo.Builder builder) {
        if (phase == Phase.REMOVE && spawnConfig.shouldSpawn.get()) {
            //Note: We need to run after addition in case we ran after any mods added their skeletons,
            // but we run before after everything to make it easier for another mod to remove us
            StructureSettingsBuilder structureSettings = builder.getStructureSettings();
            StructureSpawnOverrideBuilder spawnOverrides = structureSettings.getSpawnOverrides(MobCategory.MONSTER);
            //Fail quick if there are no overrides for this structure, or it is blacklisted
            if (spawnOverrides != null && !structure.is(babyType.structureBlacklist)) {
                for (MobSpawnSettings.SpawnerData spawner : spawnConfig.getSpawnersToAdd(spawnOverrides.getSpawns())) {
                    spawnOverrides.addSpawn(spawner);
                    ResourceKey<Structure> structureKey = structure.getKey();
                    Mekanism.logger.debug("Adding spawn rate for '{}' in structure '{}', with weight: {}, minSize: {}, maxSize: {}",
                          Util.getRegisteredName(BuiltInRegistries.ENTITY_TYPE, spawner.type), structureKey == null ? null : structureKey.location(), spawner.getWeight(),
                          spawner.minCount, spawner.maxCount);
                }
            }
        }
    }

    @Override
    public MapCodec<? extends StructureModifier> codec() {
        return AdditionsStructureModifierSerializers.SPAWN_BABIES.get();
    }

    public static MapCodec<BabyEntitySpawnStructureModifier> makeCodec() {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
              BabyType.CODEC.fieldOf(SerializationConstants.BABY_TYPE).forGetter(BabyEntitySpawnStructureModifier::babyType)
        ).apply(builder, BabyEntitySpawnStructureModifier::new));
    }
}