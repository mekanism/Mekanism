package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.stack.EmiStack;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.integration.emi.IMekanismEmiHelper;
import net.minecraft.core.Holder;

public class MekanismEmiHelper implements IMekanismEmiHelper {

    public static final MekanismEmiHelper INSTANCE = new MekanismEmiHelper();

    private MekanismEmiHelper() {
    }

    @Override
    public EmiStack createEmiStack(Holder<Chemical> chemical, long size) {
        if (size < 1) {
            return EmiStack.EMPTY;
        }
        return new ChemicalEmiStack(chemical, size);
    }

    @Override
    public Optional<ChemicalStack> asChemicalStack(EmiStack stack) {
        if (stack instanceof ChemicalEmiStack chemicalEmiStack) {
            return Optional.of(chemicalEmiStack.getStack());
        }
        return Optional.empty();
    }
}