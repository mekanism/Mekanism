package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Output: FloatingLong
 *
 * @apiNote Energy conversion recipes can be used in any slots in Mekanism machines that are able to convert items into energy.
 */
@NothingNullByDefault
public abstract class ItemStackToEnergyRecipe extends MekanismRecipe implements Predicate<@NotNull ItemStack> {

    private static final Holder<Item> ENERGY_TABLET = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "energy_tablet"));

    @Override
    public abstract boolean test(ItemStack itemStack);

    /**
     * Gets the input ingredient.
     */
    public abstract ItemStackIngredient getInput();

    /**
     * Gets the output based on the given input.
     *
     * @param input Specific input.
     *
     * @return Output as a constant.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    public abstract FloatingLong getOutput(ItemStack input);

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<FloatingLong> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public final RecipeType<ItemStackToEnergyRecipe> getType() {
        return MekanismRecipeTypes.TYPE_ENERGY_CONVERSION.value();
    }

    @Override
    public String getGroup() {
        return "energy_conversion";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ENERGY_TABLET);
    }
}
