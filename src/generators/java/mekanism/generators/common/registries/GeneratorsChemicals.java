package mekanism.generators.common.registries;

import mekanism.api.chemical.Chemical;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.registration.DatapackDeferredRegister;
import mekanism.generators.common.GeneratorsChemicalConstants;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.resources.ResourceKey;

public class GeneratorsChemicals {

    private GeneratorsChemicals() {
    }

    public static final DatapackDeferredRegister<Chemical> CHEMICALS = DatapackDeferredRegister.chemicals(MekanismGenerators.MODID);

    public static final ResourceKey<Chemical> DEUTERIUM = dataKey(GeneratorsChemicalConstants.DEUTERIUM);
    public static final ResourceKey<Chemical> TRITIUM = CHEMICALS.dataKey("tritium");
    public static final ResourceKey<Chemical> FUSION_FUEL = CHEMICALS.dataKey("fusion_fuel");

    private static ResourceKey<Chemical> dataKey(IChemicalConstant constant) {
        return CHEMICALS.dataKey(constant.getName());
    }
}