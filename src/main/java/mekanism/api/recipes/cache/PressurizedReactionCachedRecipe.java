package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.tuple.Pair;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class PressurizedReactionCachedRecipe extends CachedRecipe<PressurizedReactionRecipe> {

    private final BiFunction<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull ItemStack> inputStack;
    private final Supplier<@NonNull FluidTank> fluidInputTank;
    private final Supplier<@NonNull GasTank> gasInputTank;

    public PressurizedReactionCachedRecipe(PressurizedReactionRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull ItemStack> inputStack,
          Supplier<@NonNull FluidTank> fluidInputTank, Supplier<@NonNull GasTank> gasInputTank,
          BiFunction<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputStack = inputStack;
        this.fluidInputTank = fluidInputTank;
        this.gasInputTank = gasInputTank;
        this.addToOutput = addToOutput;
    }

    @Override
    public boolean hasResourcesForTick() {
        //TODO: Implement
        return false;
    }

    @Override
    public boolean hasRoomForOutput() {
        //TODO: implement
        return false;
    }

    @Override
    protected void useResources() {
        super.useResources();
        //TODO: Use any secondary resources or remove this override
    }

    @Override
    protected void finishProcessing() {
        //TODO: add the output to the output slot
    }
}