package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntitySeismicVibrator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class SeismicVibratorContainer extends MekanismTileContainer<TileEntitySeismicVibrator> {

    public SeismicVibratorContainer(int id, PlayerInventory inv, TileEntitySeismicVibrator tile) {
        super(MekanismContainerTypes.SEISMIC_VIBRATOR, id, inv, tile);
    }

    public SeismicVibratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntitySeismicVibrator.class));
    }
}