package mekanism.additions.common.config;

import mekanism.additions.common.MekanismAdditions;
import mekanism.common.config.IConfigTranslation;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum AdditionsConfigTranslations implements IConfigTranslation {
    //Client Config
    CLIENT_VOICE_KEY_TOGGLE("client.voice_key_is_toggle", "Voice Key is Toggle",
          "If the voice server is enabled and voiceKeyIsToggle is also enabled, the voice key will act as a toggle instead of requiring to be held while talking."),

    //Server config
    SERVER_OBSIDIAN_TNT("server.obsidian_tnt", "Obsidian TNT", "Settings for configuring Obsidian TNT"),
    SERVER_OBSIDIAN_DELAY("server.obsidian_tnt.delay", "Fuse Time", "Fuse time for Obsidian TNT."),
    SERVER_OBSIDIAN_RADIUS("server.obsidian_tnt.radius", "Radius", "Radius of the explosion of Obsidian TNT."),

    SERVER_VOICE("server.voice", "Voice Server", "Settings for configuring the Voice Server"),
    SERVER_VOICE_ENABLED("server.voice.enabled", "Enabled", "Enables the voice server for Walkie Talkies."),
    SERVER_VOICE_PORT("server.voice.port", "TCP Port", "TCP port for the Voice server to listen on."),

    SERVER_BABY("server.baby", "Baby Mobs", "Settings for configuring values relating to baby mobs"),
    SERVER_BABY_ARROW_DAMAGE("server.baby.arrow_damage_multiplier", "Arrow Damage Multiplier", "Damage multiplier of arrows shot by baby mobs."),
    SERVER_BABY_SPAWNING("server.baby.spawning", "Entity Spawning", "Config options regarding spawning of entities."),

    ;

    private final String key;
    private final String title;
    private final String tooltip;

    AdditionsConfigTranslations(String path, String title, String tooltip) {
        this.key = Util.makeDescriptionId("configuration", MekanismAdditions.rl(path));
        this.title = title;
        this.tooltip = tooltip;
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

    public record BabySpawnTranslations(
          IConfigTranslation topLevel,
          IConfigTranslation shouldSpawn,
          IConfigTranslation minSize,
          IConfigTranslation maxSize,
          IConfigTranslation weight,
          IConfigTranslation costPerEntity,
          IConfigTranslation maxCost,
          IConfigTranslation biomeBlacklist,
          IConfigTranslation structureBlacklist
    ) {

        private static String getKey(String name, String path) {
            name = name.replace(" ", "_");
            return Util.makeDescriptionId("configuration", MekanismAdditions.rl("server.baby.spawning." + name + "." + path));
        }

        public static BabySpawnTranslations create(String name) {
            return new BabySpawnTranslations(
                  new ConfigTranslation(getKey(name, "top_level"), name, "Config options regarding " + name + "."),
                  new ConfigTranslation(getKey(name, "should_spawn"), "Should Spawn", "Enable the spawning of " + name + ". Think baby zombies."),
                  new ConfigTranslation(getKey(name, "min_size"), "Min Group Size", "The multiplier for minimum group size of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "max_size"), "Max Group Size", "The multiplier for maximum group size of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "weight"), "Weight Multiplier", "The multiplier for weight of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "cost_per_entity"), "Cost Per Entity Multiplier", "The multiplier for spawn cost per entity of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "max_cost"), "Max Cost Multiplier", "The multiplier for max spawn cost of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "biome_blacklist"), "Biome Blacklist", "The list of biome ids that " + name + " will not spawn in even if the normal mob variant can spawn."),
                  new ConfigTranslation(getKey(name, "structure_blacklist"), "Structure Blacklist", "The list of structure ids that " + name + " will not spawn in even if the normal mob variant can spawn.")
            );
        }

    }
}