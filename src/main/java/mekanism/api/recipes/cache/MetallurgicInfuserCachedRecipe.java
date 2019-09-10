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
import mekanism.api.infuse.InfusionContainer;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class MetallurgicInfuserCachedRecipe extends CachedRecipe<MetallurgicInfuserRecipe> {

    private final BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull InfusionContainer> infusionContainer;
    private final Supplier<@NonNull ItemStack> inputStack;

    public MetallurgicInfuserCachedRecipe(MetallurgicInfuserRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull InfusionContainer> infusionContainer,
          Supplier<@NonNull ItemStack> inputStack, BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.infusionContainer = infusionContainer;
        this.inputStack = inputStack;
        this.addToOutput = addToOutput;
    }

    private ItemStack getItemInput() {
        return inputStack.get();
    }

    private InfusionContainer getInfusionContainer() {
        return infusionContainer.get();
    }

    @Override
    public boolean hasResourcesForTick() {
        return recipe.test(getInfusionContainer(), getItemInput());
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getInfusionContainer(), getItemInput()), true);
    }

    @Override
    protected void finishProcessing() {
        addToOutput.apply(recipe.getOutput(getInfusionContainer(), getItemInput()), false);
    }
}