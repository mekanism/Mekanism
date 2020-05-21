package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ThermoelectricBoilerContainer extends MekanismTileContainer<TileEntityBoilerCasing> {

    public ThermoelectricBoilerContainer(int id, PlayerInventory inv, TileEntityBoilerCasing tile) {
        super(MekanismContainerTypes.THERMOELECTRIC_BOILER, id, inv, tile);
    }

    public ThermoelectricBoilerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityBoilerCasing.class));
    }

    @Override
    protected int getInventoryXOffset() {
        return super.getInventoryXOffset() + 20;
    }
}