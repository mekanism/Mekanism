package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.Nullable;

/// Multi-block used by wind turbines, solar panels, and other machines
public class TileEntityBoundingBlock extends TileEntityUpdateable implements IUpgradeTile, Nameable {

    @Nullable
    private Vec3i mainPosOffset;
    private int currentRedstoneLevel;

    public TileEntityBoundingBlock(BlockPos pos, BlockState state) {
        super(MekanismTileEntityTypes.BOUNDING_BLOCK, pos, state);
    }

    public void setMainLocationOffset(@Nullable Vec3i offset) {
        mainPosOffset = offset;
    }

    @Nullable
    public BlockPos getMainPos() {
        return mainPosOffset == null ? null : worldPosition.offset(mainPosOffset);
    }

    @Nullable
    public BlockEntity getMainTile() {
        BlockPos mainPos = getMainPos();
        return mainPos == null ? null : WorldUtils.getTileEntity(level, mainPos);
    }

    @Nullable
    private IBoundingBlock getMain() {
        // Return the main tile; note that it's possible, esp. when chunks are
        // loading that the main tile has not yet loaded and thus is null.
        BlockEntity tile = getMainTile();
        if (tile != null && !(tile instanceof IBoundingBlock)) {
            // On the off chance that another block got placed there (which seems only likely with corruption, go ahead and log what we found.)
            Mekanism.logger.error("Found tile {} instead of an IBoundingBlock, at an offset of {} from {} in {}. Multiblock cannot function", tile, mainPosOffset,
                  worldPosition, level == null ? "null" : level.dimension().identifier());
            return null;
        }
        return (IBoundingBlock) tile;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        //Remove the main block if a bounding block gets broken by being directly replaced
        if (level != null && mainPosOffset != null) {
            BlockPos mainPos = pos.offset(mainPosOffset);
            BlockState mainState = level.getBlockState(mainPos);
            if (!mainState.isAir()) {
                //Set the main block to air, which will invalidate the rest of the bounding blocks
                level.removeBlock(mainPos, false);
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        boolean handled = super.triggerEvent(id, param);
        IBoundingBlock main = getMain();
        return main != null && mainPosOffset != null && main.triggerBoundingEvent(mainPosOffset, id, param) || handled;
    }

    public void onNeighborChange(LevelReader level) {
        if (!level.isClientSide()) {
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
        if (main != null && main.supportsComparator() && mainPosOffset != null) {
            return main.getBoundingComparatorSignal(mainPosOffset);
        }
        return Redstone.SIGNAL_NONE;
    }

    @Override
    public boolean supportsUpgrades() {
        IBoundingBlock main = getMain();
        return main != null && main.supportsUpgrades();
    }

    @Nullable
    @Override
    public TagKey<Upgrade> getSupportedUpgrade() {
        IBoundingBlock main = getMain();
        return main == null ? null : main.getSupportedUpgrade();
    }

    @Override
    public float getVolumeFactor() {
        IBoundingBlock main = getMain();
        return main == null ? 1.0F : main.getVolumeFactor();
    }

    @Override
    public List<Component> getUpgradeWindowInfo(Holder<Upgrade> upgrade) {
        IBoundingBlock main = getMain();
        return main == null ? Collections.emptyList() : main.getUpgradeWindowInfo(upgrade);
    }

    @Nullable
    @Override
    public TileComponentUpgrade getComponent() {
        IBoundingBlock main = getMain();
        if (main != null && main.supportsUpgrades()) {
            return main.getComponent();
        }
        return null;
    }

    @Override
    public void recalculateUpgrades(HolderGetter<Upgrade> upgrades, Holder<Upgrade> upgradeType, int totalInstalled) {
        IBoundingBlock main = getMain();
        if (main != null && main.supportsUpgrades()) {
            main.recalculateUpgrades(upgrades, upgradeType, totalInstalled);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read(SerializationConstants.OFFSET, Vec3i.CODEC).ifPresent(pos -> mainPosOffset = pos);
        currentRedstoneLevel = input.getIntOr(SerializationConstants.REDSTONE, currentRedstoneLevel);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(SerializationConstants.OFFSET, Vec3i.CODEC, mainPosOffset);
        output.putInt(SerializationConstants.REDSTONE, currentRedstoneLevel);
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.storeNullable(SerializationConstants.OFFSET, Vec3i.CODEC, mainPosOffset);
        output.putInt(SerializationConstants.REDSTONE, currentRedstoneLevel);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.loadAdditional(input);//we do NOT call super directly, as it will call a load (like from disk) and BEs will never see their changes
        input.read(SerializationConstants.OFFSET, Vec3i.CODEC).ifPresent(pos -> mainPosOffset = pos);
        currentRedstoneLevel = input.getIntOr(SerializationConstants.REDSTONE, currentRedstoneLevel);
    }

    @Override
    public boolean hasCustomName() {
        return getMainTile() instanceof Nameable mainTile && mainTile.hasCustomName();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Component getName() {
        if (getMainTile() instanceof Nameable mainTile && mainTile.hasCustomName()) {
            return mainTile.getCustomName();
        }
        return MekanismBlocks.BOUNDING_BLOCK.value().getName();
    }

    @Override
    public String getPlainTextName() {
        return getMainTile() instanceof Nameable mainTile ? mainTile.getPlainTextName() : MekanismBlocks.BOUNDING_BLOCK.value().getName().getString();
    }

    @Override
    public Component getDisplayName() {
        return getMainTile() instanceof Nameable mainTile ? mainTile.getDisplayName() : MekanismBlocks.BOUNDING_BLOCK.value().getName();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return getMainTile() instanceof Nameable mainTile ? mainTile.getCustomName() : null;
    }

    public static <CAP> void proxyCapability(RegisterCapabilitiesEvent event, BlockCapability<CAP, @Nullable Direction> capability) {
        event.registerBlock(capability, (_, _, _, blockEntity, context) -> {
            if (blockEntity instanceof TileEntityBoundingBlock bounding) {
                IBoundingBlock main = bounding.getMain();
                if (main != null && bounding.mainPosOffset != null) {
                    return main.getOffsetCapability(capability, context, bounding.mainPosOffset);
                }
            }
            return null;
        }, MekanismBlocks.BOUNDING_BLOCK.value());
    }

    public static <CAP, CONTEXT extends @Nullable Object> void alwaysProxyCapability(RegisterCapabilitiesEvent event, BlockCapability<CAP, CONTEXT> capability) {
        event.registerBlock(capability, (level, _, _, boundingBlock, context) -> {
            if (boundingBlock instanceof TileEntityBoundingBlock bounding) {
                BlockPos mainPos = bounding.getMainPos();
                if (mainPos != null) {
                    return WorldUtils.getCapability(level, capability, mainPos, context);
                }
            }
            return null;
        }, MekanismBlocks.BOUNDING_BLOCK.value());
    }
}