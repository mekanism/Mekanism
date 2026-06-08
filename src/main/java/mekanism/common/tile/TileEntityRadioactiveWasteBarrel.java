package mekanism.common.tile;

import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPITags;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.IContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.StackedWasteBarrel;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.proxy.BelowContainerCache;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityRadioactiveWasteBarrel extends TileEntityMekanism implements IConfigurable {

    private long lastProcessTick;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getStored", "getCapacity", "getNeeded",
                                                                                        "getFilledPercentage"}, docPlaceholder = "barrel")
    StackedWasteBarrel chemicalTank;

    @Nullable
    private BelowContainerCache<ChemicalResource, IChemicalTank> belowTankCache;

    private int processTicks;

    public TileEntityRadioactiveWasteBarrel(BlockPos pos, BlockState state) {
        super(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, pos, state);
        delaySupplier = NO_DELAY;
    }

    @NotNull
    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        MekContainerHelper<IChemicalTank> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(chemicalTank = StackedWasteBarrel.create(this, listener), RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (level.getGameTime() > lastProcessTick) {
            //If we are not on the same tick do stuff, otherwise ignore it (anti tick accelerator protection)
            lastProcessTick = level.getGameTime();
            if (!chemicalTank.isEmpty()) {
                ChemicalResource chemicalType = chemicalTank.resource();
                int decayAmount = MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get();
                if (decayAmount > 0 && !chemicalType.is(MekanismAPITags.Chemicals.WASTE_BARREL_DECAY_BLACKLIST) &&
                    ++processTicks >= MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get()) {
                    processTicks = 0;
                    try (Transaction transaction = Transaction.openRoot()) {
                        chemicalTank.extract(chemicalType, decayAmount, transaction, AutomationType.INTERNAL);
                        transaction.commit();
                    }
                }
            }
            if (getActive()) {
                if (belowTankCache == null) {
                    belowTankCache = new BelowContainerCache<>(Capabilities.CHEMICAL, (ServerLevel) level, worldPosition);
                }
                int toEmit = chemicalTank.amountAsInt();
                IChemicalTank below = belowTankCache.getContainer(StackedWasteBarrel.class);
                if (below != null) {
                    //If the block below this barrel, is also a barrel. Only emit as much as it might be able to accept.
                    // This prevents it then trying to go up the chain back to this barrel and any ones above it
                    toEmit = Math.min(below.getNeededAsInt(ChemicalResource.EMPTY), toEmit);
                }
                ResourceUtils.emit(belowTankCache.getHandler(), chemicalTank, toEmit, null);
            }
            //Note: We don't need to do any checking here if the packet needs due to capacity changing as we do it
            // in TileentityMekanism after this method is called. And given radioactive waste barrels can only contain
            // radioactive substances the check for radiation scale also will work for syncing capacity for purposes
            // of when the client sneak right-clicks on the barrel
        }
        return sendUpdatePacket;
    }

    @Override
    public void setLevel(@NotNull Level world) {
        super.setLevel(world);
        //Invalidate the cache as if the level changed then it might no longer be valid
        belowTankCache = null;
    }

    public StackedWasteBarrel getChemicalTank() {
        return chemicalTank;
    }

    public double getChemicalScale() {
        return chemicalTank.amountAsLong() / (double) chemicalTank.capacityAsLong(chemicalTank.resource());
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
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.putChild(SerializationConstants.CHEMICAL, chemicalTank);
        output.putInt(SerializationConstants.PROGRESS, processTicks);
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        super.handleUpdateTag(input);
        input.readChild(SerializationConstants.CHEMICAL, chemicalTank);
        processTicks = input.getIntOr(SerializationConstants.PROGRESS, processTicks);
    }

    @Override
    public int getRedstoneLevel() {
        return ContainerType.CHEMICAL.getRedstoneSignalFromContainer(chemicalTank);
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }
}
