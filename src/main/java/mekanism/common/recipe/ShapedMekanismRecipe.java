package mekanism.common.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.RecipeUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
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

    public static ShapedMekanismRecipe create(NBTTagCompound nbtTags) {
        if (!nbtTags.hasKey("result") || !nbtTags.hasKey("input")) {
            Mekanism.logger
                  .error(Mekanism.LOG_TAG + " Shaped recipe parse error: missing input or result compound tag.");
            return null;
        }

        ItemStack result = new ItemStack(nbtTags.getCompoundTag("result"));
        NBTTagList list = nbtTags.getTagList("input", Constants.NBT.TAG_COMPOUND);

        if (result.isEmpty() || list.tagCount() == 0) {
            Mekanism.logger
                  .error(Mekanism.LOG_TAG + " Shaped recipe parse error: invalid result stack or input data list.");
            return null;
        }

        Object[] ret = new Object[list.tagCount()];

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);

            if (compound.hasKey("oredict")) {
                ret[i] = compound.getString("oredict");
            } else if (compound.hasKey("pattern")) {
                ret[i] = compound.getString("pattern");
            } else if (compound.hasKey("character")) {
                String s = compound.getString("character");

                if (s.length() > 1) {
                    Mekanism.logger
                          .error(Mekanism.LOG_TAG + " Shaped recipe parse error: invalid pattern character data.");
                    return null;
                }

                ret[i] = compound.getString("character").toCharArray()[0];
            } else if (compound.hasKey("itemstack")) {
                ret[i] = new ItemStack(compound.getCompoundTag("itemstack"));
            } else {
                Mekanism.logger.error(Mekanism.LOG_TAG + " Shaped recipe parse error: invalid input tag data key.");
                return null;
            }
        }

        return new ShapedMekanismRecipe(null, result, ret); //TODO Find out correct value for group
    }

    // Copy of net.minecraftforge.oredict.ShapedOreRecipe
    public static ShapedOreRecipe factory(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");
        //if (!group.isEmpty() && group.indexOf(':') == -1)
        //    group = context.getModId() + ":" + group;

        Map<Character, Ingredient> ingMap = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException(
                      "Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            ingMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
        }

        ingMap.put(' ', Ingredient.EMPTY);

        JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");

        if (patternJ.size() == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }

        String[] pattern = new String[patternJ.size()];
        for (int x = 0; x < pattern.length; ++x) {
            String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
            if (x > 0 && pattern[0].length() != line.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
            }
            pattern[x] = line;
        }

        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.width = pattern[0].length();
        primer.height = pattern.length;
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);

        Set<Character> keys = Sets.newHashSet(ingMap.keySet());
        keys.remove(' ');

        int x = 0;
        for (String line : pattern) {
            for (char chr : line.toCharArray()) {
                Ingredient ing = ingMap.get(chr);
                if (ing == null) {
                    throw new JsonSyntaxException(
                          "Pattern references symbol '" + chr + "' but it's not defined in the key");
                }
                primer.input.set(x++, ing);
                keys.remove(chr);
            }
        }

        if (!keys.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
        }

        ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
        return new ShapedMekanismRecipe(group.isEmpty() ? null : new ResourceLocation(group), result, primer);
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
            return ShapedMekanismRecipe.factory(context, json);
        }
    }
}
