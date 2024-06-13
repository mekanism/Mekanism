package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
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
public class BasicCrushingRecipe extends BasicItemStackToItemStackRecipe {

    private static final Holder<Item> CRUSHER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "crusher"));

    public BasicCrushingRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_CRUSHING.value());
    }

    @Override
    public RecipeSerializer<BasicCrushingRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRUSHING.value();
    }

    @Override
    public String getGroup() {
        return "crusher";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CRUSHER);
    }

}