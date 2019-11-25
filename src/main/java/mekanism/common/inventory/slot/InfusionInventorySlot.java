package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.infuse.InfusionTank;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfusionInventorySlot extends BasicInventorySlot {

    /**
     * Gets the InfusionStack from ItemStack conversion, ignoring the size of the item stack.
     */
    @Nonnull
    private static InfusionStack getPotentialConversion(@Nullable World world, ItemStack itemStack) {
        ItemStackToInfuseTypeRecipe foundRecipe = MekanismRecipeType.INFUSION_CONVERSION.findFirst(world, recipe -> recipe.getInput().testType(itemStack));
        return foundRecipe == null ? InfusionStack.EMPTY : foundRecipe.getOutput(itemStack);
    }

    //TODO: Rewrite this some once we make infusion tanks work as items, so that it also supports handling
    public static InfusionInventorySlot input(InfusionTank infusionTank, Predicate<InfuseType> isValidInfusion, Supplier<World> worldSupplier,
          @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(isValidInfusion, "Infusion validity check cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new InfusionInventorySlot(infusionTank, worldSupplier, stack -> {
            InfusionStack infusionStack = getPotentialConversion(worldSupplier.get(), stack);
            //Allow extraction IFF after a reload an item no longer has an infusion type
            return infusionStack.isEmpty() || !isValidInfusion.test(infusionStack.getType());
        }, stack -> {
            InfusionStack infusionStack = getPotentialConversion(worldSupplier.get(), stack);
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            return !infusionStack.isEmpty() && isValidInfusion.test(infusionStack.getType()) && infusionTank.fill(infusionStack, Action.SIMULATE) > 0;
        }, stack -> {
            InfusionStack infusionStack = getPotentialConversion(worldSupplier.get(), stack);
            return !infusionStack.isEmpty() && isValidInfusion.test(infusionStack.getType());
        }, inventory, x, y);
    }

    //TODO: Replace InfusionTank with an IInfusionHandler??
    private final InfusionTank infusionTank;
    private final Supplier<World> worldSupplier;

    private InfusionInventorySlot(InfusionTank infusionTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.infusionTank = infusionTank;
        this.worldSupplier = worldSupplier;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.EXTRA;
    }

    public void fillTank() {
        if (!current.isEmpty()) {
            ItemStackToInfuseTypeRecipe foundRecipe = MekanismRecipeType.INFUSION_CONVERSION.findFirst(worldSupplier.get(), recipe -> recipe.getInput().test(current));
            if (foundRecipe != null) {
                ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                if (!itemInput.isEmpty()) {
                    InfusionStack pendingInfusionInput = foundRecipe.getOutput(itemInput);
                    if (!pendingInfusionInput.isEmpty()) {
                        if (infusionTank.fill(pendingInfusionInput, Action.SIMULATE) == pendingInfusionInput.getAmount()) {
                            //If we can accept it all, then add it and decrease our input
                            infusionTank.fill(pendingInfusionInput, Action.EXECUTE);
                            int amountUsed = itemInput.getCount();
                            if (shrinkStack(amountUsed, Action.EXECUTE) != amountUsed) {
                                //TODO: Print warning/error
                            }
                            onContentsChanged();
                        }
                    }
                }
            }
        }
    }
}