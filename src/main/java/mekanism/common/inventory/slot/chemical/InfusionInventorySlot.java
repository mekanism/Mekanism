package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InfusionInventorySlot extends ChemicalInventorySlot<InfuseType, InfusionStack> {

    @Nullable
    public static IInfusionHandler getCapability(ItemStack stack) {
        return getCapability(stack, Capabilities.INFUSION_HANDLER);
    }

    /**
     * Gets the InfusionStack from ItemStack conversion, ignoring the size of the item stack.
     */
    private static InfusionStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
        return getPotentialConversion(MekanismRecipeType.INFUSION_CONVERSION, world, itemStack, InfusionStack.EMPTY);
    }

    /**
     * Fills the tank from this item OR converts the given item to an infusion type
     */
    public static InfusionInventorySlot fillOrConvert(IInfusionTank infusionTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, InfusionStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new InfusionInventorySlot(infusionTank, worldSupplier, getFillOrConvertExtractPredicate(infusionTank, InfusionInventorySlot::getCapability, potentialConversionSupplier),
              getFillOrConvertInsertPredicate(infusionTank, InfusionInventorySlot::getCapability, potentialConversionSupplier), stack -> {
            if (stack.getCapability(Capabilities.INFUSION_HANDLER).isPresent()) {
                //Note: we mark all infusion items as valid and have a more restrictive insert check so that we allow full tanks when they are done being filled
                return true;
            }
            //Allow infusion conversion of items that have an infusion that is valid
            InfusionStack conversion = getPotentialConversion(worldSupplier.get(), stack);
            return !conversion.isEmpty() && infusionTank.isValid(conversion);
        }, listener, x, y);
    }

    private InfusionInventorySlot(IInfusionTank infusionTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(infusionTank, worldSupplier, canExtract, canInsert, validator, listener, x, y);
    }

    @Nullable
    @Override
    protected IChemicalHandler<InfuseType, InfusionStack> getCapability() {
        return getCapability(current);
    }

    @Nullable
    @Override
    protected ItemStackToInfuseTypeRecipe getConversionRecipe(@Nullable Level world, ItemStack stack) {
        return MekanismRecipeType.INFUSION_CONVERSION.getInputCache().findFirstRecipe(world, stack);
    }
}