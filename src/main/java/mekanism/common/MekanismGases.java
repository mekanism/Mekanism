package mekanism.common;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GaseousFluid;
import mekanism.api.gas.OreGas;
import mekanism.api.providers.IGasProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public enum MekanismGases implements IGasProvider {
    HYDROGEN("hydrogen", 0xFFFFFF, true),
    OXYGEN("oxygen", 0x6CE2FF, true),
    STEAM("steam", new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_steam"), true),
    CHLORINE("chlorine", 0xCFE800, true),
    SULFUR_DIOXIDE("sulfur_dioxide", 0xA99D90, true),
    SULFUR_TRIOXIDE("sulfur_trioxide", 0xCE6C6C, true),
    SULFURIC_ACID("sulfuric_acid", 0x82802B, true),
    HYDROGEN_CHLORIDE("hydrogen_chloride", 0xA8F1E9, true),
    //Internal gases
    ETHENE("ethene", 0xEACCF9, true),
    SODIUM("sodium", 0xE9FEF4, true),
    BRINE("brine", 0xFEEF9C, true),
    DEUTERIUM("deuterium", 0xFF3232, true),
    TRITIUM("tritium", 0x64FF70, true),
    FUSION_FUEL("fusion_fuel", 0x7E007D, true),
    LITHIUM("lithium", 0xEBA400, true),
    //TODO: Rename liquid osmium? Also make it not visible again in JEI and the like?
    LIQUID_OSMIUM("liquid_osmium", 0x52bdca);

    //TODO: Fix
    public static final Fluid HEAVY_WATER = Fluids.WATER;//new Fluid("heavy_water", new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heavy_water"), new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heavy_water"));

    private final Gas gas;

    MekanismGases(String name, int color) {
        this(name, color, false);
    }

    MekanismGases(String name, int color, boolean hasFluid) {
        this.gas = new Gas(new ResourceLocation(Mekanism.MODID, name), color);
        gas.setFluid(new GaseousFluid(gas));
    }

    MekanismGases(String name, ResourceLocation texture) {
        this(name, texture, false);
    }

    MekanismGases(String name, ResourceLocation texture, boolean hasFluid) {
        this.gas = new Gas(new ResourceLocation(Mekanism.MODID, name), texture);
        gas.setFluid(new GaseousFluid(gas));
    }

    @Override
    public Gas getGas() {
        return gas;
    }

    public static void register(IForgeRegistry<Gas> registry) {
        //TODO: Fluids
        for (IGasProvider gasProvider : values()) {
            registry.register(gasProvider.getGas());
        }
        //TODO: Keep track of these in the enum?
        for (Resource resource : Resource.values()) {
            String suffix = resource.getRegistrySuffix();
            //Clean
            OreGas clean = new OreGas(new ResourceLocation(Mekanism.MODID, "clean_" + suffix), resource.tint);
            registry.register(clean);
            //Dirty
            registry.register(new OreGas(new ResourceLocation(Mekanism.MODID, suffix), resource.tint, clean));
        }
    }

    public static void registerFluids(IForgeRegistry<Fluid> registry) {
        for (IGasProvider gasProvider : values()) {
            Fluid fluid = gasProvider.getFluid();
            if (fluid != Fluids.EMPTY) {
                registry.register(fluid);
            }
        }

        //TODO: Fix
        //ForgeRegistries.FLUIDS.register(HEAVY_WATER);
        //TODO: Buckets
        /*FluidRegistry.enableUniversalBucket();
        FluidRegistry.addBucketForFluid(HEAVY_WATER);
        FluidRegistry.addBucketForFluid(BRINE.getFluid());
        FluidRegistry.addBucketForFluid(LITHIUM.getFluid());*/
    }
}