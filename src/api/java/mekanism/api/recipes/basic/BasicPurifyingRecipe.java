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
public class BasicPurifyingRecipe extends BasicItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> PURIFICATION_CHAMBER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "purification_chamber"));

    public BasicPurifyingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
        super(itemInput, chemicalInput, output, perTickUsage, MekanismRecipeTypes.TYPE_PURIFYING.value());
    }

    @Override
    public RecipeSerializer<BasicPurifyingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PURIFYING.value();
    }

    @Override
    public String getGroup() {
        return "purification_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PURIFICATION_CHAMBER);
    }
}