package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class TileEntityElectricPump extends TileEntityMekanism implements IConfigurable {

    /**
     * How many ticks it takes to run an operation.
     */
    private static final int BASE_TICKS_REQUIRED = 19;
    public static final int HEAVY_WATER_AMOUNT = FluidAttributes.BUCKET_VOLUME / 100;
    /**
     * This pump's tank
     */
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"})
    public BasicFluidTank fluidTank;
    /**
     * The type of fluid this pump is pumping
     */
    @Nonnull
    private FluidStack activeType = FluidStack.EMPTY;
    public int ticksRequired = BASE_TICKS_REQUIRED;
    /**
     * How many ticks this machine has been operating for.
     */
    public int operatingTicks;
    /**
     * The nodes that have full sources near them or in them
     */
    private final Set<BlockPos> recurringNodes = new ObjectOpenHashSet<>();

    private MachineEnergyContainer<TileEntityElectricPump> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private FluidInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityElectricPump() {
        super(MekanismBlocks.ELECTRIC_PUMP);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = BasicFluidTank.output(10_000, this), RelativeSide.TOP);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this), RelativeSide.BACK);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = FluidInventorySlot.drain(fluidTank, this, 28, 20), RelativeSide.TOP);
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 28, 51), RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.drainTank(outputSlot);
        if (MekanismUtils.canFunction(this) && (fluidTank.isEmpty() || FluidAttributes.BUCKET_VOLUME <= fluidTank.getNeeded())) {
            FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                operatingTicks++;
                if (operatingTicks >= ticksRequired) {
                    operatingTicks = 0;
                    if (suck()) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                    } else {
                        reset();
                    }
                }
            }
        }
        if (!fluidTank.isEmpty()) {
            FluidUtils.emit(Collections.singleton(Direction.UP), fluidTank, this, 256 * (1 + upgradeComponent.getUpgrades(Upgrade.SPEED)));
        }
    }

    private boolean suck() {
        boolean hasFilter = upgradeComponent.isUpgradeInstalled(Upgrade.FILTER);
        //First see if there are any fluid blocks touching the pump - if so, sucks and adds the location to the recurring list
        for (Direction orientation : EnumUtils.DIRECTIONS) {
            if (suck(worldPosition.relative(orientation), hasFilter, true)) {
                return true;
            }
        }
        //Even though we can add to recurring in the above for loop, we always then exit and don't get to here if we did so
        List<BlockPos> tempPumpList = new ArrayList<>(recurringNodes);
        Collections.shuffle(tempPumpList);
        //Finally, go over the recurring list of nodes and see if there is a fluid block available to suck - if not, will iterate around the recurring block, attempt to suck,
        //and then add the adjacent block to the recurring list
        for (BlockPos tempPumpPos : tempPumpList) {
            if (suck(tempPumpPos, hasFilter, false)) {
                return true;
            }
            //Add all the blocks surrounding this recurring node to the recurring node list
            for (Direction orientation : EnumUtils.DIRECTIONS) {
                BlockPos side = tempPumpPos.relative(orientation);
                if (WorldUtils.distanceBetween(worldPosition, side) <= MekanismConfig.general.maxPumpRange.get()) {
                    if (suck(side, hasFilter, true)) {
                        return true;
                    }
                }
            }
            recurringNodes.remove(tempPumpPos);
        }
        return false;
    }

    private boolean suck(BlockPos pos, boolean hasFilter, boolean addRecurring) {
        //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
        Optional<BlockState> state = WorldUtils.getBlockState(level, pos);
        if (state.isPresent()) {
            BlockState blockState = state.get();
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) {
                //Just in case someone does weird things and has a fluid state that is empty and a source
                // only allow collecting from non-empty sources
                Block block = blockState.getBlock();
                if (block instanceof IFluidBlock) {
                    IFluidBlock fluidBlock = (IFluidBlock) block;
                    if (validFluid(fluidBlock.drain(level, pos, FluidAction.SIMULATE), true)) {
                        //Actually drain it
                        suck(fluidBlock.drain(level, pos, FluidAction.EXECUTE), pos, addRecurring);
                        return true;
                    }
                } else if (block instanceof IBucketPickupHandler) {
                    Fluid sourceFluid = fluidState.getType();
                    FluidStack fluidStack = getOutput(sourceFluid, hasFilter);
                    if (validFluid(fluidStack, false)) {
                        //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
                        if (sourceFluid != Fluids.WATER || MekanismConfig.general.pumpWaterSources.get()) {
                            //Note we only attempt taking if it is not water, or we want to pump water sources
                            // otherwise we assume the type from the fluid state is correct
                            sourceFluid = ((IBucketPickupHandler) block).takeLiquid(level, pos, blockState);
                            //Update the fluid stack in case something somehow changed about the type
                            // making sure that we replace to heavy water if we got heavy water
                            fluidStack = getOutput(sourceFluid, hasFilter);
                            if (!validFluid(fluidStack, false)) {
                                Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                      fluidState.getType(), pos, level, sourceFluid);
                                return false;
                            }
                        }
                        suck(fluidStack, pos, addRecurring);
                        return true;
                    }
                }
                //Otherwise, we do not know how to drain from the block, or it is not valid, and we shouldn't take it so don't handle it
            }
        }
        return false;
    }

    private FluidStack getOutput(Fluid sourceFluid, boolean hasFilter) {
        if (hasFilter && sourceFluid == Fluids.WATER) {
            return MekanismFluids.HEAVY_WATER.getFluidStack(HEAVY_WATER_AMOUNT);
        }
        return new FluidStack(sourceFluid, FluidAttributes.BUCKET_VOLUME);
    }

    private void suck(@Nonnull FluidStack fluidStack, BlockPos pos, boolean addRecurring) {
        //Size doesn't matter, but we do want to take the NBT into account
        activeType = new FluidStack(fluidStack, 1);
        if (addRecurring) {
            recurringNodes.add(pos);
        }
        fluidTank.insert(fluidStack, Action.EXECUTE, AutomationType.INTERNAL);
    }

    private boolean validFluid(@Nonnull FluidStack fluidStack, boolean recheckSize) {
        if (!fluidStack.isEmpty() && (activeType.isEmpty() || activeType.isFluidEqual(fluidStack))) {
            if (fluidTank.isEmpty()) {
                return true;
            } else if (fluidTank.isFluidEqual(fluidStack)) {
                return !recheckSize || fluidStack.getAmount() <= fluidTank.getNeeded();
            }
        }
        return false;
    }

    public void reset() {
        activeType = FluidStack.EMPTY;
        recurringNodes.clear();
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putInt(NBTConstants.PROGRESS, operatingTicks);
        if (!activeType.isEmpty()) {
            nbtTags.put(NBTConstants.FLUID_STORED, activeType.writeToNBT(new CompoundNBT()));
        }
        if (!recurringNodes.isEmpty()) {
            ListNBT recurringList = new ListNBT();
            for (BlockPos nodePos : recurringNodes) {
                recurringList.add(NBTUtil.writeBlockPos(nodePos));
            }
            nbtTags.put(NBTConstants.RECURRING_NODES, recurringList);
        }
        return nbtTags;
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.load(state, nbtTags);
        operatingTicks = nbtTags.getInt(NBTConstants.PROGRESS);
        if (nbtTags.getBoolean(NBTConstants.SUCKED_LAST_OPERATION)) {//TODO - 1.18: Remove this
            //If we need were saved as needing to use power, make sure we use it when loading
            energyContainer.extract(energyContainer.getEnergyPerTick(), Action.EXECUTE, AutomationType.INTERNAL);
        }
        NBTUtils.setFluidStackIfPresent(nbtTags, NBTConstants.FLUID_STORED, fluid -> activeType = fluid);
        if (nbtTags.contains(NBTConstants.RECURRING_NODES, NBT.TAG_LIST)) {
            ListNBT tagList = nbtTags.getList(NBTConstants.RECURRING_NODES, NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                recurringNodes.add(NBTUtil.readBlockPos(tagList.getCompound(i)));
            }
        }
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        reset();
        player.sendMessage(MekanismUtils.logFormat(MekanismLang.PUMP_RESET), Util.NIL_UUID);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    public List<ITextComponent> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    public MachineEnergyContainer<TileEntityElectricPump> getEnergyContainer() {
        return energyContainer;
    }

    //Methods relating to IComputerTile
    @ComputerMethod(nameOverride = "reset")
    private void resetPump() throws ComputerException {
        validateSecurityIsPublic();
        reset();
    }
    //End methods IComputerTile
}