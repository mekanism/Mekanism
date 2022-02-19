package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.ISlurryProvider;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.MultiIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.SingleIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.TaggedIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;

/**
 * Base implementation for how Mekanism handle's SlurryStack Ingredients.
 */
public interface SlurryStackIngredient extends IChemicalStackIngredient<Slurry, SlurryStack> {

    /**
     * Creates a Slurry Stack Ingredient that matches a given slurry stack.
     *
     * @param instance Slurry stack to match.
     */
    static SlurryStackIngredient from(@Nonnull SlurryStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    /**
     * Creates a Slurry Stack Ingredient that matches a provided slurry and amount.
     *
     * @param slurry Slurry provider that provides the slurry to match.
     * @param amount Amount needed.
     */
    static SlurryStackIngredient from(@Nonnull ISlurryProvider slurry, long amount) {
        return new Single(slurry.getStack(amount));
    }

    /**
     * Creates a Slurry Stack Ingredient that matches a given slurry tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     */
    static SlurryStackIngredient from(@Nonnull ITag<Slurry> tag, long amount) {
        return new Tagged(tag, amount);
    }

    /**
     * Reads a Slurry Stack Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Slurry Stack Ingredient.
     */
    static SlurryStackIngredient read(PacketBuffer buffer) {
        return ChemicalIngredientDeserializer.SLURRY.read(buffer);
    }

    /**
     * Helper to deserialize a Json Object into a Slurry Stack Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @return Slurry Stack Ingredient.
     */
    static SlurryStackIngredient deserialize(@Nullable JsonElement json) {
        return ChemicalIngredientDeserializer.SLURRY.deserialize(json);
    }

    /**
     * Combines multiple Slurry Stack Ingredients into a single Slurry Stack Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Slurry Stack Ingredient.
     */
    static SlurryStackIngredient createMulti(SlurryStackIngredient... ingredients) {
        return ChemicalIngredientDeserializer.SLURRY.createMulti(ingredients);
    }

    @Override
    default ChemicalIngredientInfo<Slurry, SlurryStack> getIngredientInfo() {
        return ChemicalIngredientInfo.SLURRY;
    }

    class Single extends SingleIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        private Single(@Nonnull SlurryStack stack) {
            super(stack);
        }
    }

    class Tagged extends TaggedIngredient<Slurry, SlurryStack> implements SlurryStackIngredient {

        private Tagged(@Nonnull ITag<Slurry> tag, long amount) {
            super(tag, amount);
        }
    }

    class Multi extends MultiIngredient<Slurry, SlurryStack, SlurryStackIngredient> implements SlurryStackIngredient {

        Multi(@Nonnull SlurryStackIngredient... ingredients) {
            super(ingredients);
        }
    }
}