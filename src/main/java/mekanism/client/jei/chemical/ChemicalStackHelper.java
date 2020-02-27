package mekanism.client.jei.chemical;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.util.text.TextComponentUtil;
import mezz.jei.api.ingredients.IIngredientHelper;

public abstract class ChemicalStackHelper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IIngredientHelper<STACK> {

    protected abstract STACK getEmptyStack();

    protected abstract String getType();

    @Nonnull
    @Override
    public STACK getMatch(Iterable<STACK> ingredients, @Nonnull STACK toMatch) {
        for (STACK stack : ingredients) {
            if (toMatch.isTypeEqual(stack)) {
                return stack;
            }
        }
        return getEmptyStack();
    }

    @Override
    public String getDisplayName(STACK ingredient) {
        return TextComponentUtil.build(ingredient).getFormattedText();
    }

    @Override
    public String getUniqueId(STACK ingredient) {
        return getType().toLowerCase(Locale.ROOT) + ":" + ingredient.getType().getRegistryName().getPath();
    }

    @Override
    public String getWildcardId(STACK ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(STACK ingredient) {
        return ingredient.getType().getIcon().getNamespace();
    }

    @Override
    public String getResourceId(STACK ingredient) {
        return ingredient.getType().getTranslationKey();
    }

    @Override
    public STACK copyIngredient(STACK ingredient) {
        return (STACK) ingredient.copy();
    }

    @Override
    public String getErrorInfo(@Nullable STACK ingredient) {
        if (ingredient == null) {
            ingredient = getEmptyStack();
        }
        //TODO: Do this without using toStringHelper
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(GasStack.class);
        CHEMICAL chemical = ingredient.getType();
        toStringHelper.add(getType(), chemical.isEmptyType() ? "none" : TextComponentUtil.build(chemical).getFormattedText());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.getAmount());
        }
        return toStringHelper.toString();
    }
}