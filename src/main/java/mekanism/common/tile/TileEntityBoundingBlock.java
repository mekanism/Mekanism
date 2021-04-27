package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Multi-block used by wind turbines, solar panels, and other machines
 */
public class TileEntityBoundingBlock extends TileEntityUpdateable implements IUpgradeTile, ISecurityTile {

    private BlockPos mainPos = BlockPos.ZERO;

    public boolean receivedCoords;
    private int currentRedstoneLevel;

    public TileEntityBoundingBlock() {
        this(MekanismTileEntityTypes.BOUNDING_BLOCK.getTileEntityType());
    }

    public TileEntityBoundingBlock(TileEntityType<TileEntityBoundingBlock> type) {
        super(type);
    }

    public void setMainLocation(BlockPos pos) {
        receivedCoords = pos != null;
        if (!isRemote()) {
            mainPos = pos;
            sendUpdatePacket();
        }
    }

    public BlockPos getMainPos() {
        if (mainPos == null) {
            mainPos = BlockPos.ZERO;
        }
        return mainPos;
    }

    @Nullable
    public TileEntity getMainTile() {
        return receivedCoords ? WorldUtils.getTileEntity(level, getMainPos()) : null;
    }

    @Nullable
    private IBoundingBlock getMain() {
        // Return the main tile; note that it's possible, esp. when chunks are
        // loading that the main tile has not yet loaded and thus is null.
        TileEntity tile = getMainTile();
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
    public boolean hasSecurity() {
        IBoundingBlock main = getMain();
        return main != null && main.hasSecurity();
    }

    @Override
    public TileComponentSecurity getSecurity() {
        IBoundingBlock main = getMain();
        if (main != null && main.hasSecurity()) {
            return main.getSecurity();
        }
        return null;
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.load(state, nbtTags);
        NBTUtils.setBlockPosIfPresent(nbtTags, NBTConstants.MAIN, pos -> mainPos = pos);
        currentRedstoneLevel = nbtTags.getInt(NBTConstants.REDSTONE);
        receivedCoords = nbtTags.getBoolean(NBTConstants.RECEIVED_COORDS);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.put(NBTConstants.MAIN, NBTUtil.writeBlockPos(getMainPos()));
        nbtTags.putInt(NBTConstants.REDSTONE, currentRedstoneLevel);
        nbtTags.putBoolean(NBTConstants.RECEIVED_COORDS, receivedCoords);
        return nbtTags;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.MAIN, NBTUtil.writeBlockPos(getMainPos()));
        updateTag.putInt(NBTConstants.REDSTONE, currentRedstoneLevel);
        updateTag.putBoolean(NBTConstants.RECEIVED_COORDS, receivedCoords);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setBlockPosIfPresent(tag, NBTConstants.MAIN, pos -> mainPos = pos);
        currentRedstoneLevel = tag.getInt(NBTConstants.REDSTONE);
        receivedCoords = tag.getBoolean(NBTConstants.RECEIVED_COORDS);
    }
}