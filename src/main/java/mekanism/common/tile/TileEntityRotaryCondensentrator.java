package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileEntityRotaryCondensentrator extends TileEntityMekanism implements ISustainedData, IFluidHandlerWrapper, IGasHandler, ITankManager, IComparatorSupport {

    public static final int MAX_FLUID = 10000;
    public GasTank gasTank = new GasTank(MAX_FLUID);
    public FluidTank fluidTank = new FluidTank(MAX_FLUID);
    /**
     * 0: gas -> fluid; 1: fluid -> gas
     */
    public int mode;

    public int gasOutput = 256;

    public double clientEnergyUsed;
    private int currentRedstoneLevel;

    private GasInventorySlot gasInputSlot;
    private OutputInventorySlot gasOutputSlot;
    private FluidInventorySlot fluidInputSlot;
    private OutputInventorySlot fluidOutputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityRotaryCondensentrator() {
        super(MekanismBlock.ROTARY_CONDENSENTRATOR);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //TODO: Add in checks once we switch it to a recipe system for if the gas/fluid is ever valid
        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        builder.addSlot(gasInputSlot = GasInventorySlot.rotary(gasTank, gas -> true, () -> mode == 0, this, 5, 25), RelativeSide.LEFT);
        builder.addSlot(gasOutputSlot = OutputInventorySlot.at(this, 5, 56), RelativeSide.LEFT);
        builder.addSlot(fluidInputSlot = FluidInventorySlot.rotary(fluidTank, fluid -> true, () -> mode == 1, this, 155, 25), RelativeSide.RIGHT);
        builder.addSlot(fluidOutputSlot = OutputInventorySlot.at(this, 155, 56), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 155, 5), RelativeSide.FRONT, RelativeSide.BACK, RelativeSide.BOTTOM, RelativeSide.TOP);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            ChargeUtils.discharge(energySlot.getStack(), this);

            if (mode == 0) {
                TileUtils.receiveGas(gasOutputSlot.getStack(), gasTank);
                if (FluidContainerUtils.isFluidContainer(fluidInputSlot.getStack())) {
                    FluidContainerUtils.handleContainerItemFill(this, fluidTank, fluidInputSlot, fluidOutputSlot);
                }

                //TODO: Promote this stuff to being a proper RECIPE (at the very least in 1.14)
                if (getEnergy() >= getEnergyPerTick() && MekanismUtils.canFunction(this) && isValidGas(gasTank.getStack()) &&
                    (fluidTank.getFluid().isEmpty() || (fluidTank.getFluid().getAmount() < MAX_FLUID && gasEquals(gasTank.getStack(), fluidTank.getFluid())))) {
                    int operations = getUpgradedUsage();
                    double prev = getEnergy();

                    setActive(true);
                    fluidTank.fill(new FluidStack(gasTank.getType().getFluid(), operations), FluidAction.EXECUTE);
                    gasTank.drain(operations, Action.EXECUTE);
                    setEnergy(getEnergy() - getEnergyPerTick() * operations);
                    clientEnergyUsed = prev - getEnergy();
                } else {
                    setActive(false);
                }
            } else if (mode == 1) {
                TileUtils.drawGas(gasInputSlot.getStack(), gasTank);
                TileUtils.emitGas(this, gasTank, gasOutput, getLeftSide());

                if (FluidContainerUtils.isFluidContainer(fluidInputSlot.getStack())) {
                    FluidContainerUtils.handleContainerItemEmpty(this, fluidTank, fluidInputSlot, fluidOutputSlot);
                }

                //TODO: Promote this stuff to being a proper RECIPE (at the very least in 1.14)
                if (getEnergy() >= getEnergyPerTick() && MekanismUtils.canFunction(this) && isValidFluid(fluidTank.getFluid()) &&
                    (gasTank.isEmpty() || (gasTank.getStored() < MAX_FLUID && gasEquals(gasTank.getStack(), fluidTank.getFluid())))) {
                    int operations = getUpgradedUsage();
                    double prev = getEnergy();

                    setActive(true);
                    //TODO: Recipe system instead of this
                    Gas value = Gas.getFromRegistry(fluidTank.getFluid().getFluid().getRegistryName());
                    if (!value.isEmptyType()) {
                        gasTank.fill(new GasStack(value, operations), Action.EXECUTE);
                        fluidTank.drain(operations, FluidAction.EXECUTE);
                        setEnergy(getEnergy() - getEnergyPerTick() * operations);
                        clientEnergyUsed = prev - getEnergy();
                    }
                } else {
                    setActive(false);
                }
            }
            World world = getWorld();
            if (world != null) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    public int getUpgradedUsage() {
        int possibleProcess = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        if (mode == 0) { //Gas to fluid
            possibleProcess = Math.min(Math.min(gasTank.getStored(), fluidTank.getCapacity() - fluidTank.getFluidAmount()), possibleProcess);
        } else { //Fluid to gas
            possibleProcess = Math.min(Math.min(fluidTank.getFluidAmount(), gasTank.getNeeded()), possibleProcess);
        }
        possibleProcess = Math.min((int) (getEnergy() / getEnergyPerTick()), possibleProcess);
        return possibleProcess;
    }

    public boolean isValidGas(@Nonnull GasStack g) {
        return !g.isEmpty() && g.getType().hasFluid();

    }

    public boolean gasEquals(@Nonnull GasStack gas, @Nonnull FluidStack fluid) {
        return !fluid.isEmpty() && !gas.isEmpty() && gas.getType().hasFluid() && gas.getType().getFluid() == fluid.getFluid();
    }

    public boolean isValidFluid(@Nonnull Fluid f) {
        return !Gas.getFromRegistry(f.getRegistryName()).isEmptyType();
    }

    public boolean isValidFluid(@Nonnull FluidStack f) {
        return isValidFluid(f.getFluid());
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                mode = mode == 0 ? 1 : 0;
            }
            for (PlayerEntity player : playersUsing) {
                Mekanism.packetHandler.sendTo(new PacketTileEntity(this), (ServerPlayerEntity) player);
            }
            return;
        }

        super.handlePacketData(dataStream);
        if (isRemote()) {
            mode = dataStream.readInt();
            clientEnergyUsed = dataStream.readDouble();
            TileUtils.readTankData(dataStream, fluidTank);
            TileUtils.readTankData(dataStream, gasTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(mode);
        data.add(clientEnergyUsed);
        TileUtils.addTankData(data, fluidTank);
        TileUtils.addTankData(data, gasTank);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        mode = nbtTags.getInt("mode");
        gasTank.read(nbtTags.getCompound("gasTank"));
        if (nbtTags.contains("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompound("fluidTank"));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("mode", mode);
        nbtTags.put("gasTank", gasTank.write(new CompoundNBT()));
        if (!fluidTank.getFluid().isEmpty()) {
            nbtTags.put("fluidTank", fluidTank.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        return gasTank.fill(stack, action);
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        return gasTank.drain(amount, action);
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return mode == 1 && side == getLeftSide() && gasTank.canDraw(type);
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return mode == 0 && side == getLeftSide() && gasTank.canReceive(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && side != getLeftSide();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!fluidTank.getFluid().isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "fluidTank", fluidTank.getFluid().writeToNBT(new CompoundNBT()));
        }
        if (!gasTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "gasTank", gasTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "fluidTank")));
        gasTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasTank")));
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return fluidTank.fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return fluidTank.drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return mode == 1 && from == getLeftSide() && (fluidTank.getFluid().isEmpty() ? isValidFluid(fluid) : fluidTank.getFluid().isFluidEqual(fluid));
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return mode == 0 && from == getRightSide() && FluidContainerUtils.canDrain(fluidTank.getFluid(), fluid);
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (from == getRightSide()) {
            return getAllTanks();
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return new IFluidTank[]{fluidTank};
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{gasTank, fluidTank};
    }

    @Override
    public int getRedstoneLevel() {
        if (mode == 0) {
            return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
        }
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}