package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasInventorySlot extends ChemicalInventorySlot<Gas, GasStack> {

    @Nullable
    public static IGasHandler getCapability(ItemStack stack) {
        return getCapability(stack, Capabilities.GAS_HANDLER_CAPABILITY);
    }

    /**
     * Gets the GasStack from ItemStack conversion, ignoring the size of the item stack.
     */
    private static GasStack getPotentialConversion(@Nullable World world, ItemStack itemStack) {
        return getPotentialConversion(MekanismRecipeType.GAS_CONVERSION, world, itemStack, GasStack.EMPTY);
    }

    /**
     * Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static GasInventorySlot rotaryDrain(IGasTank gasTank, BooleanSupplier modeSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        Predicate<@NonNull ItemStack> insertPredicate = getDrainInsertPredicate(gasTank, GasInventorySlot::getCapability).and(stack -> modeSupplier.getAsBoolean());
        return new GasInventorySlot(gasTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent(), inventory, x, y);
    }

    /**
     * Fills the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static GasInventorySlot rotaryFill(IGasTank gasTank, BooleanSupplier modeSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new GasInventorySlot(gasTank, getFillExtractPredicate(gasTank, GasInventorySlot::getCapability),
              stack -> !modeSupplier.getAsBoolean() && fillInsertCheck(gasTank, getCapability(stack)),
              stack -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent(), inventory, x, y);
    }

    /**
     * Fills the tank from this item OR converts the given item to a gas
     */
    public static GasInventorySlot fillOrConvert(IGasTank gasTank, Supplier<World> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, GasStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new GasInventorySlot(gasTank, worldSupplier, getFillOrConvertExtractPredicate(gasTank, GasInventorySlot::getCapability, potentialConversionSupplier),
              getFillOrConvertInsertPredicate(gasTank, GasInventorySlot::getCapability, potentialConversionSupplier), stack -> {
            if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()) {
                //Note: we mark all gas items as valid and have a more restrictive insert check so that we allow full tanks when they are done being filled
                return true;
            }
            //Allow gas conversion of items that have a gas that is valid
            GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
            return !gasConversion.isEmpty() && gasTank.isValid(gasConversion);
        }, listener, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static GasInventorySlot fill(IGasTank gasTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        return new GasInventorySlot(gasTank, getFillExtractPredicate(gasTank, GasInventorySlot::getCapability),
              stack -> fillInsertCheck(gasTank, getCapability(stack)), stack -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent(), listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the gas tank, or if it is a gas tank container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static GasInventorySlot drain(IGasTank gasTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Predicate<@NonNull ItemStack> insertPredicate = getDrainInsertPredicate(gasTank, GasInventorySlot::getCapability);
        return new GasInventorySlot(gasTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent(),
              listener, x, y);
    }

    private GasInventorySlot(IGasTank gasTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        this(gasTank, () -> null, canExtract, canInsert, validator, listener, x, y);
    }

    private GasInventorySlot(IGasTank gasTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(gasTank, worldSupplier, canExtract, canInsert, validator, listener, x, y);
    }

    @Nullable
    @Override
    protected IChemicalHandler<Gas, GasStack> getCapability() {
        return getCapability(current);
    }

    @Nullable
    @Override
    protected MekanismRecipeType<? extends ItemStackToChemicalRecipe<Gas, GasStack>> getConversionRecipeType() {
        return MekanismRecipeType.GAS_CONVERSION;
    }
}