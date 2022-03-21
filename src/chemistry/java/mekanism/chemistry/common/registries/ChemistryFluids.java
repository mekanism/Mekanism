package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;

public class ChemistryFluids {
    private ChemistryFluids() {
    }

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(MekanismChemistry.MODID);

    private static FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> registerLiquidGas(String name, int tint) {
        return FLUIDS.register(name, fluidAttributes -> fluidAttributes.color(tint));
    }
}
