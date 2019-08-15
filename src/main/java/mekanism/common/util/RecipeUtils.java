package mekanism.common.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.block.interfaces.ISupportsUpgrades;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.machine.factory.ItemBlockFactory;
import mekanism.common.security.ISecurityItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

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

    public static ItemStack getCraftingResult(CraftingInventory inv, ItemStack toReturn) {
        int invLength = inv.getSizeInventory();
        if (toReturn.getItem() instanceof IEnergizedItem) {
            double energyFound = 0;
            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (!itemstack.isEmpty() && itemstack.getItem() instanceof IEnergizedItem) {
                    energyFound += ((IEnergizedItem) itemstack.getItem()).getEnergy(itemstack);
                }
            }
            double energyToSet = Math.min(((IEnergizedItem) toReturn.getItem()).getMaxEnergy(toReturn), energyFound);
            if (energyToSet > 0) {
                ((IEnergizedItem) toReturn.getItem()).setEnergy(toReturn, energyToSet);
            }
        }

        if (toReturn.getItem() instanceof IGasItem) {
            GasStack gasFound = null;
            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (!itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem) {
                    GasStack stored = ((IGasItem) itemstack.getItem()).getGas(itemstack);
                    if (stored != null) {
                        if (!((IGasItem) toReturn.getItem()).canReceiveGas(toReturn, stored.getGas())) {
                            return ItemStack.EMPTY;
                        }
                        if (gasFound == null) {
                            gasFound = stored;
                        } else {
                            if (gasFound.getGas() != stored.getGas()) {
                                return ItemStack.EMPTY;
                            }
                            gasFound.amount += stored.amount;
                        }
                    }
                }
            }

            if (gasFound != null) {
                gasFound.amount = Math.min(((IGasItem) toReturn.getItem()).getMaxGas(toReturn), gasFound.amount);
                ((IGasItem) toReturn.getItem()).setGas(toReturn, gasFound);
            }
        }

        if (toReturn.getItem() instanceof ISecurityItem) {
            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (!itemstack.isEmpty() && itemstack.getItem() instanceof ISecurityItem) {
                    ((ISecurityItem) toReturn.getItem()).setOwnerUUID(toReturn, ((ISecurityItem) itemstack.getItem()).getOwnerUUID(itemstack));
                    ((ISecurityItem) toReturn.getItem()).setSecurity(toReturn, ((ISecurityItem) itemstack.getItem()).getSecurity(itemstack));
                    break;
                }
            }
        }

        if (FluidContainerUtils.isFluidContainer(toReturn)) {
            FluidStack fluidFound = null;
            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (FluidContainerUtils.isFluidContainer(itemstack)) {
                    LazyOptionalHelper<FluidStack> fluidStackHelper = new LazyOptionalHelper<>(FluidUtil.getFluidContained(itemstack));
                    if (fluidStackHelper.isPresent()) {
                        FluidStack stored = fluidStackHelper.getValue();
                        if (new LazyOptionalHelper<>(FluidUtil.getFluidHandler(itemstack)).matches(handler -> handler.fill(stored, false) == 0)) {
                            return ItemStack.EMPTY;
                        }
                        if (fluidFound == null) {
                            fluidFound = stored;
                        } else {
                            if (fluidFound.getFluid() != stored.getFluid()) {
                                return ItemStack.EMPTY;
                            }
                            fluidFound.amount += stored.amount;
                        }
                    }
                }
            }

            if (fluidFound != null) {
                FluidStack finalFluidFound = fluidFound;
                FluidUtil.getFluidHandler(toReturn).ifPresent(handler -> handler.fill(finalFluidFound, true));
            }
        }

        if (!toReturn.isEmpty() && toReturn.getItem() instanceof ItemBlockBin) {
            int foundCount = 0;
            ItemStack foundType = ItemStack.EMPTY;
            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (!itemstack.isEmpty() && itemstack.getItem() instanceof ItemBlockBin) {
                    InventoryBin binInv = new InventoryBin(itemstack);
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

        if (ISupportsUpgrades.isInstance(toReturn)) {
            Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);
                if (ISupportsUpgrades.isInstance(itemstack)) {
                    Map<Upgrade, Integer> stackMap = Upgrade.buildMap(ItemDataUtils.getDataMapIfPresent(itemstack));
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
    }

    public static IRecipe getRecipeFromGrid(CraftingInventory inv, World world) {
        return CraftingManager.findMatchingRecipe(inv, world);
    }
}