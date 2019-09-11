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
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.tuple.Pair;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ElectrolysisCachedRecipe extends CachedRecipe<ElectrolysisRecipe> {

    private final BiFunction<@NonNull Pair<GasStack, GasStack>, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull FluidTank> inputTank;
    private final IntSupplier maxOperations;

    public ElectrolysisCachedRecipe(ElectrolysisRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull FluidTank> inputTank,
          IntSupplier maxOperations, BiFunction<@NonNull Pair<GasStack, GasStack>, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputTank = inputTank;
        this.maxOperations = maxOperations;
        this.addToOutput = addToOutput;
    }

    private FluidTank getTank() {
        return inputTank.get();
    }

    private int getMaxOperations() {
        //TODO: Use this
        return maxOperations.getAsInt();
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