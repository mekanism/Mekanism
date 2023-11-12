package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BasicCompressingRecipe extends BasicItemStackGasToItemStackRecipe implements IBasicItemStackOutput {

    private static final RegistryObject<Item> OSMIUM_COMPRESSOR = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "osmium_compressor"), ForgeRegistries.ITEMS);

    public BasicCompressingRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(itemInput, gasInput, output, MekanismRecipeTypes.TYPE_COMPRESSING.get());
    }

    @Override
    public RecipeSerializer<BasicCompressingRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMPRESSING.get();
    }

    @Override
    public String getGroup() {
        return "osmium_compressor";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(OSMIUM_COMPRESSOR.get());
    }

}