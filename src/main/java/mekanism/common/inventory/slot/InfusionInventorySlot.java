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
import mekanism.api.infuse.BasicInfusionTank;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.inventory.AutomationType;
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
    public static InfusionInventorySlot input(BasicInfusionTank infusionTank, Supplier<World> worldSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new InfusionInventorySlot(infusionTank, worldSupplier, stack -> {
            InfusionStack infusionStack = getPotentialConversion(worldSupplier.get(), stack);
            //Allow extraction IFF after a reload an item no longer has an infusion type
            return infusionStack.isEmpty() || !infusionTank.isValid(infusionStack);
        }, stack -> {
            InfusionStack infusionStack = getPotentialConversion(worldSupplier.get(), stack);
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            return !infusionStack.isEmpty() && infusionTank.insert(infusionStack, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < infusionStack.getAmount();
        }, stack -> {
            InfusionStack infusionStack = getPotentialConversion(worldSupplier.get(), stack);
            return !infusionStack.isEmpty() && infusionTank.isValid(infusionStack);
        }, inventory, x, y);
    }

    //TODO: Replace InfusionTank with an IInfusionHandler??
    private final BasicInfusionTank infusionTank;
    private final Supplier<World> worldSupplier;

    private InfusionInventorySlot(BasicInfusionTank infusionTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.infusionTank = infusionTank;
        this.worldSupplier = worldSupplier;
    }

    public void fillTank() {
        if (!isEmpty()) {
            ItemStackToInfuseTypeRecipe foundRecipe = MekanismRecipeType.INFUSION_CONVERSION.findFirst(worldSupplier.get(), recipe -> recipe.getInput().test(current));
            if (foundRecipe != null) {
                ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                if (!itemInput.isEmpty()) {
                    InfusionStack pendingInfusionInput = foundRecipe.getOutput(itemInput);
                    if (!pendingInfusionInput.isEmpty() && infusionTank.insert(pendingInfusionInput, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                        //If we can accept it all, then add it and decrease our input
                        int amount = pendingInfusionInput.getAmount();
                        if (infusionTank.shrinkStack(amount, Action.EXECUTE) != amount) {
                            //TODO: Print warning/error
                        }
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