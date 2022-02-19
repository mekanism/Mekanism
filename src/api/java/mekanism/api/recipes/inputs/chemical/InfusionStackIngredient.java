package mekanism.api.recipes.inputs.chemical;

import com.google.gson.JsonElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.MultiIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.SingleIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient.TaggedIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;

/**
 * Base implementation for how Mekanism handle's InfusionStack Ingredients.
 */
public interface InfusionStackIngredient extends IChemicalStackIngredient<InfuseType, InfusionStack> {

    /**
     * Creates an Infusion Stack Ingredient that matches a given infusion stack.
     *
     * @param instance Infusion stack to match.
     */
    static InfusionStackIngredient from(@Nonnull InfusionStack instance) {
        return from(instance.getType(), instance.getAmount());
    }

    /**
     * Creates an Infusion Stack Ingredient that matches a provided infuse type and amount.
     *
     * @param infuseType Infuse type provider that provides the infuse type to match.
     * @param amount     Amount needed.
     */
    static InfusionStackIngredient from(@Nonnull IInfuseTypeProvider infuseType, long amount) {
        return new Single(infuseType.getStack(amount));
    }

    /**
     * Creates an Infusion Stack Ingredient that matches a given infuse type tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     */
    static InfusionStackIngredient from(@Nonnull ITag<InfuseType> tag, long amount) {
        return new Tagged(tag, amount);
    }

    /**
     * Reads an Infusion Stack Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Infusion Stack Ingredient.
     */
    static InfusionStackIngredient read(PacketBuffer buffer) {
        return ChemicalIngredientDeserializer.INFUSION.read(buffer);
    }

    /**
     * Helper to deserialize a Json Object into an Infusion Stack Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @return Infusion Stack Ingredient.
     */
    static InfusionStackIngredient deserialize(@Nullable JsonElement json) {
        return ChemicalIngredientDeserializer.INFUSION.deserialize(json);
    }

    /**
     * Combines multiple Infusion Stack Ingredients into a single Infusion Stack Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Infusion Stack Ingredient.
     */
    static InfusionStackIngredient createMulti(InfusionStackIngredient... ingredients) {
        return ChemicalIngredientDeserializer.INFUSION.createMulti(ingredients);
    }

    @Override
    default ChemicalIngredientInfo<InfuseType, InfusionStack> getIngredientInfo() {
        return ChemicalIngredientInfo.INFUSION;
    }

    class Single extends SingleIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        private Single(@Nonnull InfusionStack stack) {
            super(stack);
        }
    }

    class Tagged extends TaggedIngredient<InfuseType, InfusionStack> implements InfusionStackIngredient {

        private Tagged(@Nonnull ITag<InfuseType> tag, long amount) {
            super(tag, amount);
        }
    }

    class Multi extends MultiIngredient<InfuseType, InfusionStack, InfusionStackIngredient> implements InfusionStackIngredient {

        Multi(@Nonnull InfusionStackIngredient... ingredients) {
            super(ingredients);
        }
    }
}