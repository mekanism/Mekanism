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
public class BasicCrushingRecipe extends BasicItemStackToItemStackRecipe {

    private static final RegistryObject<Item> CRUSHER = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "crusher"), ForgeRegistries.ITEMS);

    public BasicCrushingRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_CRUSHING.get());
    }

    @Override
    public RecipeSerializer<BasicCrushingRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRUSHING.get();
    }

    @Override
    public String getGroup() {
        return "crusher";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CRUSHER.get());
    }

}