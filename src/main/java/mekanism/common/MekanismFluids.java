package mekanism.common;

import java.util.Locale;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.OreGas;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class MekanismFluids {

    public static final Gas HYDROGEN = new Gas("hydrogen", 0xFFFFFF);
    public static final Gas OXYGEN = new Gas("oxygen", 0x6CE2FF);
    public static final Gas STEAM = new Gas("steam", "mekanism:block/liquid/liquid_steam");
    public static final Gas CHLORINE = new Gas("chlorine", 0xCFE800);
    public static final Gas SULFUR_DIOXIDE = new Gas("sulfur_dioxide", 0xA99D90);
    public static final Gas SULFUR_TRIOXIDE = new Gas("sulfur_trioxide", 0xCE6C6C);
    public static final Gas SULFURIC_ACID = new Gas("sulfuric_acid", 0x82802B);
    public static final Gas HYDROGEN_CHLORIDE = new Gas("hydrogen_chloride", 0xA8F1E9);

    //TODO: Fix
    public static final Fluid HEAVY_WATER = Fluids.WATER;//new Fluid("heavy_water", new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heavy_water"), new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heavy_water"));

    //Internal gases
    //TODO: Rename liquid osmium?
    public static final Gas LIQUID_OSMIUM = new Gas("liquid_osmium", 0x52bdca);
    public static final Gas ETHENE = new Gas("ethene", 0xEACCF9);
    public static final Gas SODIUM = new Gas("sodium", 0xE9FEF4);
    public static final Gas BRINE = new Gas("brine", 0xFEEF9C);
    public static final Gas DEUTERIUM = new Gas("deuterium", 0xFF3232);
    public static final Gas TRITIUM = new Gas("tritium", 0x64FF70);
    public static final Gas FUSION_FUEL = new Gas("fusion_fuel", 0x7E007D);
    public static final Gas LITHIUM = new Gas("lithium", 0xEBA400);

    public static void register() {
        GasRegistry.register(HYDROGEN).registerFluid();
        GasRegistry.register(OXYGEN).registerFluid();
        GasRegistry.register(STEAM).registerFluid();
        GasRegistry.register(CHLORINE).registerFluid();
        GasRegistry.register(SULFUR_DIOXIDE).registerFluid();
        GasRegistry.register(SULFUR_TRIOXIDE).registerFluid();
        GasRegistry.register(SULFURIC_ACID).registerFluid();
        GasRegistry.register(HYDROGEN_CHLORIDE).registerFluid();
        GasRegistry.register(ETHENE).registerFluid();
        GasRegistry.register(SODIUM).registerFluid();
        GasRegistry.register(BRINE).registerFluid();
        GasRegistry.register(DEUTERIUM).registerFluid();
        GasRegistry.register(TRITIUM).registerFluid();
        GasRegistry.register(FUSION_FUEL).registerFluid();
        GasRegistry.register(LITHIUM).registerFluid();

        GasRegistry.register(LIQUID_OSMIUM).setVisible(false);

        //TODO: Fix
        //ForgeRegistries.FLUIDS.register(HEAVY_WATER);

        for (Resource resource : Resource.values()) {
            String name = resource.getName();
            String nameLower = name.toLowerCase(Locale.ROOT);
            //Clean
            OreGas clean = new OreGas("clean" + name, "oregas." + nameLower, resource.tint);
            GasRegistry.register(clean);
            //Dirty
            GasRegistry.register(new OreGas(nameLower, "oregas." + nameLower, resource.tint, clean));
        }

        //TODO: Buckets
        /*FluidRegistry.enableUniversalBucket();
        FluidRegistry.addBucketForFluid(HEAVY_WATER);
        FluidRegistry.addBucketForFluid(BRINE.getFluid());
        FluidRegistry.addBucketForFluid(LITHIUM.getFluid());*/
    }
}