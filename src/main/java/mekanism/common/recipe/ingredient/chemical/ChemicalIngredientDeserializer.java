package mekanism.common.recipe.ingredient.chemical;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
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
import mekanism.api.recipes.ingredients.IngredientType;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiGasStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiInfusionStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiPigmentStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient.MultiSlurryStackIngredient;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
@SuppressWarnings("Convert2Diamond")//The types cannot properly be inferred
public class ChemicalIngredientDeserializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> {

    public static final ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient> GAS = new ChemicalIngredientDeserializer<Gas, GasStack, GasStackIngredient>(
          MekanismAPI.GAS_REGISTRY_NAME, GasStack::readFromPacket, IngredientCreatorAccess.gas(), MultiGasStackIngredient::new, GasStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient> INFUSION = new ChemicalIngredientDeserializer<InfuseType, InfusionStack, InfusionStackIngredient>(
          MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, InfusionStack::readFromPacket, IngredientCreatorAccess.infusion(), MultiInfusionStackIngredient::new, InfusionStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient> PIGMENT = new ChemicalIngredientDeserializer<Pigment, PigmentStack, PigmentStackIngredient>(
          MekanismAPI.PIGMENT_REGISTRY_NAME, PigmentStack::readFromPacket, IngredientCreatorAccess.pigment(), MultiPigmentStackIngredient::new, PigmentStackIngredient[]::new);
    public static final ChemicalIngredientDeserializer<Slurry, SlurryStack, SlurryStackIngredient> SLURRY = new ChemicalIngredientDeserializer<Slurry, SlurryStack, SlurryStackIngredient>(
          MekanismAPI.SLURRY_REGISTRY_NAME, SlurryStack::readFromPacket, IngredientCreatorAccess.slurry(), MultiSlurryStackIngredient::new, SlurryStackIngredient[]::new);

    private final ResourceKey<? extends Registry<CHEMICAL>> registry;
    private final Function<FriendlyByteBuf, STACK> fromPacket;
    private final IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator;
    private final IntFunction<INGREDIENT[]> arrayCreator;
    private final Function<INGREDIENT[], INGREDIENT> multiCreator;

    private ChemicalIngredientDeserializer(ResourceKey<? extends Registry<CHEMICAL>> registry, Function<FriendlyByteBuf, STACK> fromPacket,
          IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator, Function<INGREDIENT[], INGREDIENT> multiCreator, IntFunction<INGREDIENT[]> arrayCreator) {
        this.fromPacket = fromPacket;
        this.registry = registry;
        this.ingredientCreator = ingredientCreator;
        this.arrayCreator = arrayCreator;
        this.multiCreator = multiCreator;
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
            case TAGGED -> ingredientCreator.from(TagKey.create(registry, buffer.readResourceLocation()), buffer.readVarLong());
            case MULTI -> createMulti(buffer.readArray(arrayCreator, this::read));
        };
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

    public Codec<INGREDIENT> codec() {
        return ingredientCreator.codec();
    }
}