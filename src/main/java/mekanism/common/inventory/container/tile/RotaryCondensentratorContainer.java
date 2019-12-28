package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class RotaryCondensentratorContainer extends MekanismTileContainer<TileEntityRotaryCondensentrator> {

    public RotaryCondensentratorContainer(int id, PlayerInventory inv, TileEntityRotaryCondensentrator tile) {
        super(MekanismContainerTypes.ROTARY_CONDENSENTRATOR, id, inv, tile);
    }

    public RotaryCondensentratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityRotaryCondensentrator.class));
    }
}