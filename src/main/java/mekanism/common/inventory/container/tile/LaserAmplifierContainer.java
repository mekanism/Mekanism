package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLaserAmplifier;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class LaserAmplifierContainer extends MekanismTileContainer<TileEntityLaserAmplifier> {

    public LaserAmplifierContainer(int id, PlayerInventory inv, TileEntityLaserAmplifier tile) {
        super(MekanismContainerTypes.LASER_AMPLIFIER, id, inv, tile);
    }

    public LaserAmplifierContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLaserAmplifier.class));
    }
}