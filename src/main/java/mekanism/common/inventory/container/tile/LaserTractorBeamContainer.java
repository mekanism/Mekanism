package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class LaserTractorBeamContainer extends MekanismTileContainer<TileEntityLaserTractorBeam> {

    public LaserTractorBeamContainer(int id, PlayerInventory inv, TileEntityLaserTractorBeam tile) {
        super(MekanismContainerTypes.LASER_TRACTOR_BEAM, id, inv, tile);
    }

    public LaserTractorBeamContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLaserTractorBeam.class));
    }
}