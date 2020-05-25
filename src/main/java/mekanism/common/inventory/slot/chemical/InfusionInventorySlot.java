package mekanism.common.inventory.slot.chemical;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.ChemicalInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfusionInventorySlot extends ChemicalInventorySlot<InfuseType, InfusionStack> {

    @Nullable
    public static IInfusionHandler getCapability(ItemStack stack) {
        if (!stack.isEmpty()) {
            Optional<IInfusionHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                return capability.get();
            }
        }
        return null;
    }

    /**
     * Gets the InfusionStack from ItemStack conversion, ignoring the size of the item stack.
     */
    private static InfusionStack getPotentialConversion(@Nullable World world, ItemStack itemStack) {
        ItemStackToInfuseTypeRecipe foundRecipe = MekanismRecipeType.INFUSION_CONVERSION.findFirst(world, recipe -> recipe.getInput().testType(itemStack));
        return foundRecipe == null ? InfusionStack.EMPTY : foundRecipe.getOutput(itemStack);
    }

    /**
     * Fills the tank from this item OR converts the given item to an infusion type
     */
    public static InfusionInventorySlot fillOrConvert(IInfusionTank infusionTank, Supplier<World> worldSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, InfusionStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new InfusionInventorySlot(infusionTank, worldSupplier, getFillOrConvertExtractPredicate(infusionTank, InfusionInventorySlot::getCapability, potentialConversionSupplier),
              getFillOrConvertInsertPredicate(infusionTank, InfusionInventorySlot::getCapability, potentialConversionSupplier), stack -> {
            if (stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY).isPresent()) {
                //Note: we mark all infusion items as valid and have a more restrictive insert check so that we allow full tanks when they are done being filled
                return true;
            }
            //Allow infusion conversion of items that have a infusion that is valid
            InfusionStack conversion = getPotentialConversion(worldSupplier.get(), stack);
            return !conversion.isEmpty() && infusionTank.isValid(conversion);
        }, inventory, x, y);
    }

    private InfusionInventorySlot(IInfusionTank infusionTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(infusionTank, worldSupplier, canExtract, canInsert, validator, inventory, x, y);
    }

    @Nullable
    @Override
    protected IChemicalHandler<InfuseType, InfusionStack> getCapability() {
        return getCapability(current);
    }

    @Nullable
    @Override
    protected Pair<ItemStack, InfusionStack> getConversion() {
        ItemStackToInfuseTypeRecipe foundRecipe = MekanismRecipeType.INFUSION_CONVERSION.findFirst(worldSupplier.get(), recipe -> recipe.getInput().test(current));
        if (foundRecipe != null) {
            ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
            if (!itemInput.isEmpty()) {
                return Pair.of(itemInput, foundRecipe.getOutput(itemInput));
            }
        }
        return null;
    }
}