package mekanism.common.tile.multiblock;

import java.util.UUID;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;

public class TileEntitySuperheatingElement extends TileEntityInternalMultiblock {

    public TileEntitySuperheatingElement() {
        super(MekanismBlocks.SUPERHEATING_ELEMENT);
    }

    @Override
    public void setMultiblock(UUID id) {
        boolean packet = false;
        if (id == null && multiblockUUID != null) {
            BoilerMultiblockData.hotMap.removeBoolean(multiblockUUID);
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
        if (multiblockUUID != null && BoilerMultiblockData.hotMap.containsKey(multiblockUUID)) {
            newHot = BoilerMultiblockData.hotMap.getBoolean(multiblockUUID);
        }
        setActive(newHot);
    }
}