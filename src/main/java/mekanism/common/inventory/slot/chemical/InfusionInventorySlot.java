package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InfusionInventorySlot extends ChemicalInventorySlot<InfuseType, InfusionStack> {

    /**
     * Gets the InfusionStack from ItemStack conversion, ignoring the size of the item stack.
     */
    public static InfusionStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
        return getPotentialConversion(MekanismRecipeType.INFUSION_CONVERSION, world, itemStack, InfusionStack.EMPTY);
    }

    /**
     * Fills the tank from this item OR converts the given item to an infusion type
     */
    public static InfusionInventorySlot fillOrConvert(IInfusionTank infusionTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, InfusionStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new InfusionInventorySlot(infusionTank, worldSupplier, getFillOrConvertExtractPredicate(infusionTank, Capabilities.INFUSION, potentialConversionSupplier),
              getFillOrConvertInsertPredicate(infusionTank, Capabilities.INFUSION, potentialConversionSupplier), listener, x, y);
    }

    private InfusionInventorySlot(IInfusionTank infusionTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(infusionTank, worldSupplier, canExtract, canInsert, listener, x, y);
    }

    @Override
    protected MultiTypeCapability<IInfusionHandler> getChemicalCapability() {
        return Capabilities.INFUSION;
    }

    @Nullable
    @Override
    protected ItemStackToInfuseTypeRecipe getConversionRecipe(@Nullable Level world, ItemStack stack) {
        return MekanismRecipeType.INFUSION_CONVERSION.getInputCache().findFirstRecipe(world, stack);
    }
}