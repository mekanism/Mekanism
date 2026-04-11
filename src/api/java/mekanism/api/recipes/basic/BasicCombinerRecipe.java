package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.ItemStackTemplateHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class BasicCombinerRecipe extends CombinerRecipe {

    protected final ItemStackIngredient mainInput;
    protected final ItemStackIngredient extraInput;
    protected final ItemStackTemplate output;

    /**
     * @param mainInput  Main input.
     * @param extraInput Secondary/extra input.
     * @param output     Output.
     */
    public BasicCombinerRecipe(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStackTemplate output) {
        this.mainInput = Objects.requireNonNull(mainInput, "Main input cannot be null.");
        this.extraInput = Objects.requireNonNull(extraInput, "Secondary/Extra input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        this.output = output;
    }

    @Override
    public boolean test(ItemStack input, ItemStack extra) {
        return mainInput.test(input) && extraInput.test(extra);
    }

    @Override
    public ItemStackIngredient getMainInput() {
        return mainInput;
    }

    @Override
    public ItemStackIngredient getExtraInput() {
        return extraInput;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStackTemplate getOutput(@NotNull ItemStack input, @NotNull ItemStack extra) {
        return output;
    }
    @Override
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output.create());
    }

    public ItemStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicCombinerRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMBINING.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicCombinerRecipe other = (BasicCombinerRecipe) o;
        return mainInput.equals(other.mainInput) && extraInput.equals(other.extraInput) && ItemStackTemplateHelper.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int hash = mainInput.hashCode();
        hash = 31 * hash + extraInput.hashCode();
        hash = 31 * hash + ItemStackTemplateHelper.hashItemAndComponents(output);
        hash = 31 * hash + output.count();
        return hash;
    }
}