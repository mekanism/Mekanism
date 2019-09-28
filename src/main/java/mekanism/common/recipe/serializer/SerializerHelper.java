package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class SerializerHelper {

    public static ItemStack getItemStack(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        if (!json.get(key).isJsonObject()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be an object");
        }
        return ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, key));
    }

    public static GasStack getGasStack(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        if (!json.get(key).isJsonObject()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be an object");
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
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        if (!json.get(key).isJsonObject()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be an object");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, "fluid"));
        //TODO: Amount, and fluid NBT??
        return FluidStack.EMPTY;
    }
}