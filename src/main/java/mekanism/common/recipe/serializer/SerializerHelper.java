package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class SerializerHelper {

    public static ItemStack getItemStack(@Nonnull JsonObject json, @Nonnull String key) {
        //Forge: Check if primitive string to keep vanilla or a object which can contain a count field.
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find a string or object");
        }
        ItemStack itemStack;
        if (json.get(key).isJsonObject()) {
            itemStack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, key));
        } else {
            //TODO: Do we even want to support the primitive string like vanilla does for items
            String result = JSONUtils.getString(json, key);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(result));
            if (item == null || item == Items.AIR) {
                throw new IllegalStateException("Item: " + result + " does not exist");
            }
            itemStack = new ItemStack(item);
        }
        return itemStack;
    }

    public static GasStack getGasStack(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find a string or object");
        }
        if (!json.get(key).isJsonObject()) {
            //TODO: Error
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, "gas"));
        Gas gas = Gas.getFromRegistry(resourceLocation);
        if (gas.isEmptyType()) {
            throw new JsonSyntaxException("Invalid gas type '" + resourceLocation + "'");
        }
        //TODO: Amount
        int amount = 1;
        return new GasStack(gas, amount);
    }

    public static FluidStack getFluidStack(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find a string or object");
        }
        if (!json.get(key).isJsonObject()) {
            //TODO: Error
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, "fluid"));
        //TODO: Amount, and fluid NBT??
        return FluidStack.EMPTY;
    }
}