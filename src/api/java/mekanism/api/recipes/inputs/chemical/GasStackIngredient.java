package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IGasProvider;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.MultiIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.SingleIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.TaggedIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;

/**
 * Base implementation for how Mekanism handle's GasStack Ingredients.
 */
public interface GasStackIngredient extends IChemicalStackIngredient<Gas, GasStack> {

    /**
     * Creates a Gas Stack Ingredient that matches a given gas stack.
     *
     * @param instance Gas stack to match.
     */
    static GasStackIngredient from(@Nonnull GasStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    /**
     * Creates a Gas Stack Ingredient that matches a provided gas and amount.
     *
     * @param gas    Gas provider that provides the gas to match.
     * @param amount Amount needed.
     */
    static GasStackIngredient from(@Nonnull IGasProvider gas, long amount) {
        return new Single(gas.getStack(amount));
    }

    /**
     * Creates a Gas Stack Ingredient that matches a given gas tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     */
    static GasStackIngredient from(@Nonnull ITag<Gas> tag, long amount) {
        return new Tagged(tag, amount);
    }

    /**
     * Reads a Gas Stack Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Gas Stack Ingredient.
     */
    static GasStackIngredient read(PacketBuffer buffer) {
        return ChemicalIngredientDeserializer.GAS.read(buffer);
    }

    /**
     * Helper to deserialize a Json Object into a Gas Stack Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @return Gas Stack Ingredient.
     */
    static GasStackIngredient deserialize(@Nullable JsonElement json) {
        return ChemicalIngredientDeserializer.GAS.deserialize(json);
    }

    /**
     * Combines multiple Gas Stack Ingredients into a single Gas Stack Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Gas Stack Ingredient.
     */
    static GasStackIngredient createMulti(GasStackIngredient... ingredients) {
        return ChemicalIngredientDeserializer.GAS.createMulti(ingredients);
    }

    @Override
    default ChemicalIngredientInfo<Gas, GasStack> getIngredientInfo() {
        return ChemicalIngredientInfo.GAS;
    }

    class Single extends SingleIngredient<Gas, GasStack> implements GasStackIngredient {

        private Single(@Nonnull GasStack stack) {
            super(stack);
        }
    }

    class Tagged extends TaggedIngredient<Gas, GasStack> implements GasStackIngredient {

        private Tagged(@Nonnull ITag<Gas> tag, long amount) {
            super(tag, amount);
        }
    }

    class Multi extends MultiIngredient<Gas, GasStack, GasStackIngredient> implements GasStackIngredient {

        Multi(@Nonnull GasStackIngredient... ingredients) {
            super(ingredients);
        }
    }
}