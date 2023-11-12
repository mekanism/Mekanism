package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.api.recipes.basic.IBasicChemicalOutput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK> & IBasicChemicalOutput<CHEMICAL, STACK>> implements RecipeSerializer<RECIPE> {

    private final IFactory<CHEMICAL, STACK, RECIPE> factory;
    private Codec<RECIPE> codec;
    private final Codec<STACK> stackCodec;

    protected ItemStackToChemicalRecipeSerializer(IFactory<CHEMICAL, STACK, RECIPE> factory, Codec<STACK> stackCodec) {
        this.factory = factory;
        this.stackCodec = stackCodec;
    }

    protected abstract STACK stackFromBuffer(@NotNull FriendlyByteBuf buffer);

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.INPUT).forGetter(ItemStackToChemicalRecipe::getInput),
                  stackCodec.fieldOf(JsonConstants.OUTPUT).forGetter(IBasicChemicalOutput::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
            STACK output = stackFromBuffer(buffer);
            return this.factory.create(inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack to chemical recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.getInput().write(buffer);
            recipe.getOutputRaw().writeToPacket(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack to chemical recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> {

        RECIPE create(ItemStackIngredient input, STACK output);
    }
}