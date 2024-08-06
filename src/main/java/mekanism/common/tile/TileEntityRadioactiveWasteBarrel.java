package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPITags;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.StackedWasteBarrel;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityRadioactiveWasteBarrel extends TileEntityMekanism implements IConfigurable {

    private long lastProcessTick;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getStored", "getCapacity", "getNeeded",
                                                                                        "getFilledPercentage"}, docPlaceholder = "barrel")
    StackedWasteBarrel gasTank;
    private int processTicks;
    private List<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> gasHandlerBelow = Collections.emptyList();

    public TileEntityRadioactiveWasteBarrel(BlockPos pos, BlockState state) {
        super(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, pos, state);
        delaySupplier = NO_DELAY;
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSide(this::getDirection);
        builder.addTank(gasTank = StackedWasteBarrel.create(this, listener), RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (level.getGameTime() > lastProcessTick) {
            //If we are not on the same tick do stuff, otherwise ignore it (anti tick accelerator protection)
            lastProcessTick = level.getGameTime();
            if (MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get() > 0 && !gasTank.isEmpty() &&
                !gasTank.getType().is(MekanismAPITags.WASTE_BARREL_DECAY_BLACKLIST) &&
                ++processTicks >= MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get()) {
                processTicks = 0;
                gasTank.shrinkStack(MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get(), Action.EXECUTE);
            }
            if (getActive()) {
                if (gasHandlerBelow.isEmpty()) {
                    gasHandlerBelow = List.of(BlockCapabilityCache.create(Capabilities.CHEMICAL.block(), (ServerLevel) level, worldPosition.below(), Direction.UP));
                }
                ChemicalUtil.emit(gasHandlerBelow, gasTank);
            }
            //Note: We don't need to do any checking here if the packet needs due to capacity changing as we do it
            // in TileentityMekanism after this method is called. And given radioactive waste barrels can only contain
            // radioactive substances the check for radiation scale also will work for syncing capacity for purposes
            // of when the client sneak right-clicks on the barrel
        }
        return sendUpdatePacket;
    }

    public StackedWasteBarrel getGasTank() {
        return gasTank;
    }

    public double getGasScale() {
        return gasTank.getStored() / (double) gasTank.getCapacity();
    }

    public ChemicalStack getGas() {
        return gasTank.getStack();
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

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.put(SerializationConstants.GAS, gasTank.serializeNBT(provider));
        updateTag.putInt(SerializationConstants.PROGRESS, processTicks);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setCompoundIfPresent(tag, SerializationConstants.GAS, nbt -> gasTank.deserializeNBT(provider, nbt));
        NBTUtils.setIntIfPresent(tag, SerializationConstants.PROGRESS, val -> processTicks = val);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }
}
