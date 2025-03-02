package mekanism.common.tile.multiblock;

import java.util.LinkedList;
import java.util.Queue;
import mekanism.api.SerializationConstants;
import mekanism.common.Mekanism;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.particle.SPSOrbitEffect;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
    public MultiblockManager<SPSMultiblockData> getManager() {
        return Mekanism.spsManager;
    }

    @Override
    protected boolean canPlaySound() {
        SPSMultiblockData multiblock = getMultiblock();
        return multiblock.isFormed() && handleSound;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        SPSMultiblockData multiblock = getMultiblock();
        updateTag.putBoolean(SerializationConstants.HANDLE_SOUND, multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.lastProcessed > 0);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setBooleanIfPresent(tag, SerializationConstants.HANDLE_SOUND, value -> handleSound = value);
    }
}