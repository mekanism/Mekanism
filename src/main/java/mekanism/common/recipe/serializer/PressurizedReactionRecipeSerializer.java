package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class PressurizedReactionRecipeSerializer implements RecipeSerializer<BasicPressurizedReactionRecipe> {

    private final IFactory<BasicPressurizedReactionRecipe> factory;
    private Codec<BasicPressurizedReactionRecipe> codec;

    public PressurizedReactionRecipeSerializer(IFactory<BasicPressurizedReactionRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicPressurizedReactionRecipe> codec() {
        if (codec == null) {
            Codec<BasicPressurizedReactionRecipe> baseCodec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.ITEM_INPUT).forGetter(PressurizedReactionRecipe::getInputSolid),
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.FLUID_INPUT).forGetter(PressurizedReactionRecipe::getInputFluid),
                  IngredientCreatorAccess.gas().codec().fieldOf(JsonConstants.GAS_INPUT).forGetter(PressurizedReactionRecipe::getInputGas),
                  FloatingLong.CODEC.optionalFieldOf(JsonConstants.ENERGY_REQUIRED, FloatingLong.ZERO).forGetter(PressurizedReactionRecipe::getEnergyRequired),
                  ExtraCodecs.POSITIVE_INT.fieldOf(JsonConstants.DURATION).forGetter(PressurizedReactionRecipe::getDuration),
                  SerializerHelper.ITEMSTACK_CODEC.optionalFieldOf(JsonConstants.ITEM_OUTPUT, ItemStack.EMPTY).forGetter(BasicPressurizedReactionRecipe::getOutputItem),
                  ChemicalUtils.GAS_STACK_CODEC.optionalFieldOf(JsonConstants.GAS_OUTPUT, GasStack.EMPTY).forGetter(BasicPressurizedReactionRecipe::getOutputGas)
            ).apply(instance, factory::create));

            codec = ExtraCodecs.validate(baseCodec, result -> {
                if (result.getOutputItem().isEmpty() && result.getOutputGas().isEmpty()) {
                    return DataResult.error(() -> "No output specified, must have at least and Item or Gas output");
                }
                return DataResult.success(result);
            });
        }
        return codec;
    }

    @NotNull
    @Override
    public BasicPressurizedReactionRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputSolid = IngredientCreatorAccess.item().read(buffer);
            FluidStackIngredient inputFluid = IngredientCreatorAccess.fluid().read(buffer);
            GasStackIngredient inputGas = IngredientCreatorAccess.gas().read(buffer);
            FloatingLong energyRequired = FloatingLong.readFromBuffer(buffer);
            int duration = buffer.readVarInt();
            ItemStack outputItem = buffer.readItem();
            GasStack outputGas = GasStack.readFromPacket(buffer);
            return this.factory.create(inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading pressurized reaction recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicPressurizedReactionRecipe recipe) {
        try {
            recipe.getInputSolid().write(buffer);
            recipe.getInputFluid().write(buffer);
            recipe.getInputGas().write(buffer);
            recipe.getEnergyRequired().writeToBuffer(buffer);
            buffer.writeVarInt(recipe.getDuration());
            buffer.writeItem(recipe.getOutputItem());
            recipe.getOutputGas().writeToPacket(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing pressurized reaction recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicPressurizedReactionRecipe> {

        RECIPE create(ItemStackIngredient itemInput, FluidStackIngredient fluidInput, GasStackIngredient gasInput, FloatingLong energyRequired, int duration,
              ItemStack outputItem, GasStack outputGas);
    }
}