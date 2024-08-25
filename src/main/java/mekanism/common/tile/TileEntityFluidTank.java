package mekanism.common.tile;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.proxy.ProxyFluidHandler;
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
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFluidTank extends TileEntityMekanism implements IConfigurable, IFluidContainerManager {

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getStored", "getCapacity", "getNeeded",
                                                                                     "getFilledPercentage"}, docPlaceholder = "tank")
    public FluidTankFluidTank fluidTank;

    @Nullable
    private IExtendedFluidTank belowTank;
    private boolean resolvedBelowTank;

    private ContainerEditMode editMode = ContainerEditMode.BOTH;

    public FluidTankTier tier;

    private int valve;
    @NotNull
    public FluidStack valveFluid = FluidStack.EMPTY;
    private List<BlockCapabilityCache<IFluidHandler, @Nullable Direction>> fluidHandlerBelow = Collections.emptyList();

    public float prevScale;

    private boolean needsPacket;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    FluidInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;

    private boolean updateClientLight = false;

    public TileEntityFluidTank(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), FluidTankTier.class);
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
        if (updateClientLight) {
            WorldUtils.recheckLighting(level, worldPosition);
            updateClientLight = false;
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

        float scale = MekanismUtils.getScale(prevScale, fluidTank);
        //TODO - 1.21: Figure out handling of stacked tanks where it may be going back and forth between being full and not?
        // or even just empty and not
        if (MekanismUtils.scaleChanged(scale, prevScale)) {
            if (prevScale == 0 || scale == 0) {
                //If it was empty and no longer is, or wasn't empty and now is empty we want to recheck the block lighting
                // as the fluid may have changed and have a light value
                //TODO - 1.21: Do we want to use the active delay for turning lighting off?
                WorldUtils.recheckLighting(level, worldPosition);
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
            IExtendedFluidTank below = getBelowTank();
            if (below == null) {
                FluidUtils.emit(fluidHandlerBelow, fluidTank, tier.getOutput());
            } else {
                //If the block below this tank, is also a tank. Only emit as much as it might be able to accept.
                // This prevents it then trying to go up the chain back to this tank and any ones above it
                FluidUtils.emit(fluidHandlerBelow, fluidTank, Math.min(below.getNeeded(), tier.getOutput()));
            }
        }
        if (needsPacket) {
            sendUpdatePacket = true;
            needsPacket = false;
        }
        return sendUpdatePacket;
    }

    @Nullable
    private IExtendedFluidTank getBelowTank() {
        if (!resolvedBelowTank) {
            resolvedBelowTank = true;
            IFluidHandler belowHandler = fluidHandlerBelow.getFirst().getCapability();
            if (belowHandler instanceof ProxyFluidHandler fluidHandler && fluidHandler.getInternalHandler() instanceof TileEntityFluidTank tank) {
                //Note: We don't need to bother with weak references as these are vertical so will always be in the same chunk
                belowTank = tank.fluidTank;
            }
        }
        return belowTank;
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag data) {
        super.writeSustainedData(provider, data);
        NBTUtils.writeEnum(data, SerializationConstants.EDIT_MODE, editMode);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag data) {
        super.readSustainedData(provider, data);
        NBTUtils.setEnumIfPresent(data, SerializationConstants.EDIT_MODE, ContainerEditMode.BY_ID, mode -> editMode = mode);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.EDIT_MODE, editMode);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        editMode = input.getOrDefault(MekanismDataComponents.EDIT_MODE, editMode);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.FLUID;
    }

    @NotNull
    @Override
    public FluidStack insertFluid(int tank, @NotNull FluidStack stack, @Nullable Direction side, @NotNull Action action) {
        return insertExcess(stack, side, action, super.insertFluid(tank, stack, side, action));
    }

    @NotNull
    @Override
    public FluidStack insertFluid(@NotNull FluidStack stack, @Nullable Direction side, @NotNull Action action) {
        return insertExcess(stack, side, action, super.insertFluid(stack, side, action));
    }

    private FluidStack insertExcess(@NotNull FluidStack stack, @Nullable Direction side, @NotNull Action action, @NotNull FluidStack remainder) {
        if (side == Direction.UP && action.execute() && remainder.getAmount() < stack.getAmount() && !isRemote()) {
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
    public void parseUpgradeData(HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof FluidTankUpgradeData data) {
            redstone = data.redstone;
            inputSlot.setStack(data.inputSlot.getStack());
            outputSlot.setStack(data.outputSlot.getStack());
            editMode = data.editMode;
            fluidTank.setStack(data.stored);
            for (ITileComponent component : getComponents()) {
                component.read(data.components, provider);
            }
        } else {
            super.parseUpgradeData(provider, upgradeData);
        }
    }

    @NotNull
    @Override
    public FluidTankUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new FluidTankUpgradeData(provider, redstone, inputSlot, outputSlot, editMode, fluidTank.getFluid(), getComponents());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(ContainerEditMode.BY_ID, ContainerEditMode.BOTH, () -> editMode, value -> editMode = value));
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        //updateTag.put(SerializationConstants.FLUID, fluidTank.getFluid().saveOptional(provider));
        //updateTag.put(SerializationConstants.VALVE, valveFluid.saveOptional(provider));
        updateTag.putFloat(SerializationConstants.SCALE, prevScale);
        //TODO - 1.21: Re-evaluate this alternate encoding further
        CompoundTag fluidData = new CompoundTag();
        FluidStack fluid = fluidTank.getFluid();
        if (fluid.isEmpty()) {
            fluid = valveFluid;
        } else {
            fluidData.putInt(SerializationConstants.AMOUNT, fluid.getAmount());
        }
        if (!fluid.isEmpty()) {
            //Note: This should never be null as it returns a reference holder
            // We throw if it is, so that we can find the bug if it gets introduced during porting
            ResourceKey<Fluid> key = Objects.requireNonNull(fluid.getFluidHolder().getKey());
            fluidData.putString(SerializationConstants.ID, key.location().toString());
            if (!fluid.isComponentsPatchEmpty()) {
                //Note: This isn't necessarily optimal, but it does mean in general we can avoid codecs unless it happens to be a fluid that
                // does have component data
                DataResult<Tag> componentData = DataComponentPatch.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), fluid.getComponentsPatch());
                if (componentData.isError()) {
                    componentData.mapError(error -> {
                        Mekanism.logger.error("Failed to encode fluid stack component data: {}", error);
                        return error;
                    });
                } else {
                    fluidData.put(SerializationConstants.DATA, componentData.getOrThrow());
                }
            }
            fluidData.putBoolean(SerializationConstants.VALVE, !valveFluid.isEmpty());
            updateTag.put(SerializationConstants.FLUID, fluidData);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        //NBTUtils.setFluidStackIfPresent(provider, tag, SerializationConstants.FLUID, fluid -> fluidTank.setStack(fluid));
        //NBTUtils.setFluidStackIfPresent(provider, tag, SerializationConstants.VALVE, fluid -> valveFluid = fluid);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> {
            if (MekanismUtils.scaleChanged(prevScale, scale)) {
                if (prevScale == 0 || scale == 0) {
                    //If it was empty and no longer is, or wasn't empty and now is empty we want to recheck the block lighting
                    // as the fluid may have changed and have a light value, mark that the client should update the light value
                    updateClientLight = true;
                }
            }
            prevScale = scale;
        });

        boolean unsetFluid = true;
        if (tag.contains(SerializationConstants.FLUID, Tag.TAG_COMPOUND)) {
            CompoundTag fluidData = tag.getCompound(SerializationConstants.FLUID);
            if (!fluidData.isEmpty()) {
                String fluidId = fluidData.getString(SerializationConstants.ID);
                Optional<Reference<Fluid>> holder = BuiltInRegistries.FLUID.getHolder(ResourceLocation.parse(fluidId));
                if (holder.isEmpty()) {
                    Mekanism.logger.info("Received update packet for a fluid tank for an unregistered fluid with expected id: {}", fluidId);
                } else {
                    Reference<Fluid> fluidType = holder.get();
                    DataComponentPatch patch = DataComponentPatch.EMPTY;
                    int amount = fluidData.getInt(SerializationConstants.AMOUNT);
                    if (fluidData.contains(SerializationConstants.DATA)) {
                        DataResult<Pair<DataComponentPatch, Tag>> componentPatch = DataComponentPatch.CODEC.decode(
                              provider.createSerializationContext(NbtOps.INSTANCE),
                              fluidData.get(SerializationConstants.DATA)
                        );
                        if (componentPatch.isSuccess()) {
                            patch = componentPatch.getOrThrow().getFirst();
                        } else {
                            componentPatch.mapError(error -> {
                                Mekanism.logger.info("Received update packet for a fluid tank storing {}, and could not decode the data component patch: {}",
                                      fluidId, error);
                                return error;
                            });
                        }
                    }
                    //We actually have something to set, so mark that we shouldn't reset the stored fluid data
                    unsetFluid = false;
                    if (amount == 0) {
                        fluidTank.setEmpty();
                    } else {
                        fluidTank.setStack(new FluidStack(fluidType, amount, patch));
                    }
                    if (fluidData.getBoolean(SerializationConstants.VALVE)) {
                        valveFluid = new FluidStack(fluidType, 1, patch);
                    } else {
                        valveFluid = FluidStack.EMPTY;
                    }
                }
            }
        }
        if (unsetFluid) {
            fluidTank.setEmpty();
            valveFluid = FluidStack.EMPTY;
        }
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
