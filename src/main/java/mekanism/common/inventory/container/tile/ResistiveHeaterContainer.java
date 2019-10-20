package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityResistiveHeater;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ResistiveHeaterContainer extends MekanismTileContainer<TileEntityResistiveHeater> {

    public ResistiveHeaterContainer(int id, PlayerInventory inv, TileEntityResistiveHeater tile) {
        super(MekanismContainerTypes.RESISTIVE_HEATER, id, inv, tile);
    }

    public ResistiveHeaterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityResistiveHeater.class));
    }
}