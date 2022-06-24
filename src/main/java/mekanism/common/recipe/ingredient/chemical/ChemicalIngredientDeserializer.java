package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
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
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiGasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiInfusionStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiPigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiSlurryStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
@SuppressWarnings("Convert2Diamond")//The types cannot properly be inferred
public class ChemicalIngredientDeserializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> {

    public static final ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> GAS = new ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient>(
          "gas", ChemicalIngredientInfo.GAS, ChemicalTags.GAS, GasStack::readFromPacket, SerializerHelper::deserializeGas, IngredientCreatorAccess.gas(),
          MultiGasStackIngredient::new, GasStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient> INFUSION =
          new ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient>("infuse type", ChemicalIngredientInfo.INFUSION,
                ChemicalTags.INFUSE_TYPE, InfusionStack::readFromPacket, SerializerHelper::deserializeInfuseType, IngredientCreatorAccess.infusion(),
                MultiInfusionStackIngredient::new, InfusionStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> PIGMENT =
          new ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient>("pigment", ChemicalIngredientInfo.PIGMENT, ChemicalTags.PIGMENT,
                PigmentStack::readFromPacket, SerializerHelper::deserializePigment, IngredientCreatorAccess.pigment(), MultiPigmentStackIngredient::new,
                PigmentStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<Slurry, SlurryStack, SlurryStackIngredient> SLURRY =
          new ChemicalIngredientDeserializer<Slurry, SlurryStack, SlurryStackIngredient>("slurry", ChemicalIngredientInfo.SLURRY, ChemicalTags.SLURRY,
                SlurryStack::readFromPacket, SerializerHelper::deserializeSlurry, IngredientCreatorAccess.slurry(), MultiSlurryStackIngredient::new,
                SlurryStackIngredient[]::new);

    private final ChemicalTags<CHEMICAL> tags;
    private final Function<FriendlyByteBuf, STACK> fromPacket;
    private final Function<JsonObject, STACK> stackParser;
    private final ChemicalIngredientInfo<CHEMICAL, STACK> info;
    private final IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator;
    private final IntFunction<INGREDIENT[]> arrayCreator;
    private final Function<INGREDIENT[], INGREDIENT> multiCreator;
    private final String name;

    private ChemicalIngredientDeserializer(String name, ChemicalIngredientInfo<CHEMICAL, STACK> info, ChemicalTags<CHEMICAL> tags, Function<FriendlyByteBuf, STACK> fromPacket,
          Function<JsonObject, STACK> stackParser, IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator,
          Function<INGREDIENT[], INGREDIENT> multiCreator, IntFunction<INGREDIENT[]> arrayCreator) {
        this.fromPacket = fromPacket;
        this.stackParser = stackParser;
        this.tags = tags;
        this.info = info;
        this.ingredientCreator = ingredientCreator;
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

    /**
     * Reads a Chemical Stack Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Chemical Stack Ingredient.
     *
     * @throws NullPointerException if the given buffer is null.
     */
    public final INGREDIENT read(FriendlyByteBuf buffer) {
        Objects.requireNonNull(buffer, "ChemicalStackIngredients cannot be read from a null packet buffer.");
        return switch (buffer.readEnum(IngredientType.class)) {
            case SINGLE -> ingredientCreator.from(fromPacket.apply(buffer));
            case TAGGED -> ingredientCreator.from(tags.tag(buffer.readResourceLocation()), buffer.readVarLong());
            case MULTI -> createMulti(BasePacketHandler.readArray(buffer, arrayCreator, this::read));
        };
    }

    /**
     * Helper to deserialize a Json Object into a Chemical Stack Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @return chemical Stack Ingredient.
     */
    public final INGREDIENT deserialize(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Ingredient cannot be null.");
        }
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            int size = jsonArray.size();
            if (size == 0) {
                throw new JsonSyntaxException("Ingredient array cannot be empty, at least one ingredient must be defined.");
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
            throw new JsonSyntaxException("Expected " + name + " to be object or array of objects.");
        }
        JsonObject jsonObject = json.getAsJsonObject();
        String serializationKey = info.getSerializationKey();
        if (jsonObject.has(serializationKey) && jsonObject.has(JsonConstants.TAG)) {
            throw new JsonParseException("An ingredient entry is either a tag or " + getNameWithPrefix() + ", not both.");
        } else if (jsonObject.has(serializationKey)) {
            STACK stack = deserializeStack(jsonObject);
            if (stack.isEmpty()) {
                throw new JsonSyntaxException("Unable to create an ingredient from an empty stack.");
            }
            return ingredientCreator.from(stack);
        } else if (jsonObject.has(JsonConstants.TAG)) {
            if (!jsonObject.has(JsonConstants.AMOUNT)) {
                throw new JsonSyntaxException("Expected to receive a amount that is greater than zero.");
            }
            JsonElement count = jsonObject.get(JsonConstants.AMOUNT);
            if (!GsonHelper.isNumberValue(count)) {
                throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
            }
            long amount = count.getAsJsonPrimitive().getAsLong();
            if (amount < 1) {
                throw new JsonSyntaxException("Expected amount to be greater than zero.");
            }
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, JsonConstants.TAG));
            Optional<ITagManager<CHEMICAL>> manager = tags.getManager();
            if (manager.isEmpty()) {
                throw new JsonSyntaxException("Unexpected error trying to retrieve the chemical tag manager.");
            }
            ITagManager<CHEMICAL> tagManager = manager.get();
            TagKey<CHEMICAL> key = tagManager.createTagKey(resourceLocation);
            return ingredientCreator.from(key, amount);
        }
        throw new JsonSyntaxException("Expected to receive a resource location representing either a tag or " + getNameWithPrefix() + ".");
    }

    /**
     * Combines multiple Chemical Stack Ingredients into a single Chemical Stack Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Chemical Stack Ingredient.
     */
    @SafeVarargs
    public final INGREDIENT createMulti(INGREDIENT... ingredients) {
        Objects.requireNonNull(ingredients, "Cannot create a multi ingredient out of a null array.");
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Cannot create a multi ingredient out of no ingredients.");
        } else if (ingredients.length == 1) {
            return ingredients[0];
        }
        List<INGREDIENT> cleanedIngredients = new ArrayList<>();
        for (INGREDIENT ingredient : ingredients) {
            if (ingredient instanceof MultiChemicalStackIngredient) {
                //Don't worry about if our inner ingredients are multi as well, as if this is the only external method for
                // creating a multi ingredient, then we are certified they won't be of a higher depth
                cleanedIngredients.addAll(((MultiChemicalStackIngredient<CHEMICAL, STACK, INGREDIENT>) ingredient).getIngredients());
            } else {
                cleanedIngredients.add(ingredient);
            }
        }
        //There should be more than a single ingredient, or we would have split out earlier
        return multiCreator.apply(cleanedIngredients.toArray(arrayCreator.apply(0)));
    }

    /**
     * Helper to deserialize a Json Object into a Chemical Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Chemical Stack.
     */
    public final STACK deserializeStack(@NotNull JsonObject json) {
        return stackParser.apply(json);
    }

    enum IngredientType {
        SINGLE,
        TAGGED,
        MULTI
    }
}