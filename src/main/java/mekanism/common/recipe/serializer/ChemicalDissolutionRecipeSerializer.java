package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalDissolutionRecipeSerializer implements RecipeSerializer<BasicChemicalDissolutionRecipe> {

    private final IFactory<BasicChemicalDissolutionRecipe> factory;
    private Codec<BasicChemicalDissolutionRecipe> codec;

    public ChemicalDissolutionRecipeSerializer(IFactory<BasicChemicalDissolutionRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicChemicalDissolutionRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.ITEM_INPUT).forGetter(ChemicalDissolutionRecipe::getItemInput),
                  IngredientCreatorAccess.gas().codec().fieldOf(JsonConstants.GAS_INPUT).forGetter(ChemicalDissolutionRecipe::getGasInput),
                  SerializerHelper.BOXED_CHEMICALSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(r->r.getOutputRaw().getChemicalStack())
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public BasicChemicalDissolutionRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient itemInput = IngredientCreatorAccess.item().read(buffer);
            GasStackIngredient gasInput = IngredientCreatorAccess.gas().read(buffer);
            ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
            ChemicalStack<?> output = switch (chemicalType) {
                case GAS -> GasStack.readFromPacket(buffer);
                case INFUSION -> InfusionStack.readFromPacket(buffer);
                case PIGMENT -> PigmentStack.readFromPacket(buffer);
                case SLURRY -> SlurryStack.readFromPacket(buffer);
            };
            return this.factory.create(itemInput, gasInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack gas to gas recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicChemicalDissolutionRecipe recipe) {
        try {
            recipe.getItemInput().write(buffer);
            recipe.getGasInput().write(buffer);
            buffer.writeEnum(recipe.getOutputRaw().getChemicalType());
            recipe.getOutputRaw().getChemicalStack().writeToPacket(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack gas to gas recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicChemicalDissolutionRecipe> {

        RECIPE create(ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output);
    }
}