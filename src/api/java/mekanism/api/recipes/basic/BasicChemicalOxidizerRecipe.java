package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class BasicChemicalOxidizerRecipe extends BasicItemStackToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_OXIDIZER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_oxidizer"));

    public BasicChemicalOxidizerRecipe(ItemStackIngredient input, ChemicalStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_OXIDIZING.value());
    }

    @Override
    public RecipeSerializer<BasicChemicalOxidizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.OXIDIZING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_oxidizer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_OXIDIZER);
    }

}