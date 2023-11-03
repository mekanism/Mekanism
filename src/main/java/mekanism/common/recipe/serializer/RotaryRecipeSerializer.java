package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.IMekanismAccess;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RotaryRecipeSerializer<RECIPE extends RotaryRecipe> implements RecipeSerializer<RECIPE> {
    private static final Codec<Pair<FluidStackIngredient, GasStack>> FLUID_TO_GAS_CODEC = Codec.pair(IMekanismAccess.INSTANCE.fluidStackIngredientCreator().codec(), GasStack.CODEC);
    public static final Codec<Pair<GasStackIngredient, FluidStack>> GAS_TO_FLUID_CODEC = Codec.pair(IMekanismAccess.INSTANCE.gasStackIngredientCreator().codec(), FluidStack.CODEC);

    private final IFactory<RECIPE> factory;
    private final Lazy<Codec<RECIPE>> codec;

    public RotaryRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
        this.codec = Lazy.of(this::makeCodec);
    }

    private Codec<RECIPE> makeCodec() {
        return RecordCodecBuilder.create(i->i.group(
              FLUID_TO_GAS_CODEC.optionalFieldOf("f2g").forGetter(recipe-> {
                  if (recipe.hasFluidToGas()) {
                      return Optional.of(Pair.of(recipe.getFluidInput(), recipe.getGasOutput(FluidStack.EMPTY)));
                  }
                  return Optional.empty();
              }),
              GAS_TO_FLUID_CODEC.optionalFieldOf("g2f").forGetter(recipe -> {
                  if (recipe.hasGasToFluid()) {
                      return Optional.of(Pair.of(recipe.getGasInput(), recipe.getFluidOutput(GasStack.EMPTY)));
                  }
                  return Optional.empty();
              })
        ).apply(i, (f2g, g2f)->{
            if (f2g.isPresent() && g2f.isPresent()) {
                return factory.create(f2g.get().getFirst(), g2f.get().getFirst(), f2g.get().getSecond(), g2f.get().getSecond());
            }
            if (f2g.isPresent()) {
                return factory.create(f2g.get().getFirst(), f2g.get().getSecond());
            }
            if (g2f.isPresent()) {
                return factory.create(g2f.get().getFirst(), g2f.get().getSecond());
            }
            throw new IllegalStateException("Rotary recipes require at least a gas to fluid or fluid to gas conversion.");
        }));
    }

    @Override
    @NotNull
    public Codec<RECIPE> codec() {
        return this.codec.get();
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient fluidInputIngredient = null;
            GasStackIngredient gasInputIngredient = null;
            GasStack gasOutput = null;
            FluidStack fluidOutput = null;
            boolean hasFluidToGas = buffer.readBoolean();
            if (hasFluidToGas) {
                fluidInputIngredient = IngredientCreatorAccess.fluid().read(buffer);
                gasOutput = GasStack.readFromPacket(buffer);
            }
            boolean hasGasToFluid = buffer.readBoolean();
            if (hasGasToFluid) {
                gasInputIngredient = IngredientCreatorAccess.gas().read(buffer);
                fluidOutput = FluidStack.readFromPacket(buffer);
            }
            if (hasFluidToGas && hasGasToFluid) {
                return this.factory.create(fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
            } else if (hasFluidToGas) {
                return this.factory.create(fluidInputIngredient, gasOutput);
            } else if (hasGasToFluid) {
                return this.factory.create(gasInputIngredient, fluidOutput);
            }
            //Should never happen, but if we somehow get here log it
            Mekanism.logger.error("Error reading rotary recipe from packet. A recipe got sent with no conversion in either direction.");
            return null;
        } catch (Exception e) {
            Mekanism.logger.error("Error reading rotary recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing rotary recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<RECIPE extends RotaryRecipe> {

        RECIPE create(FluidStackIngredient fluidInput, GasStack gasOutput);

        RECIPE create(GasStackIngredient gasInput, FluidStack fluidOutput);

        RECIPE create(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput);
    }
}