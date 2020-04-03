package mekanism.common.registries;

import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.BucketItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

public class MekanismFluids {

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(Mekanism.MODID);

    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROGEN = registerLiquidChemical(ChemicalConstants.HYDROGEN);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> OXYGEN = registerLiquidChemical(ChemicalConstants.OXYGEN);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> CHLORINE = registerLiquidChemical(ChemicalConstants.CHLORINE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFUR_DIOXIDE = registerLiquidChemical(ChemicalConstants.SULFUR_DIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFUR_TRIOXIDE = registerLiquidChemical(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFURIC_ACID = registerLiquidChemical(ChemicalConstants.SULFURIC_ACID);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROGEN_CHLORIDE = registerLiquidChemical(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROFLUORIC_ACID = registerLiquidChemical(ChemicalConstants.HYDROFLUORIC_ACID);
    //Internal gases
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> ETHENE = registerLiquidChemical(ChemicalConstants.ETHENE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SODIUM = registerLiquidChemical(ChemicalConstants.SODIUM);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> BRINE = FLUIDS.register("brine",
          fluidAttributes -> fluidAttributes.gaseous().color(0xFFFEEF9C));
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> LITHIUM = registerLiquidChemical(ChemicalConstants.LITHIUM);

    //TODO: Why do we have a liquid steam anyways really
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> STEAM = FLUIDS.register("steam",
          FluidAttributes.builder(Mekanism.rl("block/liquid/liquid_steam"), Mekanism.rl("block/liquid/liquid_steam_flow")).gaseous().temperature(373));
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HEAVY_WATER = FLUIDS.register("heavy_water",
          FluidAttributes.builder(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow")).color(0xFF0D1455));

    private static FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> registerLiquidChemical(ChemicalConstants constants) {
        int color = constants.getColor();
        int temperature = Math.round(constants.getTemperature());
        int density = Math.round(constants.getDensity());
        //TODO: Support for luminosity?
        return FLUIDS.register(constants.getName(), fluidAttributes -> fluidAttributes.color(color).temperature(temperature).density(density).viscosity(density));
    }
}