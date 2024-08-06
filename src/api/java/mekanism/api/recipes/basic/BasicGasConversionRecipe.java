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
public class BasicGasConversionRecipe extends BasicItemStackToGasRecipe {

    private static final Holder<Item> CREATIVE_CHEMICAL_TANK = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "creative_chemical_tank"));

    public BasicGasConversionRecipe(ItemStackIngredient input, ChemicalStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_GAS_CONVERSION.value());
    }

    @Override
    public RecipeSerializer<BasicGasConversionRecipe> getSerializer() {
        return MekanismRecipeSerializers.GAS_CONVERSION.value();
    }

    @Override
    public String getGroup() {
        return "gas_conversion";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CREATIVE_CHEMICAL_TANK);
    }

}