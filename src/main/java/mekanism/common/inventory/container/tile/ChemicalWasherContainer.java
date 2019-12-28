package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityChemicalWasher;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ChemicalWasherContainer extends MekanismTileContainer<TileEntityChemicalWasher> {

    public ChemicalWasherContainer(int id, PlayerInventory inv, TileEntityChemicalWasher tile) {
        super(MekanismContainerTypes.CHEMICAL_WASHER, id, inv, tile);
    }

    public ChemicalWasherContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalWasher.class));
    }
}