package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BasicActivatingRecipe extends BasicGasToGasRecipe {

    private static final RegistryObject<Item> SOLAR_NEUTRON_ACTIVATOR = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "solar_neutron_activator"), ForgeRegistries.ITEMS);

    public BasicActivatingRecipe(GasStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_ACTIVATING.get());
    }

    @Override
    public RecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeType.ACTIVATING.get();
    }

    @Override
    public RecipeSerializer<BasicActivatingRecipe> getSerializer() {
        return MekanismRecipeSerializers.ACTIVATING.get();
    }

    @Override
    public String getGroup() {
        return "solar_neutron_activator";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(SOLAR_NEUTRON_ACTIVATOR.get());
    }
}