package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class PressurizedReactionRecipeSerializer implements RecipeSerializer<PressurizedReactionIRecipe> {

    private final IFactory<PressurizedReactionIRecipe> factory;
    private Codec<PressurizedReactionIRecipe> codec;

    public PressurizedReactionRecipeSerializer(IFactory<PressurizedReactionIRecipe> factory) {
        this.factory = factory;
    }

    @Override
    @NotNull
    public Codec<PressurizedReactionIRecipe> codec() {
        if (codec == null) {
            Codec<PressurizedReactionIRecipe> baseCodec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.ITEM_INPUT).forGetter(PressurizedReactionRecipe::getInputSolid),
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.FLUID_INPUT).forGetter(PressurizedReactionRecipe::getInputFluid),
                  IngredientCreatorAccess.gas().codec().fieldOf(JsonConstants.GAS_INPUT).forGetter(PressurizedReactionRecipe::getInputGas),
                  FloatingLong.CODEC.optionalFieldOf(JsonConstants.ENERGY_REQUIRED, FloatingLong.ZERO).forGetter(PressurizedReactionRecipe::getEnergyRequired),
                  SerializerHelper.POSITIVE_INT_CODEC.fieldOf(JsonConstants.DURATION).forGetter(PressurizedReactionRecipe::getDuration),
                  SerializerHelper.ITEMSTACK_CODEC.optionalFieldOf(JsonConstants.ITEM_OUTPUT, ItemStack.EMPTY).forGetter(PressurizedReactionIRecipe::getOutputItem),
                  GasStack.CODEC.optionalFieldOf(JsonConstants.GAS_OUTPUT, GasStack.EMPTY).forGetter(PressurizedReactionIRecipe::getOutputGas)
            ).apply(instance, factory::create));

            codec = ExtraCodecs.validate(baseCodec, result->{
                if (result.getOutputItem().isEmpty() && result.getOutputGas().isEmpty()) {
                    return DataResult.error(()->"No output specified, must have at least and Item or Gas output");
                }
                return DataResult.success(result);
            });
        }
        return codec;
    }

    @Override
    public PressurizedReactionIRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
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
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull PressurizedReactionIRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing pressurized reaction recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends PressurizedReactionRecipe> {

        RECIPE create(ItemStackIngredient itemInput, FluidStackIngredient fluidInput, GasStackIngredient gasInput, FloatingLong energyRequired, int duration,
              ItemStack outputItem, GasStack outputGas);
    }
}