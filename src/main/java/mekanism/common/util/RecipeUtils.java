package mekanism.common.util;

import mekanism.api.block.ISupportsUpgrades;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.item.block.machine.factory.ItemBlockFactory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;

public class RecipeUtils {

    public static boolean areItemsEqualForCrafting(ItemStack target, ItemStack input) {
        if (target.isEmpty() && !input.isEmpty() || !target.isEmpty() && input.isEmpty()) {
            return false;
        } else if (target.isEmpty()) {
            return true;
        }
        //TODO: Should damage even be checked here
        if (target.getItem() != input.getItem() || target.getDamage() != input.getDamage()) {
            return false;
        }

        if (target.getItem() instanceof IFactory && input.getItem() instanceof IFactory) {
            if (isFactory(target) && isFactory(input)) {
                RecipeType recipeTypeInput = ((IFactory) input.getItem()).getRecipeTypeOrNull(input);
                //If either factory has invalid NBT don't crash it
                return recipeTypeInput != null && ((IFactory) target.getItem()).getRecipeTypeOrNull(target) == recipeTypeInput;
            }
        }
        return true;
    }

    private static boolean isFactory(ItemStack stack) {
        return stack.getItem() instanceof ItemBlockFactory;
    }

    /*public static ItemStack getCraftingResult(CraftingInventory inv, ItemStack toReturn) {
        int invLength = inv.getSizeInventory();
        if (toReturn.getItem() instanceof IEnergizedItem) {
            double energyFound = 0;
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IEnergizedItem) {
                    energyFound += ((IEnergizedItem) stack.getItem()).getEnergy(stack);
                }
            }
            double energyToSet = Math.min(((IEnergizedItem) toReturn.getItem()).getMaxEnergy(toReturn), energyFound);
            if (energyToSet > 0) {
                ((IEnergizedItem) toReturn.getItem()).setEnergy(toReturn, energyToSet);
            }
        }

        if (toReturn.getItem() instanceof IGasItem) {
            GasStack gasFound = GasStack.EMPTY;
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IGasItem) {
                    GasStack stored = ((IGasItem) stack.getItem()).getGas(stack);
                    if (!stored.isEmpty()) {
                        if (!((IGasItem) toReturn.getItem()).canReceiveGas(toReturn, stored.getType())) {
                            return ItemStack.EMPTY;
                        }
                        if (gasFound.isEmpty()) {
                            gasFound = stored;
                        } else {
                            if (!gasFound.isTypeEqual(stored)) {
                                return ItemStack.EMPTY;
                            }
                            gasFound.grow(stored.getAmount());
                        }
                    }
                }
            }

            if (!gasFound.isEmpty()) {
                gasFound.setAmount(Math.min(((IGasItem) toReturn.getItem()).getMaxGas(toReturn), gasFound.getAmount()));
                ((IGasItem) toReturn.getItem()).setGas(toReturn, gasFound);
            }
        }

        if (toReturn.getItem() instanceof ISecurityItem) {
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ISecurityItem) {
                    ((ISecurityItem) toReturn.getItem()).setOwnerUUID(toReturn, ((ISecurityItem) stack.getItem()).getOwnerUUID(stack));
                    ((ISecurityItem) toReturn.getItem()).setSecurity(toReturn, ((ISecurityItem) stack.getItem()).getSecurity(stack));
                    break;
                }
            }
        }

        if (FluidContainerUtils.isFluidContainer(toReturn)) {
            FluidStack fluidFound = FluidStack.EMPTY;
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (FluidContainerUtils.isFluidContainer(stack)) {
                    FluidStack stored = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
                    if (!stored.isEmpty()) {
                        if (new LazyOptionalHelper<>(FluidUtil.getFluidHandler(stack)).matches(handler -> handler.fill(stored, FluidAction.SIMULATE) == 0)) {
                            return ItemStack.EMPTY;
                        }
                        if (fluidFound.isEmpty()) {
                            fluidFound = stored;
                        } else {
                            if (fluidFound.getFluid() != stored.getFluid()) {
                                return ItemStack.EMPTY;
                            }
                            fluidFound.setAmount(fluidFound.getAmount() + stored.getAmount());
                        }
                    }
                }
            }

            if (!fluidFound.isEmpty()) {
                FluidStack finalFluidFound = fluidFound;
                FluidUtil.getFluidHandler(toReturn).ifPresent(handler -> handler.fill(finalFluidFound, FluidAction.EXECUTE));
            }
        }

        if (!toReturn.isEmpty() && toReturn.getItem() instanceof ItemBlockBin) {
            int foundCount = 0;
            ItemStack foundType = ItemStack.EMPTY;
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemBlockBin) {
                    InventoryBin binInv = new InventoryBin(stack);
                    foundCount = binInv.getItemCount();
                    foundType = binInv.getItemType();
                }
            }

            if (foundCount > 0 && !foundType.isEmpty()) {
                InventoryBin binInv = new InventoryBin(toReturn);
                binInv.setItemCount(foundCount);
                binInv.setItemType(foundType);
            }
        }

        if (supportsUpgrades(toReturn)) {
            Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (supportsUpgrades(stack)) {
                    Map<Upgrade, Integer> stackMap = Upgrade.buildMap(ItemDataUtils.getDataMapIfPresent(stack));
                    for (Entry<Upgrade, Integer> entry : stackMap.entrySet()) {
                        if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                            Integer val = upgrades.get(entry.getKey());
                            upgrades.put(entry.getKey(), Math.min(entry.getKey().getMax(), (val != null ? val : 0) + entry.getValue()));
                        }
                    }
                }
            }
            Upgrade.saveMap(upgrades, ItemDataUtils.getDataMap(toReturn));
        }
        return toReturn;
    }*/

    private static boolean supportsUpgrades(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ISupportsUpgrades;
    }

    public static ICraftingRecipe getRecipeFromGrid(CraftingInventory inv, World world) {
        return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world).get();
    }
}