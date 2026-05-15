package mekanism.common.tile;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.container.LargeResourceStack;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.upgrade.FluidTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.ResourceUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFluidTank extends TileEntityMekanism implements IConfigurable, IFluidContainerManager {

    private static Codec<FluidStack> VALVE_FLUID_CODEC = FluidStack.fixedAmountCodec(1);

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getStored", "getCapacity", "getNeeded",
                                                                                     "getFilledPercentage"}, docPlaceholder = "tank")
    public FluidTankFluidTank fluidTank;

    @Nullable
    private IFluidTank belowTank;
    private boolean resolvedBelowTank;

    private ContainerEditMode editMode = ContainerEditMode.BOTH;

    public FluidTankTier tier;

    private int valve;
    @NotNull
    public FluidStack valveFluid = FluidStack.EMPTY;
    private List<BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> fluidHandlerBelow = Collections.emptyList();

    public float prevScale;

    private boolean needsPacket;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    FluidInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;

    private int lastLightLevel;
    private int lightUpdateDelay;

    public TileEntityFluidTank(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockHolder(), FluidTankTier.class);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSide(facingSupplier);
        builder.addTank(fluidTank = FluidTankFluidTank.create(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(inputSlot = FluidInventorySlot.input(fluidTank, listener, 146, 19));
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, 146, 51));
        inputSlot.setSlotOverlay(SlotOverlay.INPUT);
        outputSlot.setSlotOverlay(SlotOverlay.OUTPUT);
        return builder.build();
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        checkLight();
    }

    private void checkLight() {
        if (lightUpdateDelay > 0) {
            lightUpdateDelay--;
            if (lightUpdateDelay == 0) {
                int lightLevel = getBlockState().getLightEmission(level, worldPosition);
                if (lightLevel != lastLightLevel) {
                    lastLightLevel = lightLevel;
                    level.getLightEngine().checkBlock(worldPosition);
                }
            }
        }
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (valve > 0) {
            valve--;
            if (valve == 0) {
                valveFluid = FluidStack.EMPTY;
                needsPacket = true;
            }
        }
        checkLight();

        float scale = MekanismUtils.getScale(prevScale, fluidTank);
        //TODO - 1.21: Figure out handling of stacked tanks where it may be going back and forth between being full and not?
        // or even just empty and not
        if (MekanismUtils.scaleChanged(scale, prevScale)) {
            if (prevScale == 0 || scale == 0) {
                //If it was empty and no longer is, or wasn't empty and now is empty we want to recheck the block lighting
                // as the fluid may have changed and have a light value
                if (lightUpdateDelay == 0) {
                    lightUpdateDelay = prevScale == 0 ? 1 : MekanismConfig.general.blockDeactivationDelay.get();
                }
            }
            prevScale = scale;
            sendUpdatePacket = true;
        }
        inputSlot.handleTank(outputSlot, editMode);
        if (getActive()) {
            if (fluidHandlerBelow.isEmpty()) {
                //Note: We just pass true for this always being valid, and allow GC to handle figuring out when it no longer is valid
                fluidHandlerBelow = List.of(Capabilities.FLUID.createCache((ServerLevel) level, worldPosition.below(), Direction.UP, ConstantPredicates.ALWAYS_TRUE, () -> {
                    //Reset the tank that we know is below this
                    resolvedBelowTank = false;
                    belowTank = null;
                }));
            }
            IFluidTank below = getBelowTank();
            if (below == null) {
                ResourceUtils.emit(fluidHandlerBelow, fluidTank, tier.getOutput(), null);
            } else {
                //If the block below this tank, is also a tank. Only emit as much as it might be able to accept.
                // This prevents it then trying to go up the chain back to this tank and any ones above it
                ResourceUtils.emit(fluidHandlerBelow, fluidTank, Math.min(below.getNeededAsInt(), tier.getOutput()), null);
            }
        }
        if (needsPacket) {
            sendUpdatePacket = true;
            needsPacket = false;
        }
        return sendUpdatePacket;
    }

    @Nullable
    private IFluidTank getBelowTank() {
        if (!resolvedBelowTank) {
            resolvedBelowTank = true;
            ResourceHandler<FluidResource> belowHandler = fluidHandlerBelow.getFirst().getCapability();
            //TODO - 26.1: Re-evaluate how we want to be implementing this as the fluid handler's internal handler no longer is an instead of this class
            // due to it being an anonymous class
            /*if (belowHandler instanceof ProxyResourceHandler<FluidResource> fluidHandler && fluidHandler.getInternalHandler() instanceof TileEntityFluidTank tank) {
                //Note: We don't need to bother with weak references as these are vertical so will always be in the same chunk
                belowTank = tank.fluidTank;
            }*/
        }
        return belowTank;
    }

    @Override
    public void writeSustainedData(@NotNull ValueOutput output) {
        super.writeSustainedData(output);
        NBTUtils.writeEnum(output, SerializationConstants.EDIT_MODE, editMode);
    }

    @Override
    public void readSustainedData(@NotNull ValueInput input) {
        super.readSustainedData(input);
        NBTUtils.setEnumIfPresent(input, SerializationConstants.EDIT_MODE, ContainerEditMode.BY_ID, mode -> editMode = mode);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.EDIT_MODE, editMode);
    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentGetter input) {
        super.applyImplicitComponents(input);
        editMode = input.getOrDefault(MekanismDataComponents.EDIT_MODE, editMode);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank);
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.FLUID;
    }

    /*@NotNull
    @Override
    public FluidStack insertFluid(int tank, @NotNull FluidStack stack, @Nullable Direction side, @NotNull Action action) {
        return insertExcess(stack, side, action, super.insertFluid(tank, stack, side, action));
    }

    @NotNull
    @Override
    public FluidStack insertFluid(@NotNull FluidStack stack, @Nullable Direction side, @NotNull Action action) {
        return insertExcess(stack, side, action, super.insertFluid(stack, side, action));
    }*/

    //TODO - 26.1: Hook valve transferring back up
    private FluidStack insertExcess(@NotNull FluidStack stack, @Nullable Direction side, boolean execute, @NotNull FluidStack remainder) {
        if (side == Direction.UP && execute && remainder.amount() < stack.amount() && !isRemote()) {
            if (valve == 0) {
                //TODO - 1.21: Only mark it as needing a packet if our fluid tank volume is below a certain amount??
                needsPacket = true;
            }
            valve = SharedConstants.TICKS_PER_SECOND;
            valveFluid = stack.copyWithAmount(1);
        }
        return remainder;
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            setActive(!getActive());
            Level world = getLevel();
            if (world != null) {
                world.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.3F, 1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        return InteractionResult.PASS;
    }

    @Override
    @ComputerMethod
    public ContainerEditMode getContainerEditMode() {
        return editMode;
    }

    @Override
    public void nextMode() {
        editMode = editMode.getNext();
        markForSave();
    }

    @Override
    public void previousMode() {
        editMode = editMode.getPrevious();
        setChanged();
    }

    @Override
    public void parseUpgradeData(@NotNull IUpgradeData upgradeData, Provider provider) {
        if (upgradeData instanceof FluidTankUpgradeData data) {
            redstone = data.redstone;
            inputSlot.setContents(data.inputSlot.getResource(), data.inputSlot.amountAsInt());
            outputSlot.setContents(data.outputSlot.getResource(), data.outputSlot.amountAsInt());
            editMode = data.editMode;
            fluidTank.setContents(data.stored);
            try (var reporter = new ProblemReporter.ScopedCollector(problemPath(), Mekanism.logger)) {
                ValueInput input = TagValueInput.create(reporter, provider, data.components);
                for (ITileComponent component : getComponents()) {
                    component.read(input);
                }
            }
        } else {
            super.parseUpgradeData(upgradeData, provider);
        }
    }

    @NotNull
    @Override
    public FluidTankUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new FluidTankUpgradeData(provider, redstone, inputSlot, outputSlot, editMode, fluidTank.asStack(), getComponents(), problemPath());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(ContainerEditMode.BY_ID, ContainerEditMode.BOTH, () -> editMode, value -> editMode = value));
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        lightUpdateDelay = input.getIntOr(SerializationConstants.DELAY, lightUpdateDelay);
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.DELAY, lightUpdateDelay);
    }

    @Override
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        //updateTag.put(SerializationConstants.FLUID, fluidTank.getFluid().saveOptional(provider));
        //updateTag.put(SerializationConstants.VALVE, valveFluid.saveOptional(provider));
        output.putFloat(SerializationConstants.SCALE, prevScale);
        //TODO - 26.1: Re-evaluate this alternate encoding further (check history)
        LargeResourceStack<FluidResource> fluid = fluidTank.asStack();
        if (!fluid.isEmpty()) {
            output.store(SerializationConstants.FLUID, SerializerHelper.FLUID_RESOURCE_STACK_CODEC, fluid);
        }
        if (!valveFluid.isEmpty()) {
            output.store(SerializationConstants.VALVE, VALVE_FLUID_CODEC, valveFluid);
        }
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        super.handleUpdateTag(input);
        //input.child(SerializationConstants.FLUID).ifPresent(fluidTank::deserialize);
        //valveFluid = input.read(SerializationConstants.VALVE, FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY);
        float scale = input.getFloatOr(SerializationConstants.SCALE, prevScale);
        if (lightUpdateDelay == 0 && MekanismUtils.scaleChanged(prevScale, scale)) {
            if (prevScale == 0 || scale == 0) {
                //If it was empty and no longer is, or wasn't empty and now is empty we want to recheck the block lighting
                // as the fluid may have changed and have a light value, mark that the client should update the light value
                //Note: If we previously had no fluid, we queue the lighting for the next client tick
                lightUpdateDelay = prevScale == 0 ? 1 : MekanismConfig.general.blockDeactivationDelay.get();
            }
        }
        //TODO - 26.1: Should we only update this when the scale has changed? And/or if we had updated the light level?
        prevScale = scale;

        fluidTank.setContents(input.read(SerializationConstants.FLUID, SerializerHelper.FLUID_RESOURCE_STACK_CODEC).orElse(LargeResourceStack.EMPTY_FLUID_STACK));
        valveFluid = input.read(SerializationConstants.VALVE, VALVE_FLUID_CODEC).orElse(FluidStack.EMPTY);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true)
    void setContainerEditMode(ContainerEditMode mode) throws ComputerException {
        validateSecurityIsPublic();
        if (editMode != mode) {
            editMode = mode;
            markForSave();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void incrementContainerEditMode() throws ComputerException {
        validateSecurityIsPublic();
        nextMode();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void decrementContainerEditMode() throws ComputerException {
        validateSecurityIsPublic();
        previousMode();
    }
    //End methods IComputerTile
}
