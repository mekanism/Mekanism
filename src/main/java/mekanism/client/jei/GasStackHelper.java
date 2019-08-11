package mekanism.client.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.util.text.TextComponentUtil;
import mezz.jei.api.ingredients.IIngredientHelper;

public class GasStackHelper implements IIngredientHelper<GasStack> {

    @Override
    @Nullable
    public GasStack getMatch(Iterable<GasStack> ingredients, GasStack toMatch) {
        for (GasStack stack : ingredients) {
            if (toMatch.getGas() == stack.getGas()) {
                return stack;
            }
        }
        return null;
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
        //TODO: Do this without using toStringHelper
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(GasStack.class);
        Gas gas = ingredient == null ? null : ingredient.getGas();
        toStringHelper.add("Gas", gas != null ? TextComponentUtil.build(gas).getFormattedText() : "null");
        if (ingredient != null) {
            toStringHelper.add("Amount", ingredient.amount);
        }
        return toStringHelper.toString();
    }
}