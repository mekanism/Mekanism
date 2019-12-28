package mekanism.generators.common.inventory.container.turbine;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class TurbineContainer extends MekanismTileContainer<TileEntityTurbineCasing> {

    public TurbineContainer(int id, PlayerInventory inv, TileEntityTurbineCasing tile) {
        super(GeneratorsContainerTypes.INDUSTRIAL_TURBINE, id, inv, tile);
    }

    public TurbineContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityTurbineCasing.class));
    }
}