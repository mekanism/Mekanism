package mekanism.additions.common.config;

import mekanism.additions.common.MekanismAdditions;
import mekanism.common.config.IConfigTranslation;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum AdditionsConfigTranslations implements IConfigTranslation {
    CLIENT_TOP_LEVEL("client", "Mekanism Additions Client Config. This config only exists on the client"),

    CLIENT_VOICE_KEY_TOGGLE("client.voice_key_is_toggle", "If the voice server is enabled and voiceKeyIsToggle is also enabled, the voice key will "
                                                          + "act as a toggle instead of requiring to be held while talking."),

    SERVER_TOP_LEVEL("server", "Mekanism Additions Config. This config is synced between server and client."),

    SERVER_OBSIDIAN_DELAY("server.obsidian_tnt.delay", "Fuse time for Obsidian TNT."),
    SERVER_OBSIDIAN_RADIUS("server.obsidian_tnt.radius", "Radius of the explosion of Obsidian TNT."),

    SERVER_VOICE_ENABLED("server.voice.enabled", "Enables the voice server for Walkie Talkies."),
    SERVER_VOICE_PORT("server.voice.port", "TCP port for the Voice server to listen on."),

    SERVER_BABY_ARROW_DAMAGE("server.baby.arrow_damage_multiplier", "Damage multiplier of arrows shot by baby mobs."),
    SERVER_BABY_SPAWNING("server.baby.spawning", "Config options regarding spawning of entities."),

    ;

    private final String key;
    private final String translation;

    AdditionsConfigTranslations(String path, String translation) {
        this.key = Util.makeDescriptionId("configuration", MekanismAdditions.rl(path));
        this.translation = translation;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String translation() {
        return translation;
    }

    public record BabySpawnTranslations(
          IConfigTranslation topLevel,
          IConfigTranslation shouldSpawn,
          IConfigTranslation weight,
          IConfigTranslation minSize,
          IConfigTranslation maxSize,
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
                  new ConfigTranslation(getKey(name, "top_level"), "Config options regarding " + name + "."),
                  new ConfigTranslation(getKey(name, "should_spawn"), "Enable the spawning of " + name + ". Think baby zombies."),
                  new ConfigTranslation(getKey(name, "weight"), "The multiplier for weight of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "min_size"), "The multiplier for minimum group size of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "max_size"), "The multiplier for maximum group size of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "cost_per_entity"), "The multiplier for spawn cost per entity of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "max_cost"), "The multiplier for max spawn cost of " + name + " spawns, compared to the adult mob."),
                  new ConfigTranslation(getKey(name, "biome_blacklist"), "The list of biome ids that " + name + " will not spawn in even if the normal mob variant can spawn."),
                  new ConfigTranslation(getKey(name, "structure_blacklist"), "The list of structure ids that " + name + " will not spawn in even if the normal mob variant can spawn.")
            );
        }

    }
}