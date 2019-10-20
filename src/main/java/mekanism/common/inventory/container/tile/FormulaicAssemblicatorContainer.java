package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class FormulaicAssemblicatorContainer extends MekanismTileContainer<TileEntityFormulaicAssemblicator> {

    public FormulaicAssemblicatorContainer(int id, PlayerInventory inv, TileEntityFormulaicAssemblicator tile) {
        super(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, id, inv, tile);
    }

    public FormulaicAssemblicatorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFormulaicAssemblicator.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}