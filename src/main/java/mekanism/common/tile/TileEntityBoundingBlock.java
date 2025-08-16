package mekanism.common.tile;

import mekanism.api.SerializationConstants;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Multi-block used by wind turbines, solar panels, and other machines
 */
public class TileEntityBoundingBlock extends TileEntityUpdateable implements IUpgradeTile, Nameable {

    @Nullable
    private BlockPos mainPos;
    private int currentRedstoneLevel;

    public TileEntityBoundingBlock(BlockPos pos, BlockState state) {
        super(MekanismTileEntityTypes.BOUNDING_BLOCK, pos, state);
    }

    public void setMainLocation(@Nullable BlockPos pos, boolean sync) {
        mainPos = pos;
        if (sync && !isRemote()) {
            sendUpdatePacket();
        }
    }

    public boolean canRedirectFrom(BlockPos boundingPos) {
        return mainPos != null && !mainPos.equals(boundingPos);
    }

    public BlockPos getMainPos() {
        if (mainPos == null) {
            return worldPosition;
        }
        return mainPos;
    }

    @Nullable
    public BlockEntity getMainTile(BlockPos boundingPos) {
        return canRedirectFrom(boundingPos) ? WorldUtils.getTileEntity(level, getMainPos()) : null;
    }

    @Nullable
    private IBoundingBlock getMain() {
        // Return the main tile; note that it's possible, esp. when chunks are
        // loading that the main tile has not yet loaded and thus is null.
        BlockEntity tile = getMainTile(worldPosition);
        if (tile != null && !(tile instanceof IBoundingBlock)) {
            // On the off chance that another block got placed there (which seems only likely with corruption, go ahead and log what we found.)
            Mekanism.logger.error("Found tile {} instead of an IBoundingBlock, at {} in {}. Multiblock cannot function", tile, getMainPos(),
                  level == null ? "null" : level.dimension().location());
            return null;
        }
        return (IBoundingBlock) tile;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        boolean handled = super.triggerEvent(id, param);
        IBoundingBlock main = getMain();
        return main != null && main.triggerBoundingEvent(worldPosition.subtract(getMainPos()), id, param) || handled;
    }

    public void onNeighborChange(Level level, Block block, BlockPos neighborPos) {
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
        return Redstone.SIGNAL_NONE;
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
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        NBTUtils.setBlockPosIfPresent(nbt, SerializationConstants.MAIN, pos -> mainPos = pos);
        currentRedstoneLevel = nbt.getInt(SerializationConstants.REDSTONE);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        if (mainPos != null) {
            nbtTags.put(SerializationConstants.MAIN, NbtUtils.writeBlockPos(mainPos));
        }
        nbtTags.putInt(SerializationConstants.REDSTONE, currentRedstoneLevel);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        if (mainPos != null) {
            updateTag.put(SerializationConstants.MAIN, NbtUtils.writeBlockPos(mainPos));
        }
        updateTag.putInt(SerializationConstants.REDSTONE, currentRedstoneLevel);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setBlockPosIfPresent(tag, SerializationConstants.MAIN, pos -> mainPos = pos);
        currentRedstoneLevel = tag.getInt(SerializationConstants.REDSTONE);
    }

    @Override
    public boolean hasCustomName() {
        return getMainTile(worldPosition) instanceof Nameable mainTile && mainTile.hasCustomName();
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Component getName() {
        if (getMainTile(worldPosition) instanceof Nameable mainTile && mainTile.hasCustomName()) {
            return mainTile.getCustomName();
        }
        return MekanismBlocks.BOUNDING_BLOCK.getTextComponent();
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return getMainTile(worldPosition) instanceof Nameable mainTile ? mainTile.getDisplayName() : MekanismBlocks.BOUNDING_BLOCK.getTextComponent();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return getMainTile(worldPosition) instanceof Nameable mainTile ? mainTile.getCustomName() : null;
    }

    public static <CAP> void proxyCapability(RegisterCapabilitiesEvent event, BlockCapability<CAP, @Nullable Direction> capability) {
        event.registerBlock(capability, (level, pos, state, blockEntity, context) -> {
            if (blockEntity instanceof TileEntityBoundingBlock bounding) {
                IBoundingBlock main = bounding.getMain();
                if (main != null) {
                    return main.getOffsetCapability(capability, context, pos.subtract(bounding.getMainPos()));
                }
            }
            return null;
        }, MekanismBlocks.BOUNDING_BLOCK.value());
    }

    public static <CAP, CONTEXT> void alwaysProxyCapability(RegisterCapabilitiesEvent event, BlockCapability<CAP, CONTEXT> capability) {
        event.registerBlock(capability, (level, pos, state, boundingBlock, context) -> {
            if (boundingBlock instanceof TileEntityBoundingBlock bounding && bounding.canRedirectFrom(pos)) {
                return WorldUtils.getCapability(level, capability, bounding.getMainPos(), context);
            }
            return null;
        }, MekanismBlocks.BOUNDING_BLOCK.value());
    }
}