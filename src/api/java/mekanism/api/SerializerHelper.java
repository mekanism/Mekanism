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
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
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

    private SerializerHelper() {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Deserializes a FloatingLong that is stored in a specific key in a Json Object.
     *
     * @param json Json Object.
     * @param key  Key the FloatingLong is stored in.
     *
     * @return FloatingLong.
     */
    public static FloatingLong getFloatingLong(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        JsonElement jsonElement = json.get(key);
        if (!jsonElement.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be a json primitive representing a FloatingLong");
        }
        try {
            return FloatingLong.parseFloatingLong(jsonElement.getAsNumber().toString(), true);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException("Expected '" + key + "' to be a valid FloatingLong (positive decimal number)");
        }
    }

    private static void validateKey(@Nonnull JsonObject json, @Nonnull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        if (!json.get(key).isJsonObject()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be an object");
        }
    }

    /**
     * Gets and deserializes a Chemical Type from a given Json Object.
     *
     * @param json Json Object.
     *
     * @return Chemical Type.
     */
    public static ChemicalType getChemicalType(@Nonnull JsonObject json) {
        if (!json.has(JsonConstants.CHEMICAL_TYPE)) {
            throw new JsonSyntaxException("Missing '" + JsonConstants.CHEMICAL_TYPE + "', expected to find a string");
        }
        JsonElement element = json.get(JsonConstants.CHEMICAL_TYPE);
        if (!element.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected '" + JsonConstants.CHEMICAL_TYPE + "' to be a json primitive representing a string");
        }
        String name = element.getAsString();
        ChemicalType chemicalType = ChemicalType.fromString(name);
        if (chemicalType == null) {
            throw new JsonSyntaxException("Invalid chemical type '" + name + "'.");
        }
        return chemicalType;
    }

    /**
     * Helper to get and deserialize an Item Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains an Item Stack.
     *
     * @return Item Stack.
     */
    public static ItemStack getItemStack(@Nonnull JsonObject json, @Nonnull String key) {
        validateKey(json, key);
        return ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Fluid Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Fluid Stack.
     *
     * @return Fluid Stack.
     */
    public static FluidStack getFluidStack(@Nonnull JsonObject json, @Nonnull String key) {
        validateKey(json, key);
        return deserializeFluid(JSONUtils.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Chemical Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Chemical Stack.
     *
     * @return Chemical Stack.
     */
    public static ChemicalStack<?> getBoxedChemicalStack(@Nonnull JsonObject json, @Nonnull String key) {
        validateKey(json, key);
        JsonObject jsonObject = JSONUtils.getAsJsonObject(json, key);
        ChemicalType chemicalType = getChemicalType(jsonObject);
        if (chemicalType == ChemicalType.GAS) {
            return deserializeGas(jsonObject);
        } else if (chemicalType == ChemicalType.INFUSION) {
            return deserializeInfuseType(jsonObject);
        } else if (chemicalType == ChemicalType.PIGMENT) {
            return deserializePigment(jsonObject);
        } else if (chemicalType == ChemicalType.SLURRY) {
            return deserializeSlurry(jsonObject);
        } else {
            throw new IllegalStateException("Unknown chemical type");
        }
    }

    /**
     * Helper to get and deserialize a Gas Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Gas Stack.
     *
     * @return Gas Stack.
     */
    public static GasStack getGasStack(@Nonnull JsonObject json, @Nonnull String key) {
        validateKey(json, key);
        return deserializeGas(JSONUtils.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize an Infusion Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains an Infusion Stack.
     *
     * @return Infusion Stack.
     */
    public static InfusionStack getInfusionStack(@Nonnull JsonObject json, @Nonnull String key) {
        validateKey(json, key);
        return deserializeInfuseType(JSONUtils.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Pigment Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Pigment Stack.
     *
     * @return Pigment Stack.
     */
    public static PigmentStack getPigmentStack(@Nonnull JsonObject json, @Nonnull String key) {
        validateKey(json, key);
        return deserializePigment(JSONUtils.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Slurry Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Slurry Stack.
     *
     * @return Slurry Stack.
     */
    public static SlurryStack getSlurryStack(@Nonnull JsonObject json, @Nonnull String key) {
        validateKey(json, key);
        return deserializeSlurry(JSONUtils.getAsJsonObject(json, key));
    }

    /**
     * Helper to deserialize a Json Object into a Fluid Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Fluid Stack.
     */
    public static FluidStack deserializeFluid(@Nonnull JsonObject json) {
        if (!json.has(JsonConstants.AMOUNT)) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get(JsonConstants.AMOUNT);
        if (!JSONUtils.isNumberValue(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getAsString(json, JsonConstants.FLUID));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("Invalid fluid type '" + resourceLocation + "'");
        }
        CompoundNBT nbt = null;
        if (json.has(JsonConstants.NBT)) {
            JsonElement jsonNBT = json.get(JsonConstants.NBT);
            try {
                if (jsonNBT.isJsonObject()) {
                    nbt = JsonToNBT.parseTag(GSON.toJson(jsonNBT));
                } else {
                    nbt = JsonToNBT.parseTag(JSONUtils.convertToString(jsonNBT, JsonConstants.NBT));
                }
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT entry for fluid '" + resourceLocation + "'");
            }
        }
        return new FluidStack(fluid, amount, nbt);
    }

    /**
     * Helper to deserialize a Json Object into a Gas Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Gas Stack.
     */
    public static GasStack deserializeGas(@Nonnull JsonObject json) {
        return ChemicalIngredientDeserializer.GAS.deserializeStack(json);
    }

    /**
     * Helper to deserialize a Json Object into an Infusion Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Infusion Stack.
     */
    public static InfusionStack deserializeInfuseType(@Nonnull JsonObject json) {
        return ChemicalIngredientDeserializer.INFUSION.deserializeStack(json);
    }

    /**
     * Helper to deserialize a Json Object into a Pigment Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Pigment Stack.
     */
    public static PigmentStack deserializePigment(@Nonnull JsonObject json) {
        return ChemicalIngredientDeserializer.PIGMENT.deserializeStack(json);
    }

    /**
     * Helper to deserialize a Json Object into a Slurry Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Slurry Stack.
     */
    public static SlurryStack deserializeSlurry(@Nonnull JsonObject json) {
        return ChemicalIngredientDeserializer.SLURRY.deserializeStack(json);
    }

    /**
     * Helper to serialize an Item Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
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

    /**
     * Helper to serialize a Fluid Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonElement serializeFluidStack(@Nonnull FluidStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.FLUID, stack.getFluid().getRegistryName().toString());
        json.addProperty(JsonConstants.AMOUNT, stack.getAmount());
        if (stack.hasTag()) {
            json.addProperty(JsonConstants.NBT, stack.getTag().toString());
        }
        return json;
    }

    /**
     * Helper to serialize a Boxed Chemical Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonElement serializeBoxedChemicalStack(@Nonnull BoxedChemicalStack stack) {
        JsonObject json;
        ChemicalType chemicalType = stack.getChemicalType();
        if (chemicalType == ChemicalType.GAS) {
            json = serializeGasStack((GasStack) stack.getChemicalStack());
        } else if (chemicalType == ChemicalType.INFUSION) {
            json = serializeInfusionStack((InfusionStack) stack.getChemicalStack());
        } else if (chemicalType == ChemicalType.PIGMENT) {
            json = serializePigmentStack((PigmentStack) stack.getChemicalStack());
        } else if (chemicalType == ChemicalType.SLURRY) {
            json = serializeSlurryStack((SlurryStack) stack.getChemicalStack());
        } else {
            throw new IllegalStateException("Unknown chemical type");
        }
        json.addProperty(JsonConstants.CHEMICAL_TYPE, chemicalType.getSerializedName());
        return json;
    }

    /**
     * Helper to serialize a Gas Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonObject serializeGasStack(@Nonnull GasStack stack) {
        return ChemicalIngredientDeserializer.GAS.serializeStack(stack);
    }

    /**
     * Helper to serialize an Infusion Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonObject serializeInfusionStack(@Nonnull InfusionStack stack) {
        return ChemicalIngredientDeserializer.INFUSION.serializeStack(stack);
    }

    /**
     * Helper to serialize a Pigment Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonObject serializePigmentStack(@Nonnull PigmentStack stack) {
        return ChemicalIngredientDeserializer.PIGMENT.serializeStack(stack);
    }

    /**
     * Helper to serialize a Slurry Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonObject serializeSlurryStack(@Nonnull SlurryStack stack) {
        return ChemicalIngredientDeserializer.SLURRY.serializeStack(stack);
    }

    /**
     * Gets the deserializer type for a given chemical.
     *
     * @param chemicalType Type of chemical.
     *
     * @return Deserializer.
     */
    public static ChemicalIngredientDeserializer<?, ?, ?> getDeserializerForType(ChemicalType chemicalType) {
        switch (chemicalType) {
            case GAS:
                return ChemicalIngredientDeserializer.GAS;
            case INFUSION:
                return ChemicalIngredientDeserializer.INFUSION;
            case PIGMENT:
                return ChemicalIngredientDeserializer.PIGMENT;
            case SLURRY:
                return ChemicalIngredientDeserializer.SLURRY;
            default:
                throw new IllegalStateException("Unknown Chemical Type");
        }
    }
}