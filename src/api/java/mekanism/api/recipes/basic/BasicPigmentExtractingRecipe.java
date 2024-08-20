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
public class BasicPigmentExtractingRecipe extends BasicItemStackToChemicalRecipe {

    private static final Holder<Item> PIGMENT_EXTRACTOR = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment_extractor"));

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicPigmentExtractingRecipe(ItemStackIngredient input, ChemicalStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_PIGMENT_EXTRACTING.value());
    }

    @Override
    public String getGroup() {
        return "pigment_extractor";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PIGMENT_EXTRACTOR);
    }

    @Override
    public RecipeSerializer<BasicPigmentExtractingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_EXTRACTING.get();
    }
}