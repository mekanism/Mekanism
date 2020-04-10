package mekanism.common.tile;

import java.util.UUID;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.registries.MekanismBlocks;

public class TileEntitySuperheatingElement extends TileEntityInternalMultiblock {

    public TileEntitySuperheatingElement() {
        super(MekanismBlocks.SUPERHEATING_ELEMENT);
    }

    @Override
    public void setMultiblock(UUID id) {
        boolean packet = false;
        if (id == null && multiblockUUID != null) {
            SynchronizedBoilerData.hotMap.removeBoolean(multiblockUUID);
            packet = true;
        } else if (id != null && multiblockUUID == null) {
            packet = true;
        }
        super.setMultiblock(id);
        if (packet && !isRemote()) {
            sendUpdatePacket();
        }
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean newHot = false;
        if (multiblockUUID != null && SynchronizedBoilerData.hotMap.containsKey(multiblockUUID)) {
            newHot = SynchronizedBoilerData.hotMap.getBoolean(multiblockUUID);
        }
        setActive(newHot);
    }
}