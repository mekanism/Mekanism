package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.Upgrade;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityFluidicPlenisher extends TileEntityMekanism implements IComputerIntegration, IConfigurable, IFluidHandlerWrapper, ISustainedTank,
      IUpgradeTile, ISecurityTile, IComparatorSupport {

    private static final String[] methods = new String[]{"reset"};
    private static EnumSet<EnumFacing> dirs = EnumSet.complementOf(EnumSet.of(EnumFacing.UP));
    public Set<Coord4D> activeNodes = new LinkedHashSet<>();
    public Set<Coord4D> usedNodes = new HashSet<>();
    public boolean finishedCalc = false;
    public FluidTank fluidTank = new FluidTank(10000);
    /**
     * How many ticks it takes to run an operation.
     */
    public int BASE_TICKS_REQUIRED = 20;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /**
     * How many ticks this machine has been operating for.
     */
    public int operatingTicks;
    public TileComponentUpgrade<TileEntityFluidicPlenisher> upgradeComponent = new TileComponentUpgrade<>(this, 3);
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    private int currentRedstoneLevel;

    public TileEntityFluidicPlenisher() {
        super(MekanismBlock.FLUIDIC_PLENISHER);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(2, this);
            if (FluidContainerUtils.isFluidContainer(getInventory().get(0))) {
                FluidContainerUtils.handleContainerItemEmpty(this, fluidTank, 0, 1, new FluidChecker() {
                    @Override
                    public boolean isValid(Fluid f) {
                        return f.canBePlacedInWorld();
                    }
                });
            }

            if (MekanismUtils.canFunction(this) && getEnergy() >= getEnergyPerTick() && fluidTank.getFluid() != null && fluidTank.getFluid().getFluid().canBePlacedInWorld()) {
                if (!finishedCalc) {
                    setEnergy(getEnergy() - getEnergyPerTick());
                }
                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    if (!finishedCalc) {
                        doPlenish();
                    } else {
                        Coord4D below = Coord4D.get(this).offset(EnumFacing.DOWN);

                        if (canReplace(below, false, false) && fluidTank.getFluidAmount() >= Fluid.BUCKET_VOLUME) {
                            if (fluidTank.getFluid().getFluid().canBePlacedInWorld()) {
                                world.setBlockState(below.getPos(), MekanismUtils.getFlowingBlock(fluidTank.getFluid().getFluid()).getDefaultState(), 3);
                                setEnergy(getEnergy() - getEnergyPerTick());
                                fluidTank.drain(Fluid.BUCKET_VOLUME, true);
                            }
                        }
                    }
                    operatingTicks = 0;
                }
            }

            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    private void doPlenish() {
        if (usedNodes.size() >= MekanismConfig.current().general.maxPlenisherNodes.val()) {
            finishedCalc = true;
            return;
        }
        if (activeNodes.isEmpty()) {
            if (usedNodes.isEmpty()) {
                Coord4D below = Coord4D.get(this).offset(EnumFacing.DOWN);
                if (!canReplace(below, true, true)) {
                    finishedCalc = true;
                    return;
                }
                activeNodes.add(below);
            } else {
                finishedCalc = true;
                return;
            }
        }

        Set<Coord4D> toRemove = new HashSet<>();
        for (Coord4D coord : activeNodes) {
            if (coord.exists(world)) {
                FluidStack fluid = fluidTank.getFluid();
                if (canReplace(coord, true, false) && fluid != null) {
                    world.setBlockState(coord.getPos(), MekanismUtils.getFlowingBlock(fluid.getFluid()).getDefaultState(), 3);
                    fluidTank.drain(Fluid.BUCKET_VOLUME, true);
                }

                for (EnumFacing dir : dirs) {
                    Coord4D sideCoord = coord.offset(dir);
                    if (sideCoord.exists(world) && canReplace(sideCoord, true, true)) {
                        activeNodes.add(sideCoord);
                    }
                }
                toRemove.add(coord);
                break;
            } else {
                toRemove.add(coord);
            }
        }
        usedNodes.addAll(toRemove);
        activeNodes.removeAll(toRemove);
    }

    public boolean canReplace(Coord4D coord, boolean checkNodes, boolean isPathfinding) {
        if (checkNodes && usedNodes.contains(coord)) {
            return false;
        }
        if (coord.isAirBlock(world) || MekanismUtils.isDeadFluid(world, coord)) {
            return true;
        }
        if (MekanismUtils.isFluid(world, coord)) {
            return isPathfinding;
        }
        return coord.getBlock(world).isReplaceable(world, coord.getPos());
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            finishedCalc = dataStream.readBoolean();
            TileUtils.readTankData(dataStream, fluidTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(finishedCalc);
        TileUtils.addTankData(data, fluidTank);
        return data;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setBoolean("finishedCalc", finishedCalc);

        if (fluidTank.getFluid() != null) {
            nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
        }

        NBTTagList activeList = new NBTTagList();
        for (Coord4D wrapper : activeNodes) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            wrapper.write(tagCompound);
            activeList.appendTag(tagCompound);
        }
        if (activeList.tagCount() != 0) {
            nbtTags.setTag("activeNodes", activeList);
        }

        NBTTagList usedList = new NBTTagList();
        for (Coord4D obj : usedNodes) {
            activeList.appendTag(obj.write(new NBTTagCompound()));
        }
        if (activeList.tagCount() != 0) {
            nbtTags.setTag("usedNodes", usedList);
        }
        return nbtTags;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        operatingTicks = nbtTags.getInteger("operatingTicks");
        finishedCalc = nbtTags.getBoolean("finishedCalc");

        if (nbtTags.hasKey("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
        }

        if (nbtTags.hasKey("activeNodes")) {
            NBTTagList tagList = nbtTags.getTagList("activeNodes", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                activeNodes.add(Coord4D.read(tagList.getCompoundTagAt(i)));
            }
        }
        if (nbtTags.hasKey("usedNodes")) {
            NBTTagList tagList = nbtTags.getTagList("usedNodes", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                usedNodes.add(Coord4D.read(tagList.getCompoundTagAt(i)));
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 1) {
            return false;
        } else if (slotID == 0) {
            FluidStack fluidContained = FluidUtil.getFluidContained(itemstack);
            return fluidContained != null && fluidContained.getFluid().canBePlacedInWorld();
        } else if (slotID == 2) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 2) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return slotID == 1;
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side) {
        return getOppositeDirection() == side;
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == EnumFacing.UP) {
            return new int[]{0};
        } else if (side == EnumFacing.DOWN) {
            return new int[]{1};
        }
        return new int[]{2};
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing direction) {
        if (direction == EnumFacing.UP) {
            return new FluidTankInfo[]{fluidTank.getInfo()};
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return getTankInfo(EnumFacing.UP);
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
    public int fill(EnumFacing from, @Nonnull FluidStack resource, boolean doFill) {
        return fluidTank.fill(resource, doFill);
    }

    @Override
    public boolean canFill(EnumFacing from, @Nonnull FluidStack fluid) {
        return from == EnumFacing.UP && fluid.getFluid().canBePlacedInWorld();
    }

    @Override
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side) {
        activeNodes.clear();
        usedNodes.clear();
        finishedCalc = false;
        player.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY + LangUtils.localize("tooltip.configurator.plenisherReset")));
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side) {
        return EnumActionResult.PASS;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.CONFIGURABLE_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.cast(this);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (method == 0) {
            activeNodes.clear();
            usedNodes.clear();
            finishedCalc = false;
            return new Object[]{"Plenisher calculation reset."};
        }
        throw new NoSuchMethodException();
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
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
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }
}