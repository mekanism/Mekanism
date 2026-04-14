package mekanism.common.tile.multiblock;

import java.util.LinkedList;
import java.util.Queue;
import mekanism.api.SerializationConstants;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.multiblock.MekanismMultiblocks;
import mekanism.common.lib.multiblock.MultiblockType;
import mekanism.common.particle.SPSOrbitEffect;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class TileEntitySPSCasing extends TileEntityMultiblock<SPSMultiblockData> {

    public final Queue<SPSOrbitEffect> orbitEffects = new LinkedList<>();

    private boolean handleSound;
    private boolean prevActive;

    public TileEntitySPSCasing(BlockPos pos, BlockState state) {
        this(MekanismBlocks.SPS_CASING, pos, state);
    }

    public TileEntitySPSCasing(Holder<Block> provider, BlockPos pos, BlockState state) {
        super(provider, pos, state);
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (isMaster()) {
            //If we are still the master tick each effect and remove it if it is done
            orbitEffects.removeIf(SPSOrbitEffect::tick);
        } else {
            //Otherwise, if we are no longer master just clear them all directly rather than removing each in a removeIf
            orbitEffects.clear();
        }
    }

    @Override
    protected boolean onUpdateServer(SPSMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        boolean active = multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.lastProcessed > 0;
        if (active != prevActive) {
            prevActive = active;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    protected void structureChanged(SPSMultiblockData multiblock) {
        super.structureChanged(multiblock);
        //Transition the orbit effects over to the new multiblock
        if (multiblock.isFormed()) {
            for (SPSOrbitEffect orbitEffect : orbitEffects) {
                orbitEffect.updateMultiblock(multiblock);
            }
        }
    }

    @Override
    public SPSMultiblockData createMultiblock() {
        return new SPSMultiblockData(this);
    }

    @Override
    public MultiblockType<SPSMultiblockData> getMultiblockType() {
        return MekanismMultiblocks.SPS;
    }

    @Override
    protected boolean canPlaySound() {
        SPSMultiblockData multiblock = getMultiblock();
        return multiblock.isFormed() && handleSound;
    }

    @Override
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        SPSMultiblockData multiblock = getMultiblock();
        output.putBoolean(SerializationConstants.HANDLE_SOUND, multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.lastProcessed > 0);
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        super.handleUpdateTag(input);
        handleSound = input.getBooleanOr(SerializationConstants.HANDLE_SOUND, handleSound);
    }
}