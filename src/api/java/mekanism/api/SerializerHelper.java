package mekanism.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
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

//TODO: We probably also want to move some of the json string keys to constants also
public class SerializerHelper {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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
        CompoundNBT nbt = null;
        if (json.has("nbt")) {
            JsonElement jsonNBT = json.get("nbt");
            try {
                if (jsonNBT.isJsonObject()) {
                    nbt = JsonToNBT.getTagFromJson(GSON.toJson(jsonNBT));
                } else {
                    nbt = JsonToNBT.getTagFromJson(JSONUtils.getString(jsonNBT, "nbt"));
                }
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT entry for fluid '" + resourceLocation + "'");
            }
        }
        return new FluidStack(fluid, amount, nbt);
    }

    public static InfusionStack deserializeInfuseType(@Nonnull JsonObject json) {
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
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, "infuse_type"));
        InfuseType infuseType = InfuseType.getFromRegistry(resourceLocation);
        if (infuseType.isEmptyType()) {
            throw new JsonSyntaxException("Invalid infusion type '" + resourceLocation + "'");
        }
        return new InfusionStack(infuseType, amount);
    }


    public static JsonElement serializeItemStack(@Nonnull ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", stack.getItem().getRegistryName().toString());
        if (stack.getCount() > 1) {
            json.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }

    public static JsonElement serializeGasStack(@Nonnull GasStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("gas", stack.getType().getRegistryName().toString());
        json.addProperty("amount", stack.getAmount());
        return json;
    }

    public static JsonElement serializeFluidStack(@Nonnull FluidStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", stack.getFluid().getRegistryName().toString());
        json.addProperty("amount", stack.getAmount());
        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }

    public static JsonElement serializeInfusionStack(@Nonnull InfusionStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("infuse_type", stack.getType().getRegistryName().toString());
        json.addProperty("amount", stack.getAmount());
        return json;
    }
}