package mekanism.api.chemical;

import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import mekanism.api.text.EnumColorCollection;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/// @since 10.8.0
public class ChemicalIds {

    private ChemicalIds() {
    }

    public static final ResourceKey<Chemical> EMPTY = key("empty");

    // ----------------------
    //
    // Infuse Types
    //
    // ----------------------

    public static final ResourceKey<Chemical> BIO = key("bio");
    public static final ResourceKey<Chemical> FUNGI = key("fungi");
    public static final ResourceKey<Chemical> TIN = key("tin");
    public static final ResourceKey<Chemical> GOLD = key("gold");
    public static final ResourceKey<Chemical> REFINED_OBSIDIAN = key("refined_obsidian");
    public static final ResourceKey<Chemical> DIAMOND = key("diamond");
    public static final ResourceKey<Chemical> REDSTONE = key("redstone");
    public static final ResourceKey<Chemical> CARBON = key("carbon");

    // ----------------------
    //
    // Chemicals
    //
    // ----------------------

    public static final ResourceKey<Chemical> HYDROGEN = key("hydrogen");
    public static final ResourceKey<Chemical> OXYGEN = key("oxygen");
    public static final ResourceKey<Chemical> STEAM = key("steam");
    public static final ResourceKey<Chemical> WATER_VAPOR = key("water_vapor");
    public static final ResourceKey<Chemical> CHLORINE = key("chlorine");
    public static final ResourceKey<Chemical> SULFUR_DIOXIDE = key("sulfur_dioxide");
    public static final ResourceKey<Chemical> SULFUR_TRIOXIDE = key("sulfur_trioxide");
    public static final ResourceKey<Chemical> SULFURIC_ACID = key("sulfuric_acid");
    public static final ResourceKey<Chemical> HYDROGEN_CHLORIDE = key("hydrogen_chloride");
    public static final ResourceKey<Chemical> HYDROFLUORIC_ACID = key("hydrofluoric_acid");
    public static final ResourceKey<Chemical> URANIUM_OXIDE = key("uranium_oxide");
    public static final ResourceKey<Chemical> URANIUM_HEXAFLUORIDE = key("uranium_hexafluoride");
    public static final ResourceKey<Chemical> ETHENE = key("ethene");
    public static final ResourceKey<Chemical> SODIUM = key("sodium");
    public static final ResourceKey<Chemical> SUPERHEATED_SODIUM = key("superheated_sodium");
    public static final ResourceKey<Chemical> BRINE = key("brine");
    public static final ResourceKey<Chemical> LITHIUM = key("lithium");
    public static final ResourceKey<Chemical> OSMIUM = key("osmium");
    public static final ResourceKey<Chemical> FISSILE_FUEL = key("fissile_fuel");
    public static final ResourceKey<Chemical> NUCLEAR_WASTE = key("nuclear_waste");
    public static final ResourceKey<Chemical> SPENT_NUCLEAR_WASTE = key("spent_nuclear_waste");
    public static final ResourceKey<Chemical> PLUTONIUM = key("plutonium");
    public static final ResourceKey<Chemical> POLONIUM = key("polonium");
    public static final ResourceKey<Chemical> ANTIMATTER = key("antimatter");

    // ----------------------
    //
    // Pigments
    //
    // ----------------------

    public static EnumColorCollection<ResourceKey<Chemical>> SIMPLE_PIGMENTS = EnumColorCollection.VALUES.map(color -> key(color.getRegistryPrefix()));

    // ----------------------
    //
    // Slurries
    //
    // ----------------------

    public static final CleanDirtySlurryId IRON_SLURRY = slurry("iron");
    public static final CleanDirtySlurryId GOLD_SLURRY = slurry("gold");
    public static final CleanDirtySlurryId OSMIUM_SLURRY = slurry("osmium");
    public static final CleanDirtySlurryId COPPER_SLURRY = slurry("copper");
    public static final CleanDirtySlurryId TIN_SLURRY = slurry("tin");
    public static final CleanDirtySlurryId LEAD_SLURRY = slurry("lead");
    public static final CleanDirtySlurryId URANIUM_SLURRY = slurry("uranium");

    // ----------------------
    //
    // Mekanism Generators
    //
    // ----------------------

    /// @apiNote Only will be present in the chemical registry if Mekanism Generators is present, or a datapack adds it.
    public static final ResourceKey<Chemical> DEUTERIUM = generatorsKey("deuterium");
    /// @apiNote Only will be present in the chemical registry if Mekanism Generators is present, or a datapack adds it.
    public static final ResourceKey<Chemical> TRITIUM = generatorsKey("tritium");
    /// @apiNote Only will be present in the chemical registry if Mekanism Generators is present, or a datapack adds it.
    public static final ResourceKey<Chemical> FUSION_FUEL = generatorsKey("fusion_fuel");

    private static ResourceKey<Chemical> key(Identifier id) {
        return ResourceKey.create(MekanismRegistries.Keys.CHEMICAL, id);
    }

    private static ResourceKey<Chemical> key(String name) {
        return key(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, name));
    }

    private static ResourceKey<Chemical> generatorsKey(String name) {
        return key(Identifier.fromNamespaceAndPath(MekanismAPI.GENERATORS_MODID, name));
    }

    private static CleanDirtySlurryId slurry(String name) {
        return new CleanDirtySlurryId(key("clean_" + name), key("dirty_" + name));
    }
}