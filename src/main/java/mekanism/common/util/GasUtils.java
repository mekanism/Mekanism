package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismFluids;
import mekanism.common.OreDictCache;
import mekanism.common.base.target.GasHandlerTarget;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.GasTankTier;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * A handy class containing several utilities for efficient gas transfer.
 *
 * @author AidanBrady
 */
public final class GasUtils {

    public static IGasHandler[] getConnectedAcceptors(BlockPos pos, World world, Set<EnumFacing> sides) {
        IGasHandler[] acceptors = new IGasHandler[]{null, null, null, null, null, null};
        for (EnumFacing orientation : sides) {
            TileEntity acceptor = world.getTileEntity(pos.offset(orientation));
            if (CapabilityUtils.hasCapability(acceptor, Capabilities.GAS_HANDLER_CAPABILITY, orientation.getOpposite())) {
                acceptors[orientation.ordinal()] = CapabilityUtils.getCapability(acceptor, Capabilities.GAS_HANDLER_CAPABILITY, orientation.getOpposite());
            }
        }
        return acceptors;
    }

    /**
     * Gets all the acceptors around a tile entity.
     *
     * @return array of IGasAcceptors
     */
    public static IGasHandler[] getConnectedAcceptors(BlockPos pos, World world) {
        return getConnectedAcceptors(pos, world, EnumSet.allOf(EnumFacing.class));
    }

    public static boolean isValidAcceptorOnSide(TileEntity tile, EnumFacing side) {
        if (CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite())) {
            return false;
        }
        return CapabilityUtils.hasCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite());
    }

    /**
     * Removes a specified amount of gas from an IGasItem.
     *
     * @param itemStack - ItemStack of the IGasItem
     * @param type      - type of gas to remove from the IGasItem, null if it doesn't matter
     * @param amount    - amount of gas to remove from the ItemStack
     *
     * @return the GasStack removed by the IGasItem
     */
    public static GasStack removeGas(ItemStack itemStack, Gas type, int amount) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            if (type != null && item.getGas(itemStack) != null && item.getGas(itemStack).getGas() != type || !item.canProvideGas(itemStack, type)) {
                return null;
            }
            return item.removeGas(itemStack, amount);
        }
        return null;
    }

    /**
     * Adds a specified amount of gas to an IGasItem.
     *
     * @param itemStack - ItemStack of the IGasItem
     * @param stack     - stack to add to the IGasItem
     *
     * @return amount of gas accepted by the IGasItem
     */
    public static int addGas(ItemStack itemStack, GasStack stack) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof IGasItem && ((IGasItem) itemStack.getItem()).canReceiveGas(itemStack, stack.getGas())) {
            return ((IGasItem) itemStack.getItem()).addGas(itemStack, stack.copy());
        }
        return 0;
    }

    /**
     * Emits gas from a central block by splitting the received stack among the sides given.
     *
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     * @param sides - the list of sides to output from
     *
     * @return the amount of gas emitted
     */
    public static int emit(GasStack stack, TileEntity from, Set<EnumFacing> sides) {
        if (stack == null || stack.amount == 0) {
            return 0;
        }

        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        GasHandlerTarget target = new GasHandlerTarget(stack);
        for (EnumFacing orientation : sides) {
            TileEntity acceptor = from.getWorld().getTileEntity(from.getPos().offset(orientation));
            if (acceptor == null) {
                continue;
            }
            EnumFacing opposite = orientation.getOpposite();
            if (CapabilityUtils.hasCapability(acceptor, Capabilities.GAS_HANDLER_CAPABILITY, opposite)) {
                IGasHandler handler = CapabilityUtils.getCapability(acceptor, Capabilities.GAS_HANDLER_CAPABILITY, opposite);
                if (handler != null && handler.canReceiveGas(opposite, stack.getGas())) {
                    target.addHandler(opposite, handler);
                }
            }
        }
        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<GasHandlerTarget> targets = new HashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.amount, stack);
        }
        return 0;
    }

    public static void writeSustainedData(GasTank gasTank, ItemStack itemStack) {
        if (gasTank.stored != null && gasTank.stored.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "gasStored", gasTank.stored.write(new NBTTagCompound()));
        }
    }

    public static void readSustainedData(GasTank gasTank, ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "gasStored")) {
            gasTank.stored = GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasStored"));
        } else {
            gasTank.stored = null;
        }
    }

    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     *
     * @param itemStack - itemstack to check with
     *
     * @return fuel ticks
     */
    public static GasStack getItemGas(ItemStack itemStack, BiFunction<Gas, Integer, GasStack> getIfValid) {
        //TODO: Allow them to register other items that get converted to gas
        GasStack gasStack = null;
        if (itemStack.isItemEqual(new ItemStack(Items.FLINT))) {
            gasStack = getIfValid.apply(MekanismFluids.Oxygen, 10);
            if (gasStack != null) {
                return gasStack;
            }
        }
        List<String> oreDictNames = OreDictCache.getOreDictName(itemStack);
        if (oreDictNames.contains("dustSulfur")) {
            gasStack = getIfValid.apply(MekanismFluids.SulfuricAcid, 2);
            if (gasStack != null) {
                return gasStack;
            }
        }
        if (oreDictNames.contains("dustSalt")) {
            gasStack = getIfValid.apply(MekanismFluids.HydrogenChloride, 2);
            if (gasStack != null) {
                return gasStack;
            }
        }
        if (oreDictNames.contains("ingotOsmium")) {
            gasStack = getIfValid.apply(MekanismFluids.LiquidOsmium, 200);
            if (gasStack != null) {
                return gasStack;
            }
        }
        OreDictionary.getOres("blockOsmium");
        if (oreDictNames.contains("blockOsmium")) {
            gasStack = getIfValid.apply(MekanismFluids.LiquidOsmium, 1800);
            if (gasStack != null) {
                return gasStack;
            }
        }
        if (itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            GasStack gas = item.getGas(itemStack);
            //Check to make sure it can provide the gas it contains
            if (gas != null && item.canProvideGas(itemStack, gas.getGas())) {
                gasStack = getIfValid.apply(gas.getGas(), 1);
                if (gasStack != null) {
                    return gasStack;
                }
            }
        }
        return gasStack;
    }

    //TODO: Have item to gas and gas to items be registered in a lookup registry
    public static List<ItemStack> getStacksForGas(Gas type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        if (type == MekanismFluids.SulfuricAcid) {
            stacks.addAll(OreDictionary.getOres("dustSulfur"));
        }
        if (type == MekanismFluids.HydrogenChloride) {
            stacks.addAll(OreDictionary.getOres("dustSalt"));
        }
        if (type == MekanismFluids.LiquidOsmium) {
            stacks.addAll(OreDictionary.getOres("ingotOsmium"));
            stacks.addAll(OreDictionary.getOres("blockOsmium"));
        }
        if (type == MekanismFluids.Oxygen) {
            stacks.add(new ItemStack(Items.FLINT));
        }
        stacks.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, type));
        return stacks;
    }
}