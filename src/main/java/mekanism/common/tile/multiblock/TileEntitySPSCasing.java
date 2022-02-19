package mekanism.common.tile.multiblock;

import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.particle.SPSOrbitEffect;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileEntitySPSCasing extends TileEntityMultiblock<SPSMultiblockData> {

    public final Queue<SPSOrbitEffect> orbitEffects = new LinkedList<>();

    private boolean handleSound;
    private boolean prevActive;

    public TileEntitySPSCasing() {
        super(MekanismBlocks.SPS_CASING);
    }

    public TileEntitySPSCasing(IBlockProvider provider) {
        super(provider);
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

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        SPSMultiblockData multiblock = getMultiblock();
        updateTag.putBoolean(NBTConstants.HANDLE_SOUND, multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.lastProcessed > 0);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HANDLE_SOUND, value -> handleSound = value);
    }
}