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

    @Nullable
    @Override
    public STACK getMatch(Iterable<STACK> ingredients, @Nonnull STACK toMatch) {
        for (STACK stack : ingredients) {
            if (toMatch.isTypeEqual(stack)) {
                return stack;
            }
        }
        //JEI expects null to be returned if there is no match
        // so that it can filter hidden ingredients
        return null;
    }

    @Override
    public String getDisplayName(STACK ingredient) {
        return TextComponentUtil.build(ingredient).getFormattedText();
    }

    @Override
    public String getUniqueId(STACK ingredient) {
        return getType().toLowerCase(Locale.ROOT) + ":" + ingredient.getTypeRegistryName();
    }

    @Override
    public String getWildcardId(STACK ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(STACK ingredient) {
        return ingredient.getTypeRegistryName().getNamespace();
    }

    /*@Override
    public Iterable<Integer> getColors(STACK ingredient) {
        CHEMICAL chemical = ingredient.getType();
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(chemical.getIcon());
        int renderColor = chemical.getTint();
        //TODO: Does tint need alpha applied/factored in to getting the color
        // Either way this is waiting on https://github.com/mezz/JustEnoughItems/issues/1886
        return ColorGetter.getColors(sprite, renderColor, 1);
    }*/

    @Override
    public String getResourceId(STACK ingredient) {
        return ingredient.getTypeRegistryName().getPath();
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
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(GasStack.class);
        CHEMICAL chemical = ingredient.getType();
        toStringHelper.add(getType(), chemical.isEmptyType() ? "none" : TextComponentUtil.build(chemical).getFormattedText());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.getAmount());
        }
        return toStringHelper.toString();
    }
}