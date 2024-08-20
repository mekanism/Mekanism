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
public class BasicInjectingRecipe extends BasicItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> CHEMICAL_INJECTION_CHAMBER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_injection_chamber"));

    public BasicInjectingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage) {
        super(itemInput, chemicalInput, output, perTickUsage, MekanismRecipeTypes.TYPE_INJECTING.value());
    }

    @Override
    public RecipeSerializer<BasicInjectingRecipe> getSerializer() {
        return MekanismRecipeSerializers.INJECTING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_injection_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_INJECTION_CHAMBER);
    }

}