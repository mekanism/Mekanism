package mekanism.additions.common.config;

import mekanism.additions.common.MekanismAdditions;
import mekanism.common.config.IConfigTranslation;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum AdditionsConfigTranslations implements IConfigTranslation {
    //Client Config
    CLIENT_PUSH_TO_TALK("client.push_to_talk", "Push to Talk",
          "If the voice server is enabled and pushToTalk is disabled, the voice key will act as a toggle instead of requiring to be held while talking."),

    //Server config
    SERVER_OBSIDIAN_TNT("server.obsidian_tnt", "Obsidian TNT", "Settings for configuring Obsidian TNT", true),
    SERVER_OBSIDIAN_DELAY("server.obsidian_tnt.delay", "Fuse Time", "Fuse time in ticks for Obsidian TNT. Vanilla TNT has a fuse of 80 ticks (4 seconds)."),
    SERVER_OBSIDIAN_RADIUS("server.obsidian_tnt.radius", "Radius", "Radius of the explosion of Obsidian TNT. Vanilla TNT has a radius of 4."),

    SERVER_VOICE("server.voice", "Voice Server", "Settings for configuring the Voice Server", true),
    SERVER_VOICE_ENABLED("server.voice.enabled", "Enabled", "Enables the voice server for Walkie Talkies."),
    SERVER_VOICE_PORT("server.voice.port", "TCP Port", "TCP port for the Voice server to listen on."),

    SERVER_BABY("server.baby", "Baby Mobs", "Settings for configuring values relating to baby mobs", true),
    SERVER_BABY_ARROW_DAMAGE("server.baby.arrow_damage_multiplier", "Arrow Damage Multiplier", "Damage multiplier of arrows shot by baby mobs."),

    ;

    private final String key;
    private final String title;
    private final String tooltip;
    @Nullable
    private final String button;

    AdditionsConfigTranslations(String path, String title, String tooltip) {
        this(path, title, tooltip, false);
    }

    AdditionsConfigTranslations(String path, String title, String tooltip, boolean isSection) {
        this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection));
    }

    AdditionsConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
        this.key = Util.makeDescriptionId("configuration", MekanismAdditions.rl(path));
        this.title = title;
        this.tooltip = tooltip;
        this.button = button;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }

    @Nullable
    @Override
    public String button() {
        return button;
    }

    public record BabySpawnTranslations(
          IConfigTranslation topLevel,
          IConfigTranslation shouldSpawn,
          IConfigTranslation minSize,
          IConfigTranslation maxSize,
          IConfigTranslation weight,
          IConfigTranslation costPerEntity,
          IConfigTranslation maxCost
    ) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, shouldSpawn, minSize, maxSize, weight, costPerEntity, maxCost};
        }

        private static String getKey(String name, String path) {
            return Util.makeDescriptionId("configuration", MekanismAdditions.rl("server.baby.spawning." + name + "." + path));
        }

        public static BabySpawnTranslations create(String key) {
            String name = TextUtils.formatAndCapitalize(key);
            return new BabySpawnTranslations(
                  new ConfigTranslation(getKey(key, "top_level"), name, "Config options regarding the spawning of " + name + ".", "Edit Spawn Settings"),
                  new ConfigTranslation(getKey(key, "should_spawn"), "Should Spawn", "Enable the spawning of " + name + ". Think baby zombies."),
                  new ConfigTranslation(getKey(key, "min_size"), "Min Group Size", "The multiplier for minimum group size of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(key, "max_size"), "Max Group Size", "The multiplier for maximum group size of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(key, "weight"), "Weight Multiplier", "The multiplier for weight of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(key, "cost_per_entity"), "Cost Per Entity Multiplier", "The multiplier for spawn cost per entity of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(key, "max_cost"), "Max Cost Multiplier", "The multiplier for max spawn cost of " + name + " spawns, compared to the adult mob.")
            );
        }

    }
}