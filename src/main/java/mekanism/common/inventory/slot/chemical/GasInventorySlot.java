package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GasInventorySlot extends ChemicalInventorySlot {

    /**
     * Gets the GasStack from ItemStack conversion, ignoring the size of the item stack.
     */
    public static ChemicalStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
        return getPotentialConversion(MekanismRecipeType.GAS_CONVERSION, world, itemStack);
    }

    /**
     * Fills the tank from this item OR converts the given item to a gas
     */
    public static GasInventorySlot fillOrConvert(IChemicalTank gasTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, ChemicalStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new GasInventorySlot(gasTank, worldSupplier, getFillOrConvertExtractPredicate(gasTank, Capabilities.CHEMICAL, potentialConversionSupplier),
              getFillOrConvertInsertPredicate(gasTank, Capabilities.CHEMICAL, potentialConversionSupplier), listener, x, y);
    }

    private GasInventorySlot(IChemicalTank gasTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        super(gasTank, worldSupplier, canExtract, canInsert, listener, x, y);
    }

    @Nullable
    @Override
    protected ItemStackToGasRecipe getConversionRecipe(@Nullable Level world, ItemStack stack) {
        return MekanismRecipeType.GAS_CONVERSION.getInputCache().findFirstRecipe(world, stack);
    }
}