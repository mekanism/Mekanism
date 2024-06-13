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
public class BasicSmeltingRecipe extends BasicItemStackToItemStackRecipe implements IBasicItemStackOutput {

    private static final Holder<Item> ENERGIZED_SMELTER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "energized_smelter"));

    public BasicSmeltingRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_SMELTING.value());
    }

    @Override
    public RecipeSerializer<BasicSmeltingRecipe> getSerializer() {
        return MekanismRecipeSerializers.SMELTING.value();
    }

    @Override
    public String getGroup() {
        return "energized_smelter";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ENERGIZED_SMELTER);
    }

}