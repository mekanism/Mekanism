package mekanism.generators.common.registries;

import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.generators.common.GeneratorsChemicalConstants;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.BucketItem;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

public class GeneratorsFluids {

    private GeneratorsFluids() {
    }

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(MekanismGenerators.MODID);

    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> BIOETHANOL = FLUIDS.register("bioethanol",
          fluidAttributes -> fluidAttributes.color(0xFFCEEB3D).luminosity(15));
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> DEUTERIUM = FLUIDS.registerLiquidChemical(GeneratorsChemicalConstants.DEUTERIUM);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> FUSION_FUEL = registerLiquidGas("fusion_fuel", 0xFF7E007D);
    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> TRITIUM = registerLiquidGas("tritium", 0xFF64FF70);

    private static FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> registerLiquidGas(String name, int tint) {
        return FLUIDS.register(name, fluidAttributes -> fluidAttributes.color(tint));
    }
}