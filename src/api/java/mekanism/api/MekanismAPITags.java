package mekanism.api;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;

/**
 * Provides access to pre-existing tag keys for various functionality that we use tags for.
 *
 * @since 10.6.2
 */
@NothingNullByDefault
public class MekanismAPITags {

    private static final ResourceLocation HIDDEN_RL = ResourceLocation.fromNamespaceAndPath("c", "hidden_from_recipe_viewers");

    private MekanismAPITags() {
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, path);
    }

    public static class Gases {

        private Gases() {
        }

        /**
         * Tag that holds all gases that recipe viewers should not show to users.
         */
        public static final TagKey<Gas> HIDDEN_FROM_RECIPE_VIEWERS = TagKey.create(MekanismAPI.GAS_REGISTRY_NAME, HIDDEN_RL);
        /**
         * Gases in this tag that are radioactive will not decay inside a Radioactive Waste Barrel.
         */
        public static final TagKey<Gas> WASTE_BARREL_DECAY_BLACKLIST = tag("waste_barrel_decay_blacklist");

        private static TagKey<Gas> tag(String name) {
            return TagKey.create(MekanismAPI.GAS_REGISTRY_NAME, rl(name));
        }
    }

    public static class InfuseTypes {

        private InfuseTypes() {
        }

        /**
         * Tag that holds all infuse types that recipe viewers should not show to users.
         */
        public static final TagKey<InfuseType> HIDDEN_FROM_RECIPE_VIEWERS = TagKey.create(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, HIDDEN_RL);

        /**
         * Represents an infuse type that is equivalent to carbon.
         */
        public static final TagKey<InfuseType> CARBON = tag("carbon");
        /**
         * Represents an infuse type that is equivalent to redstone.
         */
        public static final TagKey<InfuseType> REDSTONE = tag("redstone");
        /**
         * Represents an infuse type that is equivalent to diamond.
         */
        public static final TagKey<InfuseType> DIAMOND = tag("diamond");
        /**
         * Represents an infuse type that is equivalent to refined obsidian.
         */
        public static final TagKey<InfuseType> REFINED_OBSIDIAN = tag("refined_obsidian");
        /**
         * Represents an infuse type that is equivalent to bio.
         */
        public static final TagKey<InfuseType> BIO = tag("bio");
        /**
         * Represents an infuse type that is equivalent to fungi.
         */
        public static final TagKey<InfuseType> FUNGI = tag("fungi");
        /**
         * Represents an infuse type that is equivalent to gold.
         */
        public static final TagKey<InfuseType> GOLD = tag("gold");
        /**
         * Represents an infuse type that is equivalent to tin.
         */
        public static final TagKey<InfuseType> TIN = tag("tin");

        private static TagKey<InfuseType> tag(String name) {
            return TagKey.create(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, rl(name));
        }
    }

    public static class Pigments {

        private Pigments() {
        }

        /**
         * Tag that holds all pigments that recipe viewers should not show to users.
         */
        public static final TagKey<Pigment> HIDDEN_FROM_RECIPE_VIEWERS = TagKey.create(MekanismAPI.PIGMENT_REGISTRY_NAME, HIDDEN_RL);
    }

    public static class Slurries {

        private Slurries() {
        }

        /**
         * Tag that holds all slurries that recipe viewers should not show to users.
         */
        public static final TagKey<Slurry> HIDDEN_FROM_RECIPE_VIEWERS = TagKey.create(MekanismAPI.SLURRY_REGISTRY_NAME, HIDDEN_RL);

        /**
         * Represents all dirty slurries.
         */
        public static final TagKey<Slurry> DIRTY = tag("dirty");
        /**
         * Represents all clean slurries.
         */
        public static final TagKey<Slurry> CLEAN = tag("clean");

        private static TagKey<Slurry> tag(String name) {
            return TagKey.create(MekanismAPI.SLURRY_REGISTRY_NAME, rl(name));
        }
    }

    public static class MobEffects {

        private MobEffects() {
        }

        /**
         * Mob effects in this tag, will be skipped when trying to speed up potion effects with a Scuba Mask or the Inhalation Purification Unit.
         */
        public static final TagKey<MobEffect> SPEED_UP_BLACKLIST = tag("speed_up_blacklist");

        private static TagKey<MobEffect> tag(String name) {
            return TagKey.create(Registries.MOB_EFFECT, rl(name));
        }
    }

    public static class DamageTypes {

        private DamageTypes() {
        }

        /**
         * Represents any damage type that is always supported by the MekaSuit.
         */
        public static final TagKey<DamageType> MEKASUIT_ALWAYS_SUPPORTED = tag("mekasuit_always_supported");
        /**
         * Represents any type of damage that can be prevented by the Scuba Mask or the Inhalation Purification Unit.
         */
        public static final TagKey<DamageType> IS_PREVENTABLE_MAGIC = tag("is_preventable_magic");

        private static TagKey<DamageType> tag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, rl(name));
        }
    }

    /**
     * @since 10.6.3
     */
    public static class Entities {

        private Entities() {
        }

        /**
         * Represents any entity type that is immune to all Radiation.
         */
        public static final TagKey<EntityType<?>> RADIATION_IMMUNE = commonTag("radiation_immune");
        /**
         * Represents any entity type that is immune to Mekanism Radiation.
         */
        public static final TagKey<EntityType<?>> MEK_RADIATION_IMMUNE = tag("radiation_immune");

        private static TagKey<EntityType<?>> commonTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, rl(name));
        }
    }
}