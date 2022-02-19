package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.MultiIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.SingleIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.TaggedIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;

/**
 * Base implementation for how Mekanism handle's PigmentStack Ingredients.
 */
public interface PigmentStackIngredient extends IChemicalStackIngredient<Pigment, PigmentStack> {

    /**
     * Creates a Pigment Stack Ingredient that matches a given pigment stack.
     *
     * @param instance Pigment stack to match.
     */
    static PigmentStackIngredient from(@Nonnull PigmentStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    /**
     * Creates a Pigment Stack Ingredient that matches a provided pigment and amount.
     *
     * @param pigment Pigment provider that provides the pigment to match.
     * @param amount  Amount needed.
     */
    static PigmentStackIngredient from(@Nonnull IPigmentProvider pigment, long amount) {
        return new Single(pigment.getStack(amount));
    }

    /**
     * Creates a Pigment Stack Ingredient that matches a given pigment tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     */
    static PigmentStackIngredient from(@Nonnull ITag<Pigment> tag, long amount) {
        return new Tagged(tag, amount);
    }

    /**
     * Reads a Pigment Stack Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Pigment Stack Ingredient.
     */
    static PigmentStackIngredient read(PacketBuffer buffer) {
        return ChemicalIngredientDeserializer.PIGMENT.read(buffer);
    }

    /**
     * Helper to deserialize a Json Object into a Pigment Stack Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @return Pigment Stack Ingredient.
     */
    static PigmentStackIngredient deserialize(@Nullable JsonElement json) {
        return ChemicalIngredientDeserializer.PIGMENT.deserialize(json);
    }

    /**
     * Combines multiple Pigment Stack Ingredients into a single Pigment Stack Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Pigment Stack Ingredient.
     */
    static PigmentStackIngredient createMulti(PigmentStackIngredient... ingredients) {
        return ChemicalIngredientDeserializer.PIGMENT.createMulti(ingredients);
    }

    @Override
    default ChemicalIngredientInfo<Pigment, PigmentStack> getIngredientInfo() {
        return ChemicalIngredientInfo.PIGMENT;
    }

    class Single extends SingleIngredient<Pigment, PigmentStack> implements PigmentStackIngredient {

        private Single(@Nonnull PigmentStack stack) {
            super(stack);
        }
    }

    class Tagged extends TaggedIngredient<Pigment, PigmentStack> implements PigmentStackIngredient {

        private Tagged(@Nonnull ITag<Pigment> tag, long amount) {
            super(tag, amount);
        }
    }

    class Multi extends MultiIngredient<Pigment, PigmentStack, PigmentStackIngredient> implements PigmentStackIngredient {

        Multi(@Nonnull PigmentStackIngredient... ingredients) {
            super(ingredients);
        }
    }
}