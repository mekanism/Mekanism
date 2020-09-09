package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.MultiIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * Internal helper class used to reduce the additional code needed to deserialize different types of chemical stack ingredients
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("Convert2Diamond")//The types cannot properly be inferred
public class ChemicalIngredientDeserializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> {

    public static final ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> GAS = new ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient>(
          "gas", ChemicalIngredientInfo.GAS, ChemicalTags.GAS, GasStack::readFromPacket, Gas::getFromRegistry, GasStackIngredient::from, GasStackIngredient::from,
          GasStackIngredient.Multi::new, GasStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient> INFUSION =
          new ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient>("infuse type", ChemicalIngredientInfo.INFUSION,
                ChemicalTags.INFUSE_TYPE, InfusionStack::readFromPacket, InfuseType::getFromRegistry, InfusionStackIngredient::from, InfusionStackIngredient::from,
                InfusionStackIngredient.Multi::new, InfusionStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> PIGMENT =
          new ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient>("pigment", ChemicalIngredientInfo.PIGMENT, ChemicalTags.PIGMENT,
                PigmentStack::readFromPacket, Pigment::getFromRegistry, PigmentStackIngredient::from, PigmentStackIngredient::from, PigmentStackIngredient.Multi::new,
                PigmentStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<Slurry, SlurryStack, SlurryStackIngredient> SLURRY =
          new ChemicalIngredientDeserializer<Slurry, SlurryStack, SlurryStackIngredient>("slurry", ChemicalIngredientInfo.SLURRY, ChemicalTags.SLURRY,
                SlurryStack::readFromPacket, Slurry::getFromRegistry, SlurryStackIngredient::from, SlurryStackIngredient::from, SlurryStackIngredient.Multi::new,
                SlurryStackIngredient[]::new);

    private final ChemicalTags<CHEMICAL> tags;
    private final Function<PacketBuffer, STACK> fromPacket;
    private final Function<ResourceLocation, CHEMICAL> fromRegistry;
    private final ChemicalIngredientInfo<CHEMICAL, STACK> info;
    private final Function<STACK, INGREDIENT> stackToIngredient;
    private final TagIngredientCreator<CHEMICAL, STACK, INGREDIENT> tagToIngredient;
    private final IntFunction<INGREDIENT[]> arrayCreator;
    private final Function<INGREDIENT[], INGREDIENT> multiCreator;
    private final String name;

    private ChemicalIngredientDeserializer(String name, ChemicalIngredientInfo<CHEMICAL, STACK> info, ChemicalTags<CHEMICAL> tags, Function<PacketBuffer, STACK> fromPacket,
          Function<ResourceLocation, CHEMICAL> fromRegistry, Function<STACK, INGREDIENT> stackToIngredient, TagIngredientCreator<CHEMICAL, STACK, INGREDIENT> tagToIngredient,
          Function<INGREDIENT[], INGREDIENT> multiCreator, IntFunction<INGREDIENT[]> arrayCreator) {
        this.fromPacket = fromPacket;
        this.fromRegistry = fromRegistry;
        this.tags = tags;
        this.info = info;
        this.stackToIngredient = stackToIngredient;
        this.tagToIngredient = tagToIngredient;
        this.arrayCreator = arrayCreator;
        this.multiCreator = multiCreator;
        this.name = name;
    }

    private String getNameWithPrefix() {
        if ("aeiou".indexOf(Character.toLowerCase(name.charAt(0))) == -1) {
            return "a " + name;
        }
        return "an " + name;
    }

    public final INGREDIENT read(PacketBuffer buffer) {
        IngredientType type = buffer.readEnumValue(IngredientType.class);
        if (type == IngredientType.SINGLE) {
            return stackToIngredient.apply(fromPacket.apply(buffer));
        } else if (type == IngredientType.TAGGED) {
            return tagToIngredient.create(tags.tag(buffer.readResourceLocation()), buffer.readVarLong());
        }
        INGREDIENT[] ingredients = arrayCreator.apply(buffer.readVarInt());
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = read(buffer);
        }
        return createMulti(ingredients);
    }

    public final INGREDIENT deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined");
            } else if (size > 1) {
                INGREDIENT[] ingredients = arrayCreator.apply(size);
                for (int i = 0; i < size; i++) {
                    //Read all the ingredients
                    ingredients[i] = deserialize(jsonArray.get(i));
                }
                return createMulti(ingredients);
            }
            //If we only have a single element, just set our json as that so that we don't have to use Multi for efficiency reasons
            json = jsonArray.get(0);
        }
        if (!json.isJsonObject()) {
            throw new JsonSyntaxException("Expected " + name + " to be object or array of objects");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        String serializationKey = info.getSerializationKey();
        if (jsonObject.has(serializationKey) && jsonObject.has(JsonConstants.TAG)) {
            throw new JsonParseException("An ingredient entry is either a tag or " + getNameWithPrefix() + ", not both");
        } else if (jsonObject.has(serializationKey)) {
            return stackToIngredient.apply(deserializeStack(jsonObject));
        } else if (jsonObject.has(JsonConstants.TAG)) {
            if (!jsonObject.has(JsonConstants.AMOUNT)) {
                throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
            }
            JsonElement count = jsonObject.get(JsonConstants.AMOUNT);
            if (!JSONUtils.isNumber(count)) {
                throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
            }
            long amount = count.getAsJsonPrimitive().getAsLong();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to be greater than zero.");
            }
            ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(jsonObject, JsonConstants.TAG));
            ITag<CHEMICAL> tag = tags.getCollection().get(resourceLocation);
            if (tag == null) {
                throw new JsonSyntaxException("Unknown " + name + " tag '" + resourceLocation + "'");
            }
            return tagToIngredient.create(tag, amount);
        }
        throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or " + getNameWithPrefix() + ".");
    }

    @SafeVarargs
    public final INGREDIENT createMulti(INGREDIENT... ingredients) {
        if (ingredients.length == 0) {
            //TODO: Throw error
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<INGREDIENT> cleanedIngredients = new ArrayList<>();
        for (INGREDIENT ingredient : ingredients) {
            if (ingredient instanceof ChemicalStackIngredient.MultiIngredient) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                cleanedIngredients.addAll(((MultiIngredient<CHEMICAL, STACK, INGREDIENT>) ingredient).getIngredients());
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single ingredient or we would have split out earlier
        return multiCreator.apply(cleanedIngredients.toArray(arrayCreator.apply(0)));
    }

    public final STACK deserializeStack(@Nonnull JsonObject json) {
        if (!json.has(JsonConstants.AMOUNT)) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get(JsonConstants.AMOUNT);
        if (!JSONUtils.isNumber(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        long amount = count.getAsJsonPrimitive().getAsLong();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(json, info.getSerializationKey()));
        CHEMICAL chemical = fromRegistry.apply(resourceLocation);
        if (chemical.isEmptyType()) {
            throw new JsonSyntaxException("Invalid " + name + " type '" + resourceLocation + "'");
        }
        return info.createStack(chemical, amount);
    }

    public final JsonObject serializeStack(STACK stack) {
        JsonObject json = new JsonObject();
        json.addProperty(info.getSerializationKey(), stack.getType().getRegistryName().toString());
        json.addProperty(JsonConstants.AMOUNT, stack.getAmount());
        return json;
    }

    @FunctionalInterface
    public interface TagIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> {

        INGREDIENT create(ITag<CHEMICAL> tag, long amount);
    }

    public enum IngredientType {
        SINGLE,
        TAGGED,
        MULTI
    }
}