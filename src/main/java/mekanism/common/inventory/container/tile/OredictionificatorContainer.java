package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class OredictionificatorContainer extends MekanismTileContainer<TileEntityOredictionificator> {

    public OredictionificatorContainer(int id, PlayerInventory inv, TileEntityOredictionificator tile) {
        super(MekanismContainerTypes.OREDICTIONIFICATOR, id, inv, tile);
    }

    public OredictionificatorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityOredictionificator.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}