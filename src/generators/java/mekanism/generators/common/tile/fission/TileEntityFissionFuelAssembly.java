package mekanism.generators.common.tile.fission;

import java.util.UUID;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityFissionFuelAssembly extends TileEntityInternalMultiblock {

    public TileEntityFissionFuelAssembly() {
        super(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY);
    }

    @Override
    public void setMultiblock(UUID id) {
        boolean packet = false;
        if (id == null && multiblockUUID != null) {
            FissionReactorMultiblockData.burningMap.removeBoolean(multiblockUUID);
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
        if (multiblockUUID != null && FissionReactorMultiblockData.burningMap.containsKey(multiblockUUID)) {
            newHot = FissionReactorMultiblockData.burningMap.getBoolean(multiblockUUID);
        }
        setActive(newHot);
    }
}
