package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class BasicPaintingRecipe extends BasicItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> PAINTING_MACHINE = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "painting_machine"));

    /**
     * @param itemInput     Item input.
     * @param chemicalInput Chemical input.
     * @param output        Output.
     */
    public BasicPaintingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
        super(itemInput, chemicalInput, output, perTickUsage, MekanismRecipeTypes.TYPE_PAINTING.value());
    }

    @Override
    public String getGroup() {
        return "painting_machine";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PAINTING_MACHINE);
    }

    @Override
    public RecipeSerializer<BasicPaintingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PAINTING.get();
    }
}