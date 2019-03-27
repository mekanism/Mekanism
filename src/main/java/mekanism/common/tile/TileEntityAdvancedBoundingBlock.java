package mekanism.common.tile;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.Coord4D;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.Mekanism;
import mekanism.common.base.IAdvancedBoundingBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
      @Interface(iface = "cofh.redstoneflux.api.IEnergyProvider", modid = MekanismHooks.REDSTONEFLUX_MOD_ID),
      @Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = MekanismHooks.REDSTONEFLUX_MOD_ID),
      @Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = MekanismHooks.IC2_MOD_ID)
})
public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock implements ISidedInventory, IEnergySink,
      IStrictEnergyAcceptor, IEnergyReceiver, IEnergyProvider, IComputerIntegration, ISpecialConfigData {

    @Override
    public boolean isEmpty() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return true;
        }

        return inv.isEmpty();
    }

    @Override
    public int getSizeInventory() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return 0;
        }

        return inv.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return ItemStack.EMPTY;
        }

        return inv.getStackInSlot(i);
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return ItemStack.EMPTY;
        }

        return inv.decrStackSize(i, j);
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return ItemStack.EMPTY;
        }

        return inv.removeStackFromSlot(i);
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return;
        }

        inv.setInventorySlotContents(i, itemstack);
    }

    @Override
    public String getName() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return "null";
        }

        return inv.getName();
    }

    @Override
    public boolean hasCustomName() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return false;
        }

        return inv.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public int getInventoryStackLimit() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return 0;
        }

        return inv.getInventoryStackLimit();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return false;
        }

        return inv.isUsableByPlayer(entityplayer);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return;
        }

        inv.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return;
        }

        inv.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return false;
        }

        return inv.canBoundInsert(getPos(), i, itemstack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return InventoryUtils.EMPTY;
        }

        return inv.getBoundSlots(getPos(), side);
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing side) {
        return isItemValidForSlot(i, itemstack);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing side) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return false;
        }

        return inv.canBoundExtract(getPos(), i, itemstack, side);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return false;
        }

        return inv.acceptsEnergyFrom(emitter, direction);
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null || !canReceiveEnergy(from)) {
            return 0;
        }

        return inv.receiveEnergy(from, maxReceive, simulate);
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return 0;
        }

        return inv.extractEnergy(from, maxExtract, simulate);
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public boolean canConnectEnergy(EnumFacing from) {
        return canReceiveEnergy(from);
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getEnergyStored(EnumFacing from) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return 0;
        }

        return inv.getEnergyStored(from);
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getMaxEnergyStored(EnumFacing from) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return 0;
        }

        return inv.getMaxEnergyStored(from);
    }

    @Override
    public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null || !canReceiveEnergy(side)) {
            return 0;
        }

        return inv.acceptEnergy(side, amount, simulate);
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return false;
        }

        return inv.canBoundReceiveEnergy(getPos(), side);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getDemandedEnergy() {
        IAdvancedBoundingBlock inv = getInv();
        return inv == null ? 0 : inv.getDemandedEnergy();
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null || !canReceiveEnergy(directionFrom)) {
            return amount;
        }

        return inv.injectEnergy(directionFrom, amount, voltage);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getSinkTier() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return 0;
        }

        return inv.getSinkTier();
    }

    public IAdvancedBoundingBlock getInv() {
        if (!receivedCoords || !world.isBlockLoaded(mainPos)) {
            return null;
        }

        TileEntity tile = new Coord4D(mainPos, world).getTileEntity(world);

        if (tile == null) {
            return null;
        }

        if (!(tile instanceof IAdvancedBoundingBlock)) {
            Mekanism.logger
                  .error("Found tile {} instead of an IAdvancedBoundingBlock, at {}. Multiblock cannot function", tile,
                        mainPos);
            //world.setBlockToAir(mainPos);
            return null;
        }

        return (IAdvancedBoundingBlock) tile;
    }

    @Override
    public void onPower() {
        super.onPower();

        IAdvancedBoundingBlock inv = getInv();
        if (inv != null) {
            inv.onPower();
        }
    }

    @Override
    public void onNoPower() {
        super.onNoPower();

        IAdvancedBoundingBlock inv = getInv();
        if (inv != null) {
            inv.onNoPower();
        }
    }

    @Override
    public String[] getMethods() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return new String[]{};
        }

        return inv.getMethods();
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return new Object[]{};
        }

        return inv.invoke(method, arguments);
    }

    @Override
    public NBTTagCompound getConfigurationData(NBTTagCompound nbtTags) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return new NBTTagCompound();
        }

        return inv.getConfigurationData(nbtTags);
    }

    @Override
    public void setConfigurationData(NBTTagCompound nbtTags) {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return;
        }

        inv.setConfigurationData(nbtTags);
    }

    @Override
    public String getDataType() {
        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return "null";
        }

        return inv.getDataType();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return super.hasCapability(capability, facing);
        }

        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return super.hasCapability(capability, facing);
        }

        return inv.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return super.getCapability(capability, facing);
        }

        IAdvancedBoundingBlock inv = getInv();
        if (inv == null) {
            return super.getCapability(capability, facing);
        }

        return inv.getCapability(capability, facing);
    }
}
