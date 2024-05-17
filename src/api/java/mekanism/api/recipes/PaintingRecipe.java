package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Input: Pigment
 * <br>
 * Output: ItemStack
 *
 * @apiNote Painting Machines can process this recipe type.
 */
@NothingNullByDefault
public abstract class PaintingRecipe extends ItemStackChemicalToItemStackRecipe<Pigment, PigmentStack, PigmentStackIngredient> {

    private static final Holder<Item> PAINTING_MACHINE = DeferredHolder.create(Registries.ITEM, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "painting_machine"));

    @Override
    public abstract ItemStackIngredient getItemInput();

    @Override
    public abstract PigmentStackIngredient getChemicalInput();

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ItemStack getOutput(ItemStack inputItem, PigmentStack inputChemical);

    @NotNull
    @Override
    public abstract ItemStack getResultItem(@NotNull HolderLookup.Provider provider);

    @Override
    public abstract boolean test(ItemStack itemStack, PigmentStack gasStack);

    @Override
    public abstract List<@NotNull ItemStack> getOutputDefinition();

    @Override
    public final RecipeType<PaintingRecipe> getType() {
        return MekanismRecipeTypes.TYPE_PAINTING.value();
    }

    @Override
    public String getGroup() {
        return "painting_machine";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PAINTING_MACHINE);
    }
}
