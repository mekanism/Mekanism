package mekanism.common.util;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.security.ISecurityItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeUtils {

    public static boolean areItemsEqualForCrafting(ItemStack target, ItemStack input) {
        if (target.isEmpty() && !input.isEmpty() || !target.isEmpty() && input.isEmpty()) {
            return false;
        } else if (target.isEmpty()) {
            return true;
        }

        if (target.getItem() != input.getItem()) {
            return false;
        }

        if (target.getItemDamage() != input.getItemDamage() && target.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            return false;
        }

        if (target.getItem() instanceof ITierItem && input.getItem() instanceof ITierItem) {
            if (((ITierItem) target.getItem()).getBaseTier(target) != ((ITierItem) input.getItem())
                  .getBaseTier(input)) {
                return false;
            }
        }

        if (target.getItem() instanceof IFactory && input.getItem() instanceof IFactory) {
            if (isFactory(target) && isFactory(input)) {
                return ((IFactory) target.getItem()).getRecipeType(target) == ((IFactory) input.getItem())
                      .getRecipeType(input);
            }
        }

        return true;
    }

    private static boolean isFactory(ItemStack stack) {
        return BlockStateMachine.MachineType.get(stack) == BlockStateMachine.MachineType.BASIC_FACTORY
              || BlockStateMachine.MachineType.get(stack) == BlockStateMachine.MachineType.ADVANCED_FACTORY
              || BlockStateMachine.MachineType.get(stack) == BlockStateMachine.MachineType.ELITE_FACTORY;
    }

    public static ItemStack getCraftingResult(InventoryCrafting inv, ItemStack toReturn) {
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
                    ((ISecurityItem) toReturn.getItem())
                          .setOwnerUUID(toReturn, ((ISecurityItem) itemstack.getItem()).getOwnerUUID(itemstack));
                    ((ISecurityItem) toReturn.getItem())
                          .setSecurity(toReturn, ((ISecurityItem) itemstack.getItem()).getSecurity(itemstack));

                    break;
                }
            }
        }

        if (FluidContainerUtils.isFluidContainer(toReturn)) {
            FluidStack fluidFound = null;

            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);

                if (FluidContainerUtils.isFluidContainer(itemstack)) {
                    FluidStack stored = FluidUtil.getFluidContained(itemstack);

                    if (stored != null) {
                        if (FluidUtil.getFluidHandler(itemstack).fill(stored, false) == 0) {
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
                FluidUtil.getFluidHandler(toReturn).fill(fluidFound, true);
            }
        }

        if (BasicBlockType.get(toReturn) == BasicBlockType.BIN) {
            int foundCount = 0;
            ItemStack foundType = ItemStack.EMPTY;

            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);

                if (!itemstack.isEmpty() && BasicBlockType.get(itemstack) == BasicBlockType.BIN) {
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

        if (BlockStateMachine.MachineType.get(toReturn) != null && BlockStateMachine.MachineType
              .get(toReturn).supportsUpgrades) {
            Map<Upgrade, Integer> upgrades = new HashMap<>();

            for (int i = 0; i < invLength; i++) {
                ItemStack itemstack = inv.getStackInSlot(i);

                if (!itemstack.isEmpty() && BlockStateMachine.MachineType.get(itemstack) != null
                      && BlockStateMachine.MachineType.get(itemstack).supportsUpgrades) {
                    Map<Upgrade, Integer> stackMap = Upgrade.buildMap(ItemDataUtils.getDataMapIfPresent(itemstack));

                    for (Map.Entry<Upgrade, Integer> entry : stackMap.entrySet()) {
                        if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                            Integer val = upgrades.get(entry.getKey());

                            upgrades.put(entry.getKey(),
                                  Math.min(entry.getKey().getMax(), (val != null ? val : 0) + entry.getValue()));
                        }
                    }
                }
            }

            Upgrade.saveMap(upgrades, ItemDataUtils.getDataMap(toReturn));
        }

        return toReturn;
    }

    public static IRecipe getRecipeFromGrid(InventoryCrafting inv, World world) {
        return CraftingManager.findMatchingRecipe(inv, world);
    }
}
