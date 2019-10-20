package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ChemicalOxidizerContainer extends MekanismTileContainer<TileEntityChemicalOxidizer> {

    public ChemicalOxidizerContainer(int id, PlayerInventory inv, TileEntityChemicalOxidizer tile) {
        super(MekanismContainerTypes.CHEMICAL_OXIDIZER, id, inv, tile);
    }

    public ChemicalOxidizerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalOxidizer.class));
    }
}