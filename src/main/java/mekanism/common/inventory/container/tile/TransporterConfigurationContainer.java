package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class TransporterConfigurationContainer extends EmptyTileContainer<TileEntityMekanism> {

    public TransporterConfigurationContainer(int id, PlayerInventory inv, TileEntityMekanism tile) {
        super(MekanismContainerTypes.TRANSPORTER_CONFIGURATION, id, inv, tile);
    }

    public TransporterConfigurationContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityMekanism.class));
    }

    @Override
    protected void addContainerTrackers() {
        //NO-OP for now, eventually have this maybe add stuff to sync
    }
}