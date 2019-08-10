package mekanism.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.RecipeUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ShapelessMekanismRecipe extends ShapelessOreRecipe {

    public ShapelessMekanismRecipe(ResourceLocation group, Block result, Object... recipe) {
        this(group, new ItemStack(result), recipe);
    }

    public ShapelessMekanismRecipe(ResourceLocation group, Item result, Object... recipe) {
        this(group, new ItemStack(result), recipe);
    }

    public ShapelessMekanismRecipe(ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result) {
        super(group, input, result);
    }

    public ShapelessMekanismRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe) {
        super(group, result, recipe);
    }

    public static ShapelessMekanismRecipe create(CompoundNBT nbtTags) {
        if (!nbtTags.contains("result") || !nbtTags.contains("input")) {
            Mekanism.logger.error(Mekanism.LOG_TAG + " Shapeless recipe parse error: missing input or result compound tag.");
            return null;
        }

        ItemStack result = ItemStack.read(nbtTags.getCompound("result"));
        ListNBT list = nbtTags.getList("input", Constants.NBT.TAG_COMPOUND);
        if (result.isEmpty() || list.isEmpty()) {
            Mekanism.logger.error(Mekanism.LOG_TAG + " Shapeless recipe parse error: invalid result stack or input data list.");
            return null;
        }

        Object[] ret = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT compound = list.getCompound(i);
            if (compound.contains("oredict")) {
                ret[i] = compound.getString("oredict");
            } else if (compound.contains("itemstack")) {
                ret[i] = ItemStack.read(compound.getCompound("itemstack"));
            } else {
                Mekanism.logger.error(Mekanism.LOG_TAG + " Shapeless recipe parse error: invalid input tag data key.");
                return null;
            }
        }
        return new ShapelessMekanismRecipe(null, result, ret);
    }

    // Copy of net.minecraftforge.oredict.ShapelessOreRecipe
    public static ShapelessMekanismRecipe factory(JsonContext context, JsonObject json) {
        String group = JSONUtils.getString(json, "group", "");

        NonNullList<Ingredient> ings = NonNullList.create();
        for (JsonElement ele : JSONUtils.getJsonArray(json, "ingredients")) {
            ings.add(CraftingHelper.getIngredient(ele, context));
        }
        if (ings.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"), true);
        return new ShapelessMekanismRecipe(group.isEmpty() ? null : new ResourceLocation(group), ings, itemstack);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull CraftingInventory inv) {
        return RecipeUtils.getCraftingResult(inv, output.copy());
    }

    // Used in _factories.json
    public static class RecipeFactory implements IRecipeFactory {

        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            return ShapelessMekanismRecipe.factory(context, json);
        }
    }
}