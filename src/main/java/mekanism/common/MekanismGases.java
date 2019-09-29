package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.Slurry;
import mekanism.api.providers.IGasProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
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
    LIQUID_OSMIUM("liquid_osmium", 0x52BDCA),

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

    //TODO: Fix, and move to a MekanismFluids class?
    // Given I am thinking it may make sense to "separate" the direct pairing of gas and fluids
    public static final Fluid HEAVY_WATER = makeHeavyWater();

    private static Fluid makeHeavyWater() {
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(() -> HEAVY_WATER, () -> HEAVY_WATER,
              FluidAttributes.builder(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heavy_water"),
                    new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heavy_water")));
        ForgeFlowingFluid flowingFluid = new ForgeFlowingFluid.Source(properties);
        flowingFluid.setRegistryName(new ResourceLocation(Mekanism.MODID, "heavy_water"));
        return flowingFluid;
    }

    private final Gas gas;

    MekanismGases(String name, int color) {
        this(name, color, false);
    }

    MekanismGases(String name, int color, boolean hasFluid) {
        gas = new Gas(new ResourceLocation(Mekanism.MODID, name), color);
        if (hasFluid) {
            gas.createFluid();
        }
    }

    MekanismGases(String name, ResourceLocation texture, boolean hasFluid) {
        gas = new Gas(new ResourceLocation(Mekanism.MODID, name), texture);
        if (hasFluid) {
            gas.createFluid();
        }
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

    public static void registerFluids(IForgeRegistry<Fluid> registry) {
        for (IGasProvider gasProvider : values()) {
            Fluid fluid = gasProvider.getFluid();
            if (fluid != Fluids.EMPTY) {
                registry.register(fluid);
            }
        }

        //TODO: Fix
        registry.register(HEAVY_WATER);
        //TODO: Buckets
        /*FluidRegistry.enableUniversalBucket();
        FluidRegistry.addBucketForFluid(HEAVY_WATER);
        FluidRegistry.addBucketForFluid(BRINE.getFluid());
        FluidRegistry.addBucketForFluid(LITHIUM.getFluid());*/
    }
}