package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ChemicalDissolutionChamberContainer extends MekanismTileContainer<TileEntityChemicalDissolutionChamber> {

    public ChemicalDissolutionChamberContainer(int id, PlayerInventory inv, TileEntityChemicalDissolutionChamber tile) {
        super(MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, id, inv, tile);
    }

    public ChemicalDissolutionChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalDissolutionChamber.class));
    }
}