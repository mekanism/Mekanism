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
public class BasicEnrichingRecipe extends BasicItemStackToItemStackRecipe implements IBasicItemStackOutput {

    private static final Holder<Item> ENRICHMENT_CHAMBER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "enrichment_chamber"));

    public BasicEnrichingRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_ENRICHING.value());
    }

    @Override
    public RecipeSerializer<BasicEnrichingRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENRICHING.value();
    }

    @Override
    public String getGroup() {
        return "enrichment_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ENRICHMENT_CHAMBER);
    }

}