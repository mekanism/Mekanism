package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ChemicalInjectionChamberContainer extends MekanismTileContainer<TileEntityChemicalInjectionChamber> {

    public ChemicalInjectionChamberContainer(int id, PlayerInventory inv, TileEntityChemicalInjectionChamber tile) {
        super(MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER, id, inv, tile);
    }

    public ChemicalInjectionChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalInjectionChamber.class));
    }
}