package mekanism.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.RecipeUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedMekanismRecipe extends ShapedOreRecipe {

    public ShapedMekanismRecipe(ResourceLocation group, Block result, Object... recipe) {
        this(group, new ItemStack(result), recipe);
    }

    public ShapedMekanismRecipe(ResourceLocation group, Item result, Object... recipe) {
        this(group, new ItemStack(result), recipe);
    }

    public ShapedMekanismRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe) {
        this(group, result, CraftingHelper.parseShaped(recipe));
    }

    public ShapedMekanismRecipe(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer) {
        super(group, result, primer);
    }

    public static ShapedMekanismRecipe create(CompoundNBT nbtTags) {
        if (!nbtTags.contains("result") || !nbtTags.contains("input")) {
            Mekanism.logger.error(Mekanism.LOG_TAG + " Shaped recipe parse error: missing input or result compound tag.");
            return null;
        }
        ItemStack result = ItemStack.read(nbtTags.getCompound("result"));
        ListNBT list = nbtTags.getList("input", Constants.NBT.TAG_COMPOUND);
        if (result.isEmpty() || list.isEmpty()) {
            Mekanism.logger.error(Mekanism.LOG_TAG + " Shaped recipe parse error: invalid result stack or input data list.");
            return null;
        }

        Object[] ret = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT compound = list.getCompound(i);
            if (compound.contains("oredict")) {
                ret[i] = compound.getString("oredict");
            } else if (compound.contains("pattern")) {
                ret[i] = compound.getString("pattern");
            } else if (compound.contains("character")) {
                String s = compound.getString("character");
                if (s.length() > 1) {
                    Mekanism.logger.error(Mekanism.LOG_TAG + " Shaped recipe parse error: invalid pattern character data.");
                    return null;
                }
                ret[i] = compound.getString("character").toCharArray()[0];
            } else if (compound.contains("itemstack")) {
                ret[i] = ItemStack.read(compound.getCompound("itemstack"));
            } else {
                Mekanism.logger.error(Mekanism.LOG_TAG + " Shaped recipe parse error: invalid input tag data key.");
                return null;
            }
        }
        return new ShapedMekanismRecipe(null, result, ret);
    }

    // Copy of net.minecraftforge.oredict.ShapedOreRecipe
    public static ShapedOreRecipe factory(JsonContext context, JsonObject json) {
        String group = JSONUtils.getString(json, "group", "");
        //if (!group.isEmpty() && group.indexOf(':') == -1)
        //    group = context.getModId() + ":" + group;

        Map<Character, Ingredient> ingMap = new HashMap<>();
        for (Entry<String, JsonElement> entry : JSONUtils.getJsonObject(json, "key").entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }
            ingMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
        }

        ingMap.put(' ', Ingredient.EMPTY);

        JsonArray patternJ = JSONUtils.getJsonArray(json, "pattern");
        if (patternJ.size() == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }

        String[] pattern = new String[patternJ.size()];
        for (int x = 0; x < pattern.length; ++x) {
            String line = JSONUtils.getString(patternJ.get(x), "pattern[" + x + "]");
            if (x > 0 && pattern[0].length() != line.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
            }
            pattern[x] = line;
        }

        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.width = pattern[0].length();
        primer.height = pattern.length;
        primer.mirrored = JSONUtils.getBoolean(json, "mirrored", true);
        primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);

        Set<Character> keys = new HashSet<>(ingMap.keySet());
        keys.remove(' ');

        int x = 0;
        for (String line : pattern) {
            for (char chr : line.toCharArray()) {
                Ingredient ing = ingMap.get(chr);
                if (ing == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
                }
                primer.input.set(x++, ing);
                keys.remove(chr);
            }
        }

        if (!keys.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
        }
        ItemStack result = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "result"), context);
        return new ShapedMekanismRecipe(group.isEmpty() ? null : new ResourceLocation(group), result, primer);
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
            return ShapedMekanismRecipe.factory(context, json);
        }
    }
}