package mekanism.common.recipe.ingredients;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.OreDictCache;
import net.minecraft.item.ItemStack;

public class OredictMekIngredient implements IMekanismIngredient<ItemStack> {

    private final String oreDict;

    public OredictMekIngredient(@Nonnull String oreDict) {
        this.oreDict = oreDict;
    }

    @Nonnull
    @Override
    public List<ItemStack> getMatching() {
        return OreDictCache.getOreDictStacks(oreDict, false);
    }

    @Override
    public boolean contains(@Nonnull ItemStack stack) {
        return OreDictCache.getOreDictName(stack).contains(oreDict);
    }

    @Override
    public int hashCode() {
        return oreDict.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OredictMekIngredient && oreDict.equals(((OredictMekIngredient) obj).oreDict);
    }
}