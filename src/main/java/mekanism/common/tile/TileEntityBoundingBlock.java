package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Multi-block used by wind turbines, solar panels, and other machines
 */
public class TileEntityBoundingBlock extends TileEntityUpdateable implements IUpgradeTile, Nameable {

    private BlockPos mainPos = BlockPos.ZERO;

    private boolean receivedCoords;
    private int currentRedstoneLevel;

    public TileEntityBoundingBlock(BlockPos pos, BlockState state) {
        super(MekanismTileEntityTypes.BOUNDING_BLOCK, pos, state);
    }

    public void setMainLocation(BlockPos pos) {
        receivedCoords = pos != null;
        if (!isRemote()) {
            mainPos = pos;
            sendUpdatePacket();
        }
    }

    public boolean hasReceivedCoords() {
        return receivedCoords;
    }

    public BlockPos getMainPos() {
        if (mainPos == null) {
            mainPos = BlockPos.ZERO;
        }
        return mainPos;
    }

    @Nullable
    public BlockEntity getMainTile() {
        return receivedCoords ? WorldUtils.getTileEntity(level, getMainPos()) : null;
    }

    @Nullable
    private IBoundingBlock getMain() {
        // Return the main tile; note that it's possible, esp. when chunks are
        // loading that the main tile has not yet loaded and thus is null.
        BlockEntity tile = getMainTile();
        if (tile != null && !(tile instanceof IBoundingBlock)) {
            // On the off chance that another block got placed there (which seems only likely with corruption, go ahead and log what we found.)
            Mekanism.logger.error("Found tile {} instead of an IBoundingBlock, at {}. Multiblock cannot function", tile, getMainPos());
            return null;
        }
        return (IBoundingBlock) tile;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        IBoundingBlock main = getMain();
        if (main == null) {
            return super.getCapability(capability, side);
        }
        return main.getOffsetCapability(capability, side, worldPosition.subtract(getMainPos()));
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        boolean handled = super.triggerEvent(id, param);
        IBoundingBlock main = getMain();
        return main != null && main.triggerBoundingEvent(worldPosition.subtract(getMainPos()), id, param) || handled;
    }

    public void onNeighborChange(Block block, BlockPos neighborPos) {
        if (!isRemote()) {
            int power = level.getBestNeighborSignal(getBlockPos());
            if (currentRedstoneLevel != power) {
                IBoundingBlock main = getMain();
                if (main != null) {
                    main.onBoundingBlockPowerChange(worldPosition, currentRedstoneLevel, power);
                }
                currentRedstoneLevel = power;
            }
        }
    }

    public int getComparatorSignal() {
        IBoundingBlock main = getMain();
        if (main != null && main.supportsComparator()) {
            return main.getBoundingComparatorSignal(worldPosition.subtract(getMainPos()));
        }
        return 0;
    }

    @Override
    public boolean supportsUpgrades() {
        IBoundingBlock main = getMain();
        return main != null && main.supportsUpgrades();
    }

    @Override
    public TileComponentUpgrade getComponent() {
        IBoundingBlock main = getMain();
        if (main != null && main.supportsUpgrades()) {
            return main.getComponent();
        }
        return null;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgradeType) {
        IBoundingBlock main = getMain();
        if (main != null && main.supportsUpgrades()) {
            main.recalculateUpgrades(upgradeType);
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        NBTUtils.setBlockPosIfPresent(nbt, NBTConstants.MAIN, pos -> mainPos = pos);
        currentRedstoneLevel = nbt.getInt(NBTConstants.REDSTONE);
        receivedCoords = nbt.getBoolean(NBTConstants.RECEIVED_COORDS);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        if (receivedCoords) {
            nbtTags.put(NBTConstants.MAIN, NbtUtils.writeBlockPos(getMainPos()));
        }
        nbtTags.putInt(NBTConstants.REDSTONE, currentRedstoneLevel);
        nbtTags.putBoolean(NBTConstants.RECEIVED_COORDS, receivedCoords);
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        if (receivedCoords) {
            updateTag.put(NBTConstants.MAIN, NbtUtils.writeBlockPos(getMainPos()));
        }
        updateTag.putInt(NBTConstants.REDSTONE, currentRedstoneLevel);
        updateTag.putBoolean(NBTConstants.RECEIVED_COORDS, receivedCoords);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBlockPosIfPresent(tag, NBTConstants.MAIN, pos -> mainPos = pos);
        currentRedstoneLevel = tag.getInt(NBTConstants.REDSTONE);
        receivedCoords = tag.getBoolean(NBTConstants.RECEIVED_COORDS);
    }

    @Override
    public boolean hasCustomName() {
        return getMainTile() instanceof Nameable mainTile && mainTile.hasCustomName();
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Component getName() {
        // Safe check for the custom name being null is done in {@link hasCustomName()} already
        return hasCustomName() ? getCustomName() : MekanismBlocks.BOUNDING_BLOCK.getTextComponent();
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return getMainTile() instanceof Nameable mainTile ? mainTile.getDisplayName() : MekanismBlocks.BOUNDING_BLOCK.getTextComponent();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return getMainTile() instanceof Nameable mainTile ? mainTile.getCustomName() : null;
    }
}