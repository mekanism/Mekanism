package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.ITickable;

public class TileEntitySuperheatingElement extends TileEntityInternalMultiblock implements ITickable {

    public boolean prevHot;

    @Override
    public void update() {
        super.update();
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

        if (packet && !world.isRemote) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
        }
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            boolean newHot = false;

            if (multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(multiblockUUID) != null) {
                newHot = SynchronizedBoilerData.clientHotMap.get(multiblockUUID);
            }

            if (prevHot != newHot) {
                MekanismUtils.updateBlock(world, getPos());

                prevHot = newHot;
            }
        }
    }
}
