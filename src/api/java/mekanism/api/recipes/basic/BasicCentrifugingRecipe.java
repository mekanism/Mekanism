package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class BasicCentrifugingRecipe extends BasicChemicalToChemicalRecipe {

    private static final Holder<Item> ISOTOPIC_CENTRIFUGE = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "isotopic_centrifuge"));

    public BasicCentrifugingRecipe(ChemicalStackIngredient input, ChemicalStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_CENTRIFUGING.value());
    }

    @Override
    public RecipeSerializer<BasicCentrifugingRecipe> getSerializer() {
        return MekanismRecipeSerializers.CENTRIFUGING.value();
    }

    @Override
    public String getGroup() {
        return "isotopic_centrifuge";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ISOTOPIC_CENTRIFUGE);
    }

}