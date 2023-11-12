package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BasicCentrifugingRecipe extends BasicGasToGasRecipe {

    private static final RegistryObject<Item> ISOTOPIC_CENTRIFUGE = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "isotopic_centrifuge"), ForgeRegistries.ITEMS);

    public BasicCentrifugingRecipe(GasStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_CENTRIFUGING.get());
    }

    @Override
    public RecipeType<GasToGasRecipe> getType() {
        return MekanismRecipeTypes.TYPE_CENTRIFUGING.get();
    }

    @Override
    public RecipeSerializer<BasicCentrifugingRecipe> getSerializer() {
        return MekanismRecipeSerializers.CENTRIFUGING.get();
    }

    @Override
    public String getGroup() {
        return "isotopic_centrifuge";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ISOTOPIC_CENTRIFUGE.get());
    }

}