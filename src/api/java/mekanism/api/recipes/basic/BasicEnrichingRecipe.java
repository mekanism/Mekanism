package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
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
public class BasicEnrichingRecipe extends BasicItemStackToItemStackRecipe implements IBasicItemStackOutput {

    private static final RegistryObject<Item> ENRICHMENT_CHAMBER = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "enrichment_chamber"), ForgeRegistries.ITEMS);

    public BasicEnrichingRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_ENRICHING.get());
    }

    @Override
    public RecipeSerializer<BasicEnrichingRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENRICHING.get();
    }

    @Override
    public String getGroup() {
        return "enrichment_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ENRICHMENT_CHAMBER.get());
    }

}