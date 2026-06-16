package mekanism.api.recipes.inputs;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class InputHelper {

    private InputHelper() {
    }

    /// Wrap an inventory slot into an [IInputHandler].
    ///
    /// @param slot           Slot to wrap.
    /// @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
    public static IInputHandler<Item, ItemStack> getInputHandler(IInventorySlot slot, RecipeError notEnoughError) {
        return new ItemInputHandler(slot, notEnoughError);
    }

    /// Wrap a chemical tank into an [IInputHandler].
    ///
    /// @param tank           Tank to wrap.
    /// @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
    public static IInputHandler<Chemical, ChemicalStack> getInputHandler(IChemicalTank tank, RecipeError notEnoughError) {
        return new ChemicalInputHandler(tank, notEnoughError);
    }

    /// Wrap a chemical tank for constant usage into an [IInputHandler].
    ///
    /// @param tank Tank to wrap.
    public static IInputHandler<Chemical, ChemicalStack> getConstantInputHandler(IChemicalTank tank) {
        //TODO - 26.2: Should this use the normal get input handler for if the recipe isn't per tick chemical usage? Or how do we do handling for that
        return new ChemicalInputHandler(tank, RecipeError.NOT_ENOUGH_SECONDARY_INPUT) {
            @Override
            protected void resetProgress(OperationTracker tracker) {
                //Don't reset progress just because we have no output if we have constant usage
                // instead just pause the recipe
                tracker.updateOperations(0);
            }
        };
    }

    /// Wrap a fluid tank into an [IInputHandler].
    ///
    /// @param tank           Tank to wrap.
    /// @param notEnoughError The error to apply if the input does not have enough stored for the recipe to be able to perform any operations.
    public static IInputHandler<Fluid, FluidStack> getInputHandler(IFluidTank tank, RecipeError notEnoughError) {
        return new FluidInputHandler(tank, notEnoughError);
    }

    private static class ItemInputHandler extends InputResourceHandler<Item, ItemResource, IInventorySlot, ItemStack> {

        private ItemInputHandler(IInventorySlot container, RecipeError notEnoughError) {
            super(container, notEnoughError);
        }

        @Override
        protected ItemStack getEmptyStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isEmpty(ItemStack stack) {
            return stack.isEmpty();
        }

        @Override
        protected int getAmount(ItemStack stack) {
            return stack.count();
        }

        @Override
        protected ItemResource asResource(ItemStack stack) {
            return ItemResource.of(stack);
        }

        @Override
        public ItemStack getInput() {
            return container.resource().toStack(container.amountAsInt());
        }
    }

    private static class FluidInputHandler extends InputResourceHandler<Fluid, FluidResource, IFluidTank, FluidStack> {

        private FluidInputHandler(IFluidTank container, RecipeError notEnoughError) {
            super(container, notEnoughError);
        }

        @Override
        protected FluidStack getEmptyStack() {
            return FluidStack.EMPTY;
        }

        @Override
        public boolean isEmpty(FluidStack stack) {
            return stack.isEmpty();
        }

        @Override
        protected int getAmount(FluidStack stack) {
            return stack.amount();
        }

        @Override
        protected FluidResource asResource(FluidStack stack) {
            return FluidResource.of(stack);
        }

        @Override
        public FluidStack getInput() {
            return container.resource().toStack(container.amountAsInt());
        }
    }

    private static class ChemicalInputHandler extends InputResourceHandler<Chemical, ChemicalResource, IChemicalTank, ChemicalStack> {

        private ChemicalInputHandler(IChemicalTank tank, RecipeError notEnoughError) {
            super(tank, notEnoughError);
        }

        @Override
        protected ChemicalStack getEmptyStack() {
            return ChemicalStack.EMPTY;
        }

        @Override
        public boolean isEmpty(ChemicalStack stack) {
            return stack.isEmpty();
        }

        @Override
        protected int getAmount(ChemicalStack stack) {
            return stack.amount();
        }

        @Override
        protected ChemicalResource asResource(ChemicalStack stack) {
            return ChemicalResource.of(stack);
        }

        @Override
        public ChemicalStack getInput() {
            return container.resource().toStack(container.amountAsInt());
        }
    }
}