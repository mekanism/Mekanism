package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GasInventorySlot extends ChemicalInventorySlot<Gas, GasStack> {

    /**
     * Gets the GasStack from ItemStack conversion, ignoring the size of the item stack.
     */
    public static GasStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
        return getPotentialConversion(MekanismRecipeType.GAS_CONVERSION, world, itemStack, GasStack.EMPTY);
    }

    /**
     * Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static GasInventorySlot rotaryDrain(IGasTank gasTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        Predicate<@NotNull ItemStack> drainInsertPredicate = getDrainInsertPredicate(gasTank, Capabilities.GAS);
        Predicate<@NotNull ItemStack> insertPredicate = stack -> modeSupplier.getAsBoolean() && drainInsertPredicate.test(stack);
        return new GasInventorySlot(gasTank, insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    /**
     * Fills the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static GasInventorySlot rotaryFill(IGasTank gasTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new GasInventorySlot(gasTank, getFillExtractPredicate(gasTank, Capabilities.GAS),
              stack -> !modeSupplier.getAsBoolean() && fillInsertCheck(gasTank, Capabilities.GAS, stack), listener, x, y);
    }

    /**
     * Fills the tank from this item OR converts the given item to a gas
     */
    public static GasInventorySlot fillOrConvert(IGasTank gasTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, GasStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new GasInventorySlot(gasTank, worldSupplier, getFillOrConvertExtractPredicate(gasTank, Capabilities.GAS, potentialConversionSupplier),
              getFillOrConvertInsertPredicate(gasTank, Capabilities.GAS, potentialConversionSupplier), listener, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static GasInventorySlot fill(IGasTank gasTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        return new GasInventorySlot(gasTank, getFillExtractPredicate(gasTank, Capabilities.GAS), stack -> fillInsertCheck(gasTank, Capabilities.GAS, stack), listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the gas tank, or if it is a gas tank container and the tank is currently empty
     * <p>
     * Drains the tank into this item.
     */
    public static GasInventorySlot drain(IGasTank gasTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Predicate<@NotNull ItemStack> insertPredicate = getDrainInsertPredicate(gasTank, Capabilities.GAS);
        return new GasInventorySlot(gasTank, insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    private GasInventorySlot(IGasTank gasTank, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener,
          int x, int y) {
        this(gasTank, () -> null, canExtract, canInsert, listener, x, y);
    }

    private GasInventorySlot(IGasTank gasTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        super(gasTank, worldSupplier, canExtract, canInsert, listener, x, y);
    }

    @Override
    protected MultiTypeCapability<IGasHandler> getChemicalCapability() {
        return Capabilities.GAS;
    }

    @Nullable
    @Override
    protected ItemStackToGasRecipe getConversionRecipe(@Nullable Level world, ItemStack stack) {
        return MekanismRecipeType.GAS_CONVERSION.getInputCache().findFirstRecipe(world, stack);
    }
}