package mekanism.client.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.util.text.TextComponentUtil;
import mezz.jei.api.ingredients.IIngredientHelper;

public class GasStackHelper implements IIngredientHelper<GasStack> {

    @Nonnull
    @Override
    public GasStack getMatch(Iterable<GasStack> ingredients, @Nonnull GasStack toMatch) {
        for (GasStack stack : ingredients) {
            if (toMatch.isTypeEqual(stack)) {
                return stack;
            }
        }
        return GasStack.EMPTY;
    }

    @Override
    public String getDisplayName(GasStack ingredient) {
        return TextComponentUtil.build(ingredient).getFormattedText();
    }

    @Override
    public String getUniqueId(GasStack ingredient) {
        return "gas:" + ingredient.getGas().getName();
    }

    @Override
    public String getWildcardId(GasStack ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(GasStack ingredient) {
        return ingredient.getGas().getIcon().getNamespace();
    }

    @Override
    public String getResourceId(GasStack ingredient) {
        return ingredient.getGas().getTranslationKey();
    }

    @Override
    public GasStack copyIngredient(GasStack ingredient) {
        return ingredient.copy();
    }

    @Override
    public String getErrorInfo(@Nullable GasStack ingredient) {
        if (ingredient == null) {
            ingredient = GasStack.EMPTY;
        }
        //TODO: Do this without using toStringHelper
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(GasStack.class);
        Gas gas = ingredient.getGas();
        toStringHelper.add("Gas", gas == MekanismAPI.EMPTY_GAS ? "none" : TextComponentUtil.build(gas).getFormattedText());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.getAmount());
        }
        return toStringHelper.toString();
    }
}