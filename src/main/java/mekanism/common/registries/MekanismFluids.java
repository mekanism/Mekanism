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

    private MekanismFluids() {
    }

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(Mekanism.MODID);

    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROGEN = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROGEN);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> OXYGEN = FLUIDS.registerLiquidChemical(ChemicalConstants.OXYGEN);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> CHLORINE = FLUIDS.registerLiquidChemical(ChemicalConstants.CHLORINE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFUR_DIOXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFUR_DIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFUR_TRIOXIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SULFURIC_ACID = FLUIDS.registerLiquidChemical(ChemicalConstants.SULFURIC_ACID);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROGEN_CHLORIDE = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HYDROFLUORIC_ACID = FLUIDS.registerLiquidChemical(ChemicalConstants.HYDROFLUORIC_ACID);
    //Internal gases
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> ETHENE = FLUIDS.registerLiquidChemical(ChemicalConstants.ETHENE);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> SODIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.SODIUM);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> BRINE = FLUIDS.register("brine", fluidAttributes -> fluidAttributes.color(0xFFFEEF9C));
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> LITHIUM = FLUIDS.registerLiquidChemical(ChemicalConstants.LITHIUM);

    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> STEAM = FLUIDS.register("steam",
          FluidAttributes.builder(Mekanism.rl("liquid/steam"), Mekanism.rl("liquid/steam_flow")).gaseous().temperature(373));
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> HEAVY_WATER = FLUIDS.register("heavy_water",
          FluidAttributes.builder(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow")).color(0xFF0D1455));
}