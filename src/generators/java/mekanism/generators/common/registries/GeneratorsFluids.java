package mekanism.generators.common.registries;

import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.generators.common.GeneratorsChemicalConstants;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Source;

public class GeneratorsFluids {

    private GeneratorsFluids() {
    }

    public static final FluidDeferredRegister FLUIDS = new FluidDeferredRegister(MekanismGenerators.MODID);

    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> BIOETHANOL = FLUIDS.register("bioethanol",
          properties -> properties.lightLevel(15), renderProperties -> renderProperties.tint(0xFFCEEB3D));
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> DEUTERIUM = FLUIDS.registerLiquidChemical(GeneratorsChemicalConstants.DEUTERIUM);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> FUSION_FUEL = registerLiquidGas("fusion_fuel", 0xFF7E007D);
    public static final FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> TRITIUM = registerLiquidGas("tritium", 0xFF64FF70);

    private static FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> registerLiquidGas(String name, int tint) {
        return FLUIDS.register(name, renderProperties -> renderProperties.tint(tint));
    }
}