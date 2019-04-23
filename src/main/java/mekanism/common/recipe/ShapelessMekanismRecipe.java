package mekanism.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.RecipeUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
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

    public static ShapelessMekanismRecipe create(NBTTagCompound nbtTags) {
        if (!nbtTags.hasKey("result") || !nbtTags.hasKey("input")) {
            Mekanism.logger
                  .error(Mekanism.LOG_TAG + " Shapeless recipe parse error: missing input or result compound tag.");
            return null;
        }

        ItemStack result = new ItemStack(nbtTags.getCompoundTag("result"));
        NBTTagList list = nbtTags.getTagList("input", Constants.NBT.TAG_COMPOUND);

        if (result.isEmpty() || list.tagCount() == 0) {
            Mekanism.logger
                  .error(Mekanism.LOG_TAG + " Shapeless recipe parse error: invalid result stack or input data list.");
            return null;
        }

        Object[] ret = new Object[list.tagCount()];

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);

            if (compound.hasKey("oredict")) {
                ret[i] = compound.getString("oredict");
            } else if (compound.hasKey("itemstack")) {
                ret[i] = new ItemStack(compound.getCompoundTag("itemstack"));
            } else {
                Mekanism.logger.error(Mekanism.LOG_TAG + " Shapeless recipe parse error: invalid input tag data key.");
                return null;
            }
        }

        return new ShapelessMekanismRecipe(null, result, ret); //TODO Find out correct value for group
    }

    // Copy of net.minecraftforge.oredict.ShapelessOreRecipe
    public static ShapelessMekanismRecipe factory(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");

        NonNullList<Ingredient> ings = NonNullList.create();
        for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients")) {
            ings.add(CraftingHelper.getIngredient(ele, context));
        }

        if (ings.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }

        ItemStack itemstack = ShapedRecipes.deserializeItem(JsonUtils.getJsonObject(json, "result"), true);
        return new ShapelessMekanismRecipe(group.isEmpty() ? null : new ResourceLocation(group), ings, itemstack);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
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
