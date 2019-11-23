package mekanism.common;

import java.util.function.Function;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.BucketItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

//TODO: Fix the bucket models once the model PR gets merged as it will be simpler to deal with then
//TODO: Things only used in MekanismGenerators should be moved to that mod such as fusion fuel
public class MekanismFluids {

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(Mekanism.MODID);

    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROGEN = registerLiquidChemical(ChemicalAttributes.HYDROGEN);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> OXYGEN = registerLiquidChemical(ChemicalAttributes.OXYGEN);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> CHLORINE = registerLiquidChemical(ChemicalAttributes.CHLORINE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFUR_DIOXIDE = registerLiquidChemical(ChemicalAttributes.SULFUR_DIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFUR_TRIOXIDE = registerLiquidChemical(ChemicalAttributes.SULFUR_TRIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFURIC_ACID = registerLiquidChemical(ChemicalAttributes.SULFURIC_ACID);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROGEN_CHLORIDE = registerLiquidChemical(ChemicalAttributes.HYDROGEN_CHLORIDE);
    //Internal gases
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> ETHENE = registerLiquidChemical(ChemicalAttributes.ETHENE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SODIUM = registerLiquidChemical(ChemicalAttributes.SODIUM);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> BRINE = registerLiquidGas("brine", 0xFFFEEF9C);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> DEUTERIUM = registerLiquidChemical(ChemicalAttributes.DEUTERIUM);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> TRITIUM = registerLiquidGas("tritium", 0xFF64FF70);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> FUSION_FUEL = registerLiquidGas("fusion_fuel", 0xFF7E007D);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> LITHIUM = registerLiquidChemical(ChemicalAttributes.LITHIUM);

    //TODO: Why do we have a liquid steam anyways really
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> STEAM = FLUIDS.register("steam",
          FluidAttributes.builder(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_steam"),
                new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_steam_flow")).gaseous());
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HEAVY_WATER = FLUIDS.register("heavy_water",
          FluidAttributes.builder(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow")).color(0xFF0D1455));

    private static FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> registerLiquidGas(String name, int tint) {
        return registerLiquidChemical(name, fluidAttributes -> fluidAttributes.gaseous().color(tint));
    }

    private static FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> registerLiquidChemical(ChemicalAttributes attributes) {
        int color = attributes.getColor();
        int temperature = Math.round(attributes.getTemperature());
        int density = Math.round(attributes.getDensity());
        //TODO: Support for luminosity?
        return registerLiquidChemical(attributes.getName(), fluidAttributes -> fluidAttributes.color(color).temperature(temperature).density(density).viscosity(density));
    }

    private static FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> registerLiquidChemical(String name,
          Function<FluidAttributes.Builder, FluidAttributes.Builder> fluidAttributes) {
        return FLUIDS.register(name, fluidAttributes.apply(FluidAttributes.builder(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid"),
              new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_flow"))));
    }
}