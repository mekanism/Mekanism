package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntitySecurityDesk;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class SecurityDeskContainer extends MekanismTileContainer<TileEntitySecurityDesk> {

    public SecurityDeskContainer(int id, PlayerInventory inv, TileEntitySecurityDesk tile) {
        super(MekanismContainerTypes.SECURITY_DESK, id, inv, tile);
    }

    public SecurityDeskContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntitySecurityDesk.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}