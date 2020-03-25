package mekanism.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.math.FloatingLong;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SerializerHelper {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static FloatingLong getFloatingLong(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        JsonElement jsonElement = json.get(key);
        if (!jsonElement.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be a json primitive representing a FloatingLong");
        }
        try {
            return FloatingLong.parseFloatingLong(jsonElement.getAsNumber().toString());
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException("Expected '" + key + "' to be a valid FloatingLong (positive decimal number)");
        }
    }

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

    public static InfusionStack getInfusionStack(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        if (!json.get(key).isJsonObject()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be an object");
        }
        return deserializeInfuseType(JSONUtils.getJsonObject(json, key));
    }

    public static GasStack deserializeGas(@Nonnull JsonObject json) {
        if (!json.has(JsonConstants.AMOUNT)) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get(JsonConstants.AMOUNT);
        if (!JSONUtils.isNumber(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, JsonConstants.GAS));
        Gas gas = Gas.getFromRegistry(resourceLocation);
        if (gas.isEmptyType()) {
            throw new JsonSyntaxException("Invalid gas type '" + resourceLocation + "'");
        }
        return new GasStack(gas, amount);
    }

    public static FluidStack deserializeFluid(@Nonnull JsonObject json) {
        if (!json.has(JsonConstants.AMOUNT)) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get(JsonConstants.AMOUNT);
        if (!JSONUtils.isNumber(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, JsonConstants.FLUID));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("Invalid fluid type '" + resourceLocation + "'");
        }
        CompoundNBT nbt = null;
        if (json.has(JsonConstants.NBT)) {
            JsonElement jsonNBT = json.get(JsonConstants.NBT);
            try {
                if (jsonNBT.isJsonObject()) {
                    nbt = JsonToNBT.getTagFromJson(GSON.toJson(jsonNBT));
                } else {
                    nbt = JsonToNBT.getTagFromJson(JSONUtils.getString(jsonNBT, JsonConstants.NBT));
                }
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT entry for fluid '" + resourceLocation + "'");
            }
        }
        return new FluidStack(fluid, amount, nbt);
    }

    public static InfusionStack deserializeInfuseType(@Nonnull JsonObject json) {
        if (!json.has(JsonConstants.AMOUNT)) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get(JsonConstants.AMOUNT);
        if (!JSONUtils.isNumber(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, JsonConstants.INFUSE_TYPE));
        InfuseType infuseType = InfuseType.getFromRegistry(resourceLocation);
        if (infuseType.isEmptyType()) {
            throw new JsonSyntaxException("Invalid infusion type '" + resourceLocation + "'");
        }
        return new InfusionStack(infuseType, amount);
    }


    public static JsonElement serializeItemStack(@Nonnull ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.ITEM, stack.getItem().getRegistryName().toString());
        if (stack.getCount() > 1) {
            json.addProperty(JsonConstants.COUNT, stack.getCount());
        }
        if (stack.hasTag()) {
            json.addProperty(JsonConstants.NBT, stack.getTag().toString());
        }
        return json;
    }

    public static JsonElement serializeGasStack(@Nonnull GasStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.GAS, stack.getType().getRegistryName().toString());
        json.addProperty(JsonConstants.AMOUNT, stack.getAmount());
        return json;
    }

    public static JsonElement serializeFluidStack(@Nonnull FluidStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.FLUID, stack.getFluid().getRegistryName().toString());
        json.addProperty(JsonConstants.AMOUNT, stack.getAmount());
        if (stack.hasTag()) {
            json.addProperty(JsonConstants.NBT, stack.getTag().toString());
        }
        return json;
    }

    public static JsonElement serializeInfusionStack(@Nonnull InfusionStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.INFUSE_TYPE, stack.getType().getRegistryName().toString());
        json.addProperty(JsonConstants.AMOUNT, stack.getAmount());
        return json;
    }
}