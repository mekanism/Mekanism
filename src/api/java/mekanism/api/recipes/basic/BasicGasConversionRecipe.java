package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BasicGasConversionRecipe extends BasicItemStackToGasRecipe {

    private static final RegistryObject<Item> CREATIVE_CHEMICAL_TANK = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "creative_chemical_tank"), ForgeRegistries.ITEMS);

    public BasicGasConversionRecipe(ItemStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_GAS_CONVERSION.get());
    }

    @Override
    public RecipeSerializer<BasicGasConversionRecipe> getSerializer() {
        return MekanismRecipeSerializers.GAS_CONVERSION.get();
    }

    @Override
    public String getGroup() {
        return "gas_conversion";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CREATIVE_CHEMICAL_TANK.get());
    }

}