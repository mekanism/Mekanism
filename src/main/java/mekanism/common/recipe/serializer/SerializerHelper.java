package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

//TODO: Move this to API? Would let the fluid and gas ingredients use it
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
        return deserializeGas(JSONUtils.getJsonObject(json, key));
    }

    public static FluidStack getFluidStack(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        if (!json.get(key).isJsonObject()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be an object");
        }
        return deserializeFluid(JSONUtils.getJsonObject(json, key));

    }

    public static GasStack deserializeGas(@Nonnull JsonObject json) {
        if (!json.has("amount")) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get("amount");
        if (!JSONUtils.isNumber(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, "gas"));
        Gas gas = Gas.getFromRegistry(resourceLocation);
        if (gas.isEmptyType()) {
            throw new JsonSyntaxException("Invalid gas type '" + resourceLocation + "'");
        }
        return new GasStack(gas, amount);
    }

    public static FluidStack deserializeFluid(@Nonnull JsonObject json) {
        if (!json.has("amount")) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get("amount");
        if (!JSONUtils.isNumber(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("Invalid fluid type '" + resourceLocation + "'");
        }
        //TODO: Add support for reading NBT of fluid
        CompoundNBT nbt = null;
        return new FluidStack(fluid, amount, nbt);
    }
}