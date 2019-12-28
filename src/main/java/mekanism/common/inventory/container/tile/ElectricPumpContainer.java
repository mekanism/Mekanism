package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityElectricPump;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ElectricPumpContainer extends MekanismTileContainer<TileEntityElectricPump> {

    public ElectricPumpContainer(int id, PlayerInventory inv, TileEntityElectricPump tile) {
        super(MekanismContainerTypes.ELECTRIC_PUMP, id, inv, tile);
    }

    public ElectricPumpContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityElectricPump.class));
    }
}