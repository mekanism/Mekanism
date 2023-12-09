package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.Mekanism;
import mekanism.api.recipes.basic.IBasicChemicalOutput;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public abstract class ChemicalChemicalToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT> & IBasicChemicalOutput<CHEMICAL, STACK>>
      implements RecipeSerializer<RECIPE> {

    private final IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory;
    private final Codec<STACK> stackCodec;
    private Codec<RECIPE> codec;

    protected ChemicalChemicalToChemicalRecipeSerializer(IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory, Codec<STACK> stackCodec) {
        this.factory = factory;
        this.stackCodec = stackCodec;
    }

    protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

    protected abstract STACK fromBuffer(@NotNull FriendlyByteBuf buffer);

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            Codec<INGREDIENT> ingredientCodec = getDeserializer().codec();
            codec = RecordCodecBuilder.create(instance->instance.group(
                  ingredientCodec.fieldOf(JsonConstants.LEFT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getLeftInput),
                  ingredientCodec.fieldOf(JsonConstants.RIGHT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getRightInput),
                  stackCodec.fieldOf(JsonConstants.OUTPUT).forGetter(IBasicChemicalOutput::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            INGREDIENT leftInput = getDeserializer().read(buffer);
            INGREDIENT rightInput = getDeserializer().read(buffer);
            STACK output = fromBuffer(buffer);
            return this.factory.create(leftInput, rightInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading chemical chemical to chemical recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.getLeftInput().write(buffer);
            recipe.getRightInput().write(buffer);
            recipe.getOutputRaw().writeToPacket(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing chemical chemical to chemical recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> {

        RECIPE create(INGREDIENT leftInput, INGREDIENT rightInput, STACK output);
    }
}