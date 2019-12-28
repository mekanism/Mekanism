package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ThermalEvaporationControllerContainer extends MekanismTileContainer<TileEntityThermalEvaporationController> {

    public ThermalEvaporationControllerContainer(int id, PlayerInventory inv, TileEntityThermalEvaporationController tile) {
        super(MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, id, inv, tile);
    }

    public ThermalEvaporationControllerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityThermalEvaporationController.class));
    }
}