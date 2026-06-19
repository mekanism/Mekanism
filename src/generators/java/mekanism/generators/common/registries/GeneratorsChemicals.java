package mekanism.generators.common.registries;

import mekanism.api.chemical.Chemical;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.generators.common.GeneratorsChemicalConstants;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.resources.ResourceKey;

public class GeneratorsChemicals {

    private GeneratorsChemicals() {
    }

    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(MekanismGenerators.MODID);

    public static final ResourceKey<Chemical> DEUTERIUM = CHEMICALS.register(GeneratorsChemicalConstants.DEUTERIUM);
    public static final ResourceKey<Chemical> TRITIUM = CHEMICALS.register("tritium", 0xFF64FF70);
    public static final ResourceKey<Chemical> FUSION_FUEL = CHEMICALS.register("fusion_fuel", 0xFF7E007D);
}