package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntitySeismicVibrator extends TileEntityMekanism implements IBoundingBlock {

    public int clientPiston;

    private MachineEnergyContainer<TileEntitySeismicVibrator> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntitySeismicVibrator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SEISMIC_VIBRATOR, pos, state);
        cacheCoord();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            clientPiston++;
        }
        updateActiveVibrators();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        if (canFunction()) {
            long energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) == energyPerTick) {
                setActive(true);
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                if (ticker % (2 * SharedConstants.TICKS_PER_SECOND) == 0) {//Every two seconds allow for a new vibration to be sent
                    level.gameEvent(null, MekanismGameEvents.SEISMIC_VIBRATION, worldPosition);
                }
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }
        updateActiveVibrators();
        return sendUpdatePacket;
    }

    private void updateActiveVibrators() {
        if (getActive()) {
            Mekanism.activeVibrators.add(getTileGlobalPos());
        } else {
            Mekanism.activeVibrators.remove(getTileGlobalPos());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Mekanism.activeVibrators.remove(getTileGlobalPos());
    }

    public MachineEnergyContainer<TileEntitySeismicVibrator> getEnergyContainer() {
        return energyContainer;
    }

    //Computer related methods
    @ComputerMethod
    boolean isVibrating() {
        return getActive();
    }

    private void validateVibrating() throws ComputerException {
        if (!isVibrating()) {
            throw new ComputerException("Seismic Vibrator is not currently vibrating any chunks");
        }
    }

    private BlockPos getVerticalPos(int chunkRelativeX, int y, int chunkRelativeZ) throws ComputerException {
        if (chunkRelativeX < 0 || chunkRelativeX > 15) {
            throw new ComputerException("Chunk Relative X '%d' is out of range must be between 0 and 15. (Inclusive)", chunkRelativeX);
        } else if (chunkRelativeZ < 0 || chunkRelativeZ > 15) {
            throw new ComputerException("Chunk Relative Z '%d' is out of range must be between 0 and 15. (Inclusive)", chunkRelativeZ);
        }
        int x = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(worldPosition.getX()), chunkRelativeX);
        int z = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(worldPosition.getZ()), chunkRelativeZ);
        return new BlockPos(x, y, z);
    }

    @ComputerMethod
    BlockState getBlockAt(int chunkRelativeX, int y, int chunkRelativeZ) throws ComputerException {
        validateVibrating();
        if (level.isOutsideBuildHeight(y)) {
            throw new ComputerException("Y '%d' is out of range must be between %d and %d. (Inclusive)", y, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
        }
        BlockPos targetPos = getVerticalPos(chunkRelativeX, y, chunkRelativeZ);
        return level.getBlockState(targetPos);
    }

    @ComputerMethod(methodDescription = "Get a column info, table key is the Y level")
    Map<Integer, BlockState> getColumnAt(int chunkRelativeX, int chunkRelativeZ) throws ComputerException {
        validateVibrating();
        Int2ObjectMap<BlockState> blocks = new Int2ObjectOpenHashMap<>();
        BlockPos minPos = getVerticalPos(chunkRelativeX, level.getMinBuildHeight(), chunkRelativeZ);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, new BlockPos(minPos.getX(), level.getMaxBuildHeight(), minPos.getZ()))) {
            blocks.put(pos.getY(), level.getBlockState(pos));
        }
        return blocks;
    }
    //End computer related methods
}
