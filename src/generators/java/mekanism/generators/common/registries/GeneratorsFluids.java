package mekanism.generators.common.registries;

import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.BucketItem;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

public class GeneratorsFluids {

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(MekanismGenerators.MODID);

    public static final FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> BIOETHANOL = FLUIDS.register("bioethanol",
          fluidAttributes -> fluidAttributes.color(0xFFCEEB3D).luminosity(15));
}