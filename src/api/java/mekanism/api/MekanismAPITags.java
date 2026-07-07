package mekanism.api;

import mekanism.api.chemical.Chemical;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;

/// Provides access to pre-existing tag keys for various functionality that we use tags for.
///
/// @since 10.6.2
public class MekanismAPITags {

    private static final Identifier HIDDEN_RL = Identifier.fromNamespaceAndPath("c", "hidden_from_recipe_viewers");

    private MekanismAPITags() {
    }

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, path);
    }

    /// @since 10.6.4
    public static class Biomes {

        private Biomes() {
        }

        /// Represents any biomes that baby bogged should not spawn in, even if normal bogged do.
        public static final TagKey<Biome> BLACKLIST_BABY_BOGGED = additionsTag("blacklist/baby_bogged");
        /// Represents any biomes that baby creepers should not spawn in, even if normal creepers do.
        public static final TagKey<Biome> BLACKLIST_BABY_CREEPERS = additionsTag("blacklist/baby_creeper");
        /// Represents any biomes that baby endermen should not spawn in, even if normal endermen do.
        public static final TagKey<Biome> BLACKLIST_BABY_ENDERMEN = additionsTag("blacklist/baby_enderman");
        /// Represents any biomes that baby parched should not spawn in, even if normal parched do.
        public static final TagKey<Biome> BLACKLIST_BABY_PARCHED = additionsTag("blacklist/baby_parched");
        /// Represents any biomes that baby skeletons should not spawn in, even if normal skeletons do.
        public static final TagKey<Biome> BLACKLIST_BABY_SKELETONS = additionsTag("blacklist/baby_skeleton");
        /// Represents any biomes that baby strays should not spawn in, even if normal strays do.
        public static final TagKey<Biome> BLACKLIST_BABY_STRAYS = additionsTag("blacklist/baby_stray");
        /// Represents any biomes that baby wither skeletons should not spawn in, even if normal wither skeletons do.
        public static final TagKey<Biome> BLACKLIST_BABY_WITHER_SKELETONS = additionsTag("blacklist/baby_wither_skeleton");

        private static TagKey<Biome> additionsTag(String name) {
            return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MekanismAPI.ADDITIONS_MODID, name));
        }
    }

    /// @since 10.7.15
    public static class Blocks {

        private Blocks() {
        }

        /// Represents any blocks that normally would have tier installers work on them, that should deny functionality.
        public static final TagKey<Block> BLACKLIST_INSTALLER_UPGRADEABLE = tag("blacklist/installer_upgradeable");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, rl(name));
        }
    }

    /// @since 10.7.0
    public static class Chemicals {

        private Chemicals() {
        }

        /// Tag that holds all chemicals that recipe viewers should not show to users.
        public static final TagKey<Chemical> HIDDEN_FROM_RECIPE_VIEWERS = TagKey.create(MekanismRegistries.Keys.CHEMICAL, HIDDEN_RL);

        /// Chemicals in this tag that are radioactive will not decay inside a Radioactive Waste Barrel.
        public static final TagKey<Chemical> WASTE_BARREL_DECAY_BLACKLIST = tag("waste_barrel_decay_blacklist");

        /// Chemicals in this tag will be rendered as a gas, rather than similarly to fluids.
        public static final TagKey<Chemical> GASEOUS = tag("gaseous");

        /// Represents an infuse type that is equivalent to carbon.
        public static final TagKey<Chemical> CARBON = tag("carbon");
        /// Represents an infuse type that is equivalent to redstone.
        public static final TagKey<Chemical> REDSTONE = tag("redstone");
        /// Represents an infuse type that is equivalent to diamond.
        public static final TagKey<Chemical> DIAMOND = tag("diamond");
        /// Represents an infuse type that is equivalent to refined obsidian.
        public static final TagKey<Chemical> REFINED_OBSIDIAN = tag("refined_obsidian");
        /// Represents an infuse type that is equivalent to bio.
        public static final TagKey<Chemical> BIO = tag("bio");
        /// Represents an infuse type that is equivalent to fungi.
        public static final TagKey<Chemical> FUNGI = tag("fungi");
        /// Represents an infuse type that is equivalent to gold.
        public static final TagKey<Chemical> GOLD = tag("gold");
        /// Represents an infuse type that is equivalent to tin.
        public static final TagKey<Chemical> TIN = tag("tin");

        /// Represents all dirty slurries.
        public static final TagKey<Chemical> DIRTY = tag("dirty");
        /// Represents all clean slurries.
        public static final TagKey<Chemical> CLEAN = tag("clean");
        /// Chemicals in this tag cannot be inserted into framed blocks
        ///
        /// @since 10.7.3
        public static final TagKey<Chemical> FRAMEDBLOCKS_BLACKLISTED = tag("framedblocks_blacklisted");

        private static TagKey<Chemical> tag(String name) {
            return TagKey.create(MekanismRegistries.Keys.CHEMICAL, rl(name));
        }
    }

    public static class DamageTypes {

        private DamageTypes() {
        }

        /// Represents any damage type that is always supported by the MekaSuit.
        public static final TagKey<DamageType> MEKASUIT_ALWAYS_SUPPORTED = tag("mekasuit_always_supported");
        /// Represents any type of damage that can be prevented by the Scuba Mask or the Inhalation Purification Unit.
        public static final TagKey<DamageType> IS_PREVENTABLE_MAGIC = tag("is_preventable_magic");

        private static TagKey<DamageType> tag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, rl(name));
        }
    }

    /// @since 10.6.4
    public static class DimensionTypes {

        private DimensionTypes() {
        }

        /// Represents any dimension without wind.
        ///
        /// @apiNote This is used by Mekanism to determine what dimensions a wind generator can function in.
        public static final TagKey<DimensionType> NO_WIND = commonTag("no_wind");

        private static TagKey<DimensionType> commonTag(String name) {
            return TagKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath("c", name));
        }
    }

    /// @since 10.6.3
    public static class Entities {

        private Entities() {
        }

        /// Represents any entity type that is immune to all Radiation.
        public static final TagKey<EntityType<?>> RADIATION_IMMUNE = commonTag("radiation_immune");
        /// Represents any entity type that is immune to Mekanism Radiation.
        public static final TagKey<EntityType<?>> MEK_RADIATION_IMMUNE = tag("radiation_immune");

        private static TagKey<EntityType<?>> commonTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", name));
        }

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, rl(name));
        }
    }

    /// @since 10.7.0
    public static class Items {

        private Items() {
        }

        /// @since 10.8.0
        public static final TagKey<Item> MEKASUIT_HUD_RENDERER = tag("mekasuit_hud_renderer");
        /// Represents all items that are containers that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS = tag("module_containers");
        /// Represents all items that are colorable containers that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS_COLORABLE = tag("module_containers/colorable");
        /// Represents all items that are armors that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS_ARMOR = tag("module_containers/armor");
        /// Represents all items that are helmets that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS_ARMOR_HELMET = tag("module_containers/armor/helmet");
        /// Represents all items that are chestplates that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS_ARMOR_CHESTPLATE = tag("module_containers/armor/chestplate");
        /// Represents all items that are leggings that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS_ARMOR_LEGGINGS = tag("module_containers/armor/leggings");
        /// Represents all items that are boots that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS_ARMOR_BOOTS = tag("module_containers/armor/boots");
        /// Represents all items that are Meka-Tools that modules can be installed on
        ///
        /// @since 10.8.0
        public static final TagKey<Item> MODULE_CONTAINERS_MEKA_TOOL = tag("module_containers/meka_tool");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, rl(name));
        }
    }

    public static class MobEffects {

        private MobEffects() {
        }

        /// Mob effects in this tag, will be skipped when trying to speed up potion effects with a Scuba Mask or the Inhalation Purification Unit.
        public static final TagKey<MobEffect> SPEED_UP_BLACKLIST = tag("speed_up_blacklist");

        private static TagKey<MobEffect> tag(String name) {
            return TagKey.create(Registries.MOB_EFFECT, rl(name));
        }
    }


    /// @since 10.6.4
    public static class Structures {

        private Structures() {
        }

        /// Represents any structure that baby bogged should not spawn in, even if normal bogged do.
        public static final TagKey<Structure> BLACKLIST_BABY_BOGGED = additionsTag("blacklist/baby_bogged");
        /// Represents any structure that baby creepers should not spawn in, even if normal creepers do.
        public static final TagKey<Structure> BLACKLIST_BABY_CREEPERS = additionsTag("blacklist/baby_creeper");
        /// Represents any structure that baby endermen should not spawn in, even if normal endermen do.
        public static final TagKey<Structure> BLACKLIST_BABY_ENDERMEN = additionsTag("blacklist/baby_enderman");
        /// Represents any structure that baby parched should not spawn in, even if normal skeletons do.
        public static final TagKey<Structure> BLACKLIST_BABY_PARCHED = additionsTag("blacklist/baby_parched");
        /// Represents any structure that baby skeletons should not spawn in, even if normal skeletons do.
        public static final TagKey<Structure> BLACKLIST_BABY_SKELETONS = additionsTag("blacklist/baby_skeleton");
        /// Represents any structure that baby strays should not spawn in, even if normal strays do.
        public static final TagKey<Structure> BLACKLIST_BABY_STRAYS = additionsTag("blacklist/baby_stray");
        /// Represents any structure that baby wither skeletons should not spawn in, even if normal wither skeletons do.
        public static final TagKey<Structure> BLACKLIST_BABY_WITHER_SKELETONS = additionsTag("blacklist/baby_wither_skeleton");

        private static TagKey<Structure> additionsTag(String name) {
            return TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(MekanismAPI.ADDITIONS_MODID, name));
        }
    }
}