package mekanism.additions.common.world.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.additions.common.config.AdditionsConfig;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsStructureModifierSerializers;
import mekanism.common.Mekanism;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo.StructureInfo;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.common.world.StructureSettingsBuilder;
import net.minecraftforge.common.world.StructureSettingsBuilder.StructureSpawnOverrideBuilder;

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
            ResourceLocation structureName = BuiltinRegistries.STRUCTURES.getKey(structure.get());
            if (spawnOverrides != null && !spawnConfig.structureBlackList.get().contains(structureName)) {
                for (MobSpawnSettings.SpawnerData spawner : spawnConfig.getSpawnersToAdd(spawnOverrides.getSpawns())) {
                    spawnOverrides.addSpawn(spawner);
                    Mekanism.logger.debug("Adding spawn rate for '{}' in structure '{}', with weight: {}, minSize: {}, maxSize: {}",
                          RegistryUtils.getName(spawner.type), structureName, spawner.getWeight(), spawner.minCount, spawner.maxCount);
                }
            }
        }
    }

    @Override
    public Codec<? extends StructureModifier> codec() {
        return AdditionsStructureModifierSerializers.SPAWN_BABIES.get();
    }

    public static Codec<BabyEntitySpawnStructureModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
              BabyType.CODEC.fieldOf("babyType").forGetter(BabyEntitySpawnStructureModifier::babyType)
        ).apply(builder, BabyEntitySpawnStructureModifier::new));
    }
}