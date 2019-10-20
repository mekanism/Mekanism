package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class MetallurgicInfuserContainer extends MekanismTileContainer<TileEntityMetallurgicInfuser> {

    public MetallurgicInfuserContainer(int id, PlayerInventory inv, TileEntityMetallurgicInfuser tile) {
        super(MekanismContainerTypes.METALLURGIC_INFUSER, id, inv, tile);
    }

    public MetallurgicInfuserContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityMetallurgicInfuser.class));
    }
}