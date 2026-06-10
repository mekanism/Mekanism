package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.energy.BasicEnergyHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;

public class TileEntitySeismicVibrator extends TileEntityMekanism implements IBoundingBlock {

    public int clientPiston;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntitySeismicVibrator> energyContainer;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntitySeismicVibrator(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SEISMIC_VIBRATOR, pos, state);
        cacheCoord();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.input(this, listener);
        return new BasicEnergyHolder(energyContainer, facingSupplier, BACK_ONLY);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateClient(Level level) {
        super.onUpdateClient(level);
        if (getActive()) {
            clientPiston++;
        }
        updateActiveVibrators();
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        boolean isActive = false;
        if (canFunction()) {
            int energyPerTick = energyContainer.getEnergyPerTick();
            try (Transaction transaction = Transaction.openRoot()) {
                if (energyContainer.extract(energyPerTick, transaction, AutomationType.INTERNAL) == energyPerTick) {
                    isActive = true;
                    transaction.commit();
                    if (ticker % (2 * SharedConstants.TICKS_PER_SECOND) == 0) {//Every two seconds allow for a new vibration to be sent
                        level.gameEvent(null, MekanismGameEvents.SEISMIC_VIBRATION, worldPosition);
                    }
                }
            }
        }
        setActive(isActive);
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

    public MachineEnergyContainer<TileEntitySeismicVibrator> energyContainer() {
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
        Level level = getWorldNN();
        if (level.isOutsideBuildHeight(y)) {
            throw new ComputerException("Y '%d' is out of range must be between %d and %d. (Inclusive)", y, level.getMinY(), level.getMaxY());
        }
        BlockPos targetPos = getVerticalPos(chunkRelativeX, y, chunkRelativeZ);
        return level.getBlockState(targetPos);
    }

    @ComputerMethod(methodDescription = "Get a column info, table key is the Y level")
    Map<Integer, BlockState> getColumnAt(int chunkRelativeX, int chunkRelativeZ) throws ComputerException {
        validateVibrating();
        Level level = getWorldNN();
        Int2ObjectMap<BlockState> blocks = new Int2ObjectOpenHashMap<>();
        BlockPos minPos = getVerticalPos(chunkRelativeX, level.getMinY(), chunkRelativeZ);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, new BlockPos(minPos.getX(), level.getMaxY() + 1, minPos.getZ()))) {
            blocks.put(pos.getY(), level.getBlockState(pos));
        }
        return blocks;
    }
    //End computer related methods
}
