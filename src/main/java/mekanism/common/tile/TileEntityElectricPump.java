package mekanism.common.tile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismFluids;
import mekanism.common.Upgrade;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityElectricPump extends TileEntityMekanism implements IFluidHandlerWrapper, ISustainedTank, IConfigurable, IUpgradeTile, ITankManager,
      IComputerIntegration, IComparatorSupport {

    private static final int[] UPSLOTS = {0};
    private static final int[] DOWNSLOTS = {1};
    private static final int[] SIDESLOTS = {2};

    private static final String[] methods = new String[]{"reset"};
    /**
     * This pump's tank
     */
    public FluidTank fluidTank = new FluidTank(10000);
    /**
     * The type of fluid this pump is pumping
     */
    public Fluid activeType;
    public boolean suckedLastOperation;
    /**
     * How many ticks it takes to run an operation.
     */
    public int BASE_TICKS_REQUIRED = 20;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /**
     * How many ticks this machine has been operating for.
     */
    public int operatingTicks;
    /**
     * The nodes that have full sources near them or in them
     */
    public Set<Coord4D> recurringNodes = new HashSet<>();
    public TileComponentUpgrade<TileEntityElectricPump> upgradeComponent = new TileComponentUpgrade<>(this, 3);

    private int currentRedstoneLevel;

    public TileEntityElectricPump() {
        super(MekanismBlock.ELECTRIC_PUMP);
        upgradeComponent.setSupported(Upgrade.FILTER);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(2, this);
            if (fluidTank.getFluid() != null) {
                if (FluidContainerUtils.isFluidContainer(getInventory().get(0))) {
                    FluidContainerUtils.handleContainerItemFill(this, fluidTank, 0, 1);
                }
            }
            if (MekanismUtils.canFunction(this) && getEnergy() >= getEnergyPerTick()) {
                if (suckedLastOperation) {
                    setEnergy(getEnergy() - getEnergyPerTick());
                }
                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    if (fluidTank.getFluid() == null || fluidTank.getFluid().amount + Fluid.BUCKET_VOLUME <= fluidTank.getCapacity()) {
                        if (!suck(true)) {
                            suckedLastOperation = false;
                            reset();
                        } else {
                            suckedLastOperation = true;
                        }
                    } else {
                        suckedLastOperation = false;
                    }
                    operatingTicks = 0;
                }
            } else {
                suckedLastOperation = false;
            }

            if (fluidTank.getFluid() != null) {
                TileEntity tileEntity = Coord4D.get(this).offset(Direction.UP).getTileEntity(world);
                CapabilityUtils.getCapabilityHelper(tileEntity, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN).ifPresent(handler -> {
                    FluidStack toDrain = new FluidStack(fluidTank.getFluid(), Math.min(256 * (upgradeComponent.getUpgrades(Upgrade.SPEED) + 1), fluidTank.getFluidAmount()));
                    fluidTank.drain(handler.fill(toDrain, true), true);
                });
            }
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    public boolean hasFilter() {
        return upgradeComponent.getInstalledTypes().contains(Upgrade.FILTER);
    }

    public boolean suck(boolean take) {
        List<Coord4D> tempPumpList = Arrays.asList(recurringNodes.toArray(new Coord4D[0]));
        Collections.shuffle(tempPumpList);

        //First see if there are any fluid blocks touching the pump - if so, sucks and adds the location to the recurring list
        for (Direction orientation : Direction.values()) {
            Coord4D wrapper = Coord4D.get(this).offset(orientation);
            FluidStack fluid = MekanismUtils.getFluid(world, wrapper, hasFilter());
            if (fluid != null && (activeType == null || fluid.getFluid() == activeType) && (fluidTank.getFluid() == null || fluidTank.getFluid().isFluidEqual(fluid))) {
                if (take) {
                    activeType = fluid.getFluid();
                    recurringNodes.add(wrapper);
                    fluidTank.fill(fluid, true);
                    if (shouldTake(fluid, wrapper)) {
                        world.removeBlock(wrapper.getPos(), false);
                    }
                }
                return true;
            }
        }

        //Finally, go over the recurring list of nodes and see if there is a fluid block available to suck - if not, will iterate around the recurring block, attempt to suck,
        //and then add the adjacent block to the recurring list
        for (Coord4D wrapper : tempPumpList) {
            FluidStack fluid = MekanismUtils.getFluid(world, wrapper, hasFilter());
            if (fluid != null && (activeType == null || fluid.getFluid() == activeType) && (fluidTank.getFluid() == null || fluidTank.getFluid().isFluidEqual(fluid))) {
                if (take) {
                    activeType = fluid.getFluid();
                    fluidTank.fill(fluid, true);
                    if (shouldTake(fluid, wrapper)) {
                        world.removeBlock(wrapper.getPos(), false);
                    }
                }
                return true;
            }

            //Add all the blocks surrounding this recurring node to the recurring node list
            for (Direction orientation : Direction.values()) {
                Coord4D side = wrapper.offset(orientation);
                if (Coord4D.get(this).distanceTo(side) <= MekanismConfig.general.maxPumpRange.get()) {
                    fluid = MekanismUtils.getFluid(world, side, hasFilter());
                    if (fluid != null && (activeType == null || fluid.getFluid() == activeType) && (fluidTank.getFluid() == null || fluidTank.getFluid().isFluidEqual(fluid))) {
                        if (take) {
                            activeType = fluid.getFluid();
                            recurringNodes.add(side);
                            fluidTank.fill(fluid, true);
                            if (shouldTake(fluid, side)) {
                                world.removeBlock(side.getPos(), false);
                            }
                        }
                        return true;
                    }
                }
            }
            recurringNodes.remove(wrapper);
        }
        return false;
    }

    public void reset() {
        activeType = null;
        recurringNodes.clear();
    }

    private boolean shouldTake(FluidStack fluid, Coord4D coord) {
        if (fluid.getFluid() == FluidRegistry.WATER || fluid.getFluid() == MekanismFluids.HeavyWater) {
            return MekanismConfig.general.pumpWaterSources.get();
        }
        return true;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            TileUtils.readTankData(dataStream, fluidTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, fluidTank);
        return data;
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("operatingTicks", operatingTicks);
        nbtTags.putBoolean("suckedLastOperation", suckedLastOperation);

        if (activeType != null) {
            nbtTags.putString("activeType", FluidRegistry.getFluidName(activeType));
        }

        if (fluidTank.getFluid() != null) {
            nbtTags.put("fluidTank", fluidTank.writeToNBT(new CompoundNBT()));
        }

        ListNBT recurringList = new ListNBT();
        for (Coord4D wrapper : recurringNodes) {
            CompoundNBT tagCompound = new CompoundNBT();
            wrapper.write(tagCompound);
            recurringList.add(tagCompound);
        }
        if (!recurringList.isEmpty()) {
            nbtTags.put("recurringNodes", recurringList);
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        operatingTicks = nbtTags.getInt("operatingTicks");
        suckedLastOperation = nbtTags.getBoolean("suckedLastOperation");
        if (nbtTags.contains("activeType")) {
            activeType = FluidRegistry.getFluid(nbtTags.getString("activeType"));
        }
        if (nbtTags.contains("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompound("fluidTank"));
        }
        if (nbtTags.contains("recurringNodes")) {
            ListNBT tagList = nbtTags.getList("recurringNodes", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                recurringNodes.add(Coord4D.read(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            //Only allow empty fluid containers
            return FluidContainerUtils.isFluidContainer(itemstack) && !FluidUtil.getFluidContained(itemstack).isPresent();
        } else if (slotID == 2) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 2) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return slotID == 1;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return getOppositeDirection() == side;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side == Direction.UP) {
            return UPSLOTS;
        } else if (side == Direction.DOWN) {
            return DOWNSLOTS;
        }
        return SIDESLOTS;
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction direction) {
        if (direction == Direction.UP) {
            return new FluidTankInfo[]{fluidTank.getInfo()};
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return getTankInfo(Direction.UP);
    }

    @Override
    public void setFluidStack(FluidStack fluidStack, Object... data) {
        fluidTank.setFluid(fluidStack);
    }

    @Override
    public FluidStack getFluidStack(Object... data) {
        return fluidTank.getFluid();
    }

    @Override
    public boolean hasTank(Object... data) {
        return true;
    }

    @Override
    @Nullable
    public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
        return fluidTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canDrain(Direction from, @Nullable FluidStack fluid) {
        return from == Direction.byIndex(1) && FluidContainerUtils.canDrain(fluidTank.getFluid(), fluid);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        reset();
        player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY, Translation.of("mekanism.tooltip.configurator.pumpReset")));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{fluidTank};
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (method == 0) {
            reset();
            return new Object[]{"Pump calculation reset."};
        }
        throw new NoSuchMethodException();
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        switch (upgrade) {
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
            case ENERGY:
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
                setMaxEnergy(MekanismUtils.getMaxEnergy(this, getBaseStorage()));
                setEnergy(Math.min(getMaxEnergy(), getEnergy()));
            default:
                break;
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }
}