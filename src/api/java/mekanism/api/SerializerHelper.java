package mekanism.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
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
    public static FloatingLong getFloatingLong(@NotNull JsonObject json, @NotNull String key) {
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

    private static void validateKey(@NotNull JsonObject json, @NotNull String key) {
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
    public static ChemicalType getChemicalType(@NotNull JsonObject json) {
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
    public static ItemStack getItemStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Fluid Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Fluid Stack.
     *
     * @return Fluid Stack.
     */
    public static FluidStack getFluidStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return deserializeFluid(GsonHelper.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Chemical Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Chemical Stack.
     *
     * @return Chemical Stack.
     */
    public static ChemicalStack<?> getBoxedChemicalStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        JsonObject jsonObject = GsonHelper.getAsJsonObject(json, key);
        ChemicalType chemicalType = getChemicalType(jsonObject);
        return switch (chemicalType) {
            case GAS -> deserializeGas(jsonObject);
            case INFUSION -> deserializeInfuseType(jsonObject);
            case PIGMENT -> deserializePigment(jsonObject);
            case SLURRY -> deserializeSlurry(jsonObject);
        };
    }

    /**
     * Helper to get and deserialize a Gas Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Gas Stack.
     *
     * @return Gas Stack.
     */
    public static GasStack getGasStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return deserializeGas(GsonHelper.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize an Infusion Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains an Infusion Stack.
     *
     * @return Infusion Stack.
     */
    public static InfusionStack getInfusionStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return deserializeInfuseType(GsonHelper.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Pigment Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Pigment Stack.
     *
     * @return Pigment Stack.
     */
    public static PigmentStack getPigmentStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return deserializePigment(GsonHelper.getAsJsonObject(json, key));
    }

    /**
     * Helper to get and deserialize a Slurry Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Slurry Stack.
     *
     * @return Slurry Stack.
     */
    public static SlurryStack getSlurryStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return deserializeSlurry(GsonHelper.getAsJsonObject(json, key));
    }

    /**
     * Helper to deserialize a Json Object into a Fluid Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Fluid Stack.
     */
    public static FluidStack deserializeFluid(@NotNull JsonObject json) {
        if (!json.has(JsonConstants.AMOUNT)) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get(JsonConstants.AMOUNT);
        if (!GsonHelper.isNumberValue(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, JsonConstants.FLUID));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("Invalid fluid type '" + resourceLocation + "'");
        }
        CompoundTag nbt = null;
        if (json.has(JsonConstants.NBT)) {
            JsonElement jsonNBT = json.get(JsonConstants.NBT);
            try {
                if (jsonNBT.isJsonObject()) {
                    nbt = TagParser.parseTag(GSON.toJson(jsonNBT));
                } else {
                    nbt = TagParser.parseTag(GsonHelper.convertToString(jsonNBT, JsonConstants.NBT));
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
    public static GasStack deserializeGas(@NotNull JsonObject json) {
        return deserializeChemicalStack(json, JsonConstants.GAS, Gas::getFromRegistry);
    }

    /**
     * Helper to deserialize a Json Object into an Infusion Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Infusion Stack.
     */
    public static InfusionStack deserializeInfuseType(@NotNull JsonObject json) {
        return deserializeChemicalStack(json, JsonConstants.INFUSE_TYPE, InfuseType::getFromRegistry);
    }

    /**
     * Helper to deserialize a Json Object into a Pigment Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Pigment Stack.
     */
    public static PigmentStack deserializePigment(@NotNull JsonObject json) {
        return deserializeChemicalStack(json, JsonConstants.PIGMENT, Pigment::getFromRegistry);
    }

    /**
     * Helper to deserialize a Json Object into a Slurry Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Slurry Stack.
     */
    public static SlurryStack deserializeSlurry(@NotNull JsonObject json) {
        return deserializeChemicalStack(json, JsonConstants.SLURRY, Slurry::getFromRegistry);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK deserializeChemicalStack(@NotNull JsonObject json,
          @NotNull String serializationKey, @NotNull Function<ResourceLocation, CHEMICAL> fromRegistry) {
        if (!json.has(JsonConstants.AMOUNT)) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get(JsonConstants.AMOUNT);
        if (!GsonHelper.isNumberValue(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        long amount = count.getAsJsonPrimitive().getAsLong();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, serializationKey));
        CHEMICAL chemical = fromRegistry.apply(resourceLocation);
        if (chemical.isEmptyType()) {
            throw new JsonSyntaxException("Invalid " + serializationKey + " type '" + resourceLocation + "'");
        }
        //noinspection unchecked
        return (STACK) chemical.getStack(amount);
    }

    /**
     * Helper to serialize an Item Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonElement serializeItemStack(@NotNull ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.ITEM, ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
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
    public static JsonElement serializeFluidStack(@NotNull FluidStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.FLUID, ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString());
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
    public static JsonElement serializeBoxedChemicalStack(@NotNull BoxedChemicalStack stack) {
        ChemicalType chemicalType = stack.getChemicalType();
        JsonObject json = switch (chemicalType) {
            case GAS -> serializeGasStack((GasStack) stack.getChemicalStack());
            case INFUSION -> serializeInfusionStack((InfusionStack) stack.getChemicalStack());
            case PIGMENT -> serializePigmentStack((PigmentStack) stack.getChemicalStack());
            case SLURRY -> serializeSlurryStack((SlurryStack) stack.getChemicalStack());
        };
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
    public static JsonObject serializeGasStack(@NotNull GasStack stack) {
        return serializeChemicalStack(JsonConstants.GAS, stack);
    }

    /**
     * Helper to serialize an Infusion Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonObject serializeInfusionStack(@NotNull InfusionStack stack) {
        return serializeChemicalStack(JsonConstants.INFUSE_TYPE, stack);
    }

    /**
     * Helper to serialize a Pigment Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonObject serializePigmentStack(@NotNull PigmentStack stack) {
        return serializeChemicalStack(JsonConstants.PIGMENT, stack);
    }

    /**
     * Helper to serialize a Slurry Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonObject serializeSlurryStack(@NotNull SlurryStack stack) {
        return serializeChemicalStack(JsonConstants.SLURRY, stack);
    }

    private static JsonObject serializeChemicalStack(@NotNull String serializationKey, @NotNull ChemicalStack<?> stack) {
        JsonObject json = new JsonObject();
        json.addProperty(serializationKey, stack.getTypeRegistryName().toString());
        json.addProperty(JsonConstants.AMOUNT, stack.getAmount());
        return json;
    }
}