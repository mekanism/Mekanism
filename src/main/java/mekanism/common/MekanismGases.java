package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.Slurry;
import mekanism.api.providers.IGasProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public enum MekanismGases implements IGasProvider {
    HYDROGEN(ChemicalAttributes.HYDROGEN),
    OXYGEN(ChemicalAttributes.OXYGEN),
    STEAM("steam", new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_steam")),
    CHLORINE(ChemicalAttributes.CHLORINE),
    SULFUR_DIOXIDE(ChemicalAttributes.SULFUR_DIOXIDE),
    SULFUR_TRIOXIDE(ChemicalAttributes.SULFUR_TRIOXIDE),
    SULFURIC_ACID(ChemicalAttributes.SULFURIC_ACID),
    HYDROGEN_CHLORIDE(ChemicalAttributes.HYDROGEN_CHLORIDE),
    //Internal gases
    ETHENE(ChemicalAttributes.ETHENE),
    SODIUM(ChemicalAttributes.SODIUM),
    BRINE("brine", 0xFEEF9C),
    DEUTERIUM(ChemicalAttributes.DEUTERIUM),
    TRITIUM("tritium", 0x64FF70),
    FUSION_FUEL("fusion_fuel", 0x7E007D),
    LITHIUM(ChemicalAttributes.LITHIUM),
    //TODO: Rename liquid osmium? Also make it not visible again in JEI and the like?
    LIQUID_OSMIUM("liquid_osmium", 0x52BDCA, false),

    //Clean Slurry
    CLEAN_IRON_SLURRY(Resource.IRON),
    CLEAN_GOLD_SLURRY(Resource.GOLD),
    CLEAN_OSMIUM_SLURRY(Resource.OSMIUM),
    CLEAN_COPPER_SLURRY(Resource.COPPER),
    CLEAN_TIN_SLURRY(Resource.TIN),
    //Dirty Slurry
    DIRTY_IRON_SLURRY(Resource.IRON, CLEAN_IRON_SLURRY),
    DIRTY_GOLD_SLURRY(Resource.GOLD, CLEAN_GOLD_SLURRY),
    DIRTY_OSMIUM_SLURRY(Resource.OSMIUM, CLEAN_OSMIUM_SLURRY),
    DIRTY_COPPER_SLURRY(Resource.COPPER, CLEAN_COPPER_SLURRY),
    DIRTY_TIN_SLURRY(Resource.TIN, CLEAN_TIN_SLURRY);

    private final Gas gas;

    MekanismGases(ChemicalAttributes attributes) {
        gas = new Gas(new ResourceLocation(Mekanism.MODID, attributes.getName()), attributes.getColor());
    }

    MekanismGases(String name, int color) {
        this(name, color, true);
    }

    MekanismGases(String name, int color, boolean visible) {
        gas = new Gas(new ResourceLocation(Mekanism.MODID, name), color);
        gas.setVisible(visible);
    }

    MekanismGases(String name, ResourceLocation texture) {
        gas = new Gas(new ResourceLocation(Mekanism.MODID, name), texture);
    }

    MekanismGases(Resource resource) {
        this.gas = new Slurry(new ResourceLocation(Mekanism.MODID, "clean_" + resource.getRegistrySuffix() + "_slurry"), resource.tint);
    }

    MekanismGases(Resource resource, MekanismGases clean) {
        //TODO: Do this better
        this.gas = new Slurry(new ResourceLocation(Mekanism.MODID, "dirty_" + resource.getRegistrySuffix() + "_slurry"), resource.tint, (Slurry) clean.getGas());
    }

    @Nonnull
    @Override
    public Gas getGas() {
        return gas;
    }

    public static void register(IForgeRegistry<Gas> registry) {
        for (IGasProvider gasProvider : values()) {
            registry.register(gasProvider.getGas());
        }
    }
}