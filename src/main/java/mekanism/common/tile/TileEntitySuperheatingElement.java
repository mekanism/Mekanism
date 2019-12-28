package mekanism.common.tile;

import mekanism.common.Mekanism;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.ITickableTileEntity;

public class TileEntitySuperheatingElement extends TileEntityInternalMultiblock implements ITickableTileEntity {

    public boolean prevHot;

    public TileEntitySuperheatingElement() {
        super(MekanismBlocks.SUPERHEATING_ELEMENT);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void setMultiblock(String id) {
        boolean packet = false;
        if (id == null && multiblockUUID != null) {
            SynchronizedBoilerData.clientHotMap.remove(multiblockUUID);
            packet = true;
        } else if (id != null && multiblockUUID == null) {
            packet = true;
        }

        super.setMultiblock(id);

        if (packet && !isRemote()) {
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    @Override
    public void onUpdate() {
        if (isRemote()) {
            boolean newHot = false;
            if (multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(multiblockUUID) != null) {
                newHot = SynchronizedBoilerData.clientHotMap.get(multiblockUUID);
            }
            if (prevHot != newHot) {
                MekanismUtils.updateBlock(getWorld(), getPos());
                prevHot = newHot;
            }
        }
    }
}