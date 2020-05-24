package mekanism.client.jei.chemical;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;

public class SlurryStackHelper extends ChemicalStackHelper<Slurry, SlurryStack> {

    @Override
    protected SlurryStack getEmptyStack() {
        return SlurryStack.EMPTY;
    }

    @Override
    protected String getType() {
        return "Slurry";
    }
}