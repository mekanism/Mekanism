package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//todo replace with chemical slot incl conversions
@NothingNullByDefault
public class InfusionInventorySlot extends ChemicalInventorySlot {

    /**
     * Gets the InfusionStack from ItemStack conversion, ignoring the size of the item stack.
     */
    public static ChemicalStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
        return getPotentialConversion(MekanismRecipeType.INFUSION_CONVERSION, world, itemStack);
    }

    /**
     * Fills the tank from this item OR converts the given item to an infusion type
     */
    public static InfusionInventorySlot fillOrConvert(IChemicalTank infusionTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, ChemicalStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new InfusionInventorySlot(infusionTank, worldSupplier, getFillOrConvertExtractPredicate(infusionTank, Capabilities.CHEMICAL, potentialConversionSupplier),
              getFillOrConvertInsertPredicate(infusionTank, Capabilities.CHEMICAL, potentialConversionSupplier), listener, x, y);
    }

    private InfusionInventorySlot(IChemicalTank infusionTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(infusionTank, worldSupplier, canExtract, canInsert, listener, x, y);
    }

    @Nullable
    @Override
    protected ItemStackToInfuseTypeRecipe getConversionRecipe(@Nullable Level world, ItemStack stack) {
        return MekanismRecipeType.INFUSION_CONVERSION.getInputCache().findFirstRecipe(world, stack);
    }
}