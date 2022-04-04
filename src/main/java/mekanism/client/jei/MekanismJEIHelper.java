package mekanism.client.jei;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mezz.jei.api.ingredients.IIngredientHelper;

public class MekanismJEIHelper implements IMekanismJEIHelper {

    public static final MekanismJEIHelper INSTANCE = new MekanismJEIHelper();

    private MekanismJEIHelper() {
    }

    @Override
    public IIngredientHelper<GasStack> getGasStackHelper() {
        return MekanismJEI.GAS_STACK_HELPER;
    }

    @Override
    public IIngredientHelper<InfusionStack> getInfusionStackHelper() {
        return MekanismJEI.INFUSION_STACK_HELPER;
    }

    @Override
    public IIngredientHelper<PigmentStack> getPigmentStackHelper() {
        return MekanismJEI.PIGMENT_STACK_HELPER;
    }

    @Override
    public IIngredientHelper<SlurryStack> getSlurryStackHelper() {
        return MekanismJEI.SLURRY_STACK_HELPER;
    }
}