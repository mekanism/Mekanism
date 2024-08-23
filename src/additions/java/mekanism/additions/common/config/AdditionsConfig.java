package mekanism.additions.common.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.additions.common.config.AdditionsConfigTranslations.BabySpawnTranslations;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AdditionsConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedIntValue obsidianTNTDelay;
    public final CachedFloatValue obsidianTNTBlastRadius;
    public final CachedDoubleValue babyArrowDamageMultiplier;
    public final CachedBooleanValue voiceServerEnabled;
    public final CachedIntValue voicePort;
    private final Map<BabyType, SpawnConfig> spawnConfigs = new EnumMap<>(BabyType.class);

    AdditionsConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        AdditionsConfigTranslations.SERVER_OBSIDIAN_TNT.applyToBuilder(builder).push("obsidian_tnt");
        obsidianTNTDelay = CachedIntValue.wrap(this, AdditionsConfigTranslations.SERVER_OBSIDIAN_DELAY.applyToBuilder(builder)
              .defineInRange("delay", 5 * SharedConstants.TICKS_PER_SECOND, 0, Integer.MAX_VALUE));
        obsidianTNTBlastRadius = CachedFloatValue.wrap(this, AdditionsConfigTranslations.SERVER_OBSIDIAN_RADIUS.applyToBuilder(builder)
              .defineInRange("blastRadius", 12, 0.1, 1_000));
        builder.pop();

        AdditionsConfigTranslations.SERVER_VOICE.applyToBuilder(builder).push("voice_server");
        voiceServerEnabled = CachedBooleanValue.wrap(this, AdditionsConfigTranslations.SERVER_VOICE_ENABLED.applyToBuilder(builder)
              .worldRestart()
              .define("enabled", false));
        voicePort = CachedIntValue.wrap(this, AdditionsConfigTranslations.SERVER_VOICE_PORT.applyToBuilder(builder)
              .defineInRange("voicePort", 36_123, 1, 65_535));
        builder.pop();

        AdditionsConfigTranslations.SERVER_BABY.applyToBuilder(builder).push("baby_mobs");
        babyArrowDamageMultiplier = CachedDoubleValue.wrap(this, AdditionsConfigTranslations.SERVER_BABY_ARROW_DAMAGE.applyToBuilder(builder)
              .defineInRange("arrowDamageMultiplier", 0.25, 0.1, 10));

        addBabyTypeConfig(BabyType.BOGGED, builder, AdditionsEntityTypes.BABY_BOGGED, EntityType.BOGGED);
        addBabyTypeConfig(BabyType.CREEPER, builder, AdditionsEntityTypes.BABY_CREEPER, EntityType.CREEPER);
        addBabyTypeConfig(BabyType.ENDERMAN, builder, AdditionsEntityTypes.BABY_ENDERMAN, EntityType.ENDERMAN);
        addBabyTypeConfig(BabyType.SKELETON, builder, AdditionsEntityTypes.BABY_SKELETON, EntityType.SKELETON);
        addBabyTypeConfig(BabyType.STRAY, builder, AdditionsEntityTypes.BABY_STRAY, EntityType.STRAY);
        addBabyTypeConfig(BabyType.WITHER_SKELETON, builder, AdditionsEntityTypes.BABY_WITHER_SKELETON, EntityType.WITHER_SKELETON);
        builder.pop();

        configSpec = builder.build();
    }

    private void addBabyTypeConfig(BabyType type, ModConfigSpec.Builder builder, Holder<EntityType<?>> entityTypeProvider, EntityType<?> parentType) {
        spawnConfigs.put(type, new SpawnConfig(this, builder, "baby_" + type.getSerializedName(), entityTypeProvider, parentType));
    }

    @Override
    public String getFileName() {
        return "additions";
    }

    @Override
    public String getTranslation() {
        return "General Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    public SpawnConfig getConfig(BabyType babyType) {
        return spawnConfigs.get(babyType);
    }

    public static class SpawnConfig {

        public final CachedBooleanValue shouldSpawn;
        public final CachedDoubleValue minSizePercentage;
        public final CachedDoubleValue maxSizePercentage;
        public final CachedDoubleValue weightPercentage;
        public final CachedDoubleValue spawnCostPerEntityPercentage;
        public final CachedDoubleValue maxSpawnCostPercentage;
        public final Holder<EntityType<?>> entityType;
        public final EntityType<?> parentType;

        private SpawnConfig(IMekanismConfig config, ModConfigSpec.Builder builder, String name, Holder<EntityType<?>> entityType, EntityType<?> parentType) {
            this.entityType = entityType;
            this.parentType = parentType;
            BabySpawnTranslations translations = BabySpawnTranslations.create(name);

            translations.topLevel().applyToBuilder(builder).push(name);
            this.shouldSpawn = CachedBooleanValue.wrap(config, translations.shouldSpawn().applyToBuilder(builder)
                  .worldRestart()
                  .define("shouldSpawn", true));
            this.minSizePercentage = CachedDoubleValue.wrap(config, translations.minSize().applyToBuilder(builder)
                  .worldRestart()
                  .defineInRange("minSizePercentage", 0.5, 0, 100));
            this.maxSizePercentage = CachedDoubleValue.wrap(config, translations.maxSize().applyToBuilder(builder)
                  .worldRestart()
                  .defineInRange("maxSizePercentage", 0.5, 0, 100));
            this.weightPercentage = CachedDoubleValue.wrap(config, translations.weight().applyToBuilder(builder)
                  .worldRestart()
                  .defineInRange("weightPercentage", 0.05, 0, 100));
            this.spawnCostPerEntityPercentage = CachedDoubleValue.wrap(config, translations.costPerEntity().applyToBuilder(builder)
                  .worldRestart()
                  .defineInRange("spawnCostPerEntityPercentage", 1D, 0, 100));
            this.maxSpawnCostPercentage = CachedDoubleValue.wrap(config, translations.maxCost().applyToBuilder(builder)
                  .worldRestart()
                  .defineInRange("maxSpawnCostPercentage", 1D, 0, 100));
            builder.pop();
        }

        public MobSpawnSettings.SpawnerData getSpawner(MobSpawnSettings.SpawnerData parentEntry) {
            int weight = Mth.ceil(parentEntry.getWeight().asInt() * weightPercentage.get());
            int minSize = Mth.ceil(parentEntry.minCount * minSizePercentage.get());
            int maxSize = Mth.ceil(parentEntry.maxCount * maxSizePercentage.get());
            return new MobSpawnSettings.SpawnerData(entityType.value(), weight, minSize, Math.max(minSize, maxSize));
        }

        public List<MobSpawnSettings.SpawnerData> getSpawnersToAdd(List<MobSpawnSettings.SpawnerData> monsterSpawns) {
            //If the adult mob can spawn let the baby mob spawn as well
            //Note: We adjust the mob's spawning based on the adult's spawn rates
            List<SpawnerData> list = new ArrayList<>();
            for (SpawnerData monsterSpawn : monsterSpawns) {
                if (monsterSpawn.type == parentType) {
                    list.add(getSpawner(monsterSpawn));
                }
            }
            return list;
        }
    }
}