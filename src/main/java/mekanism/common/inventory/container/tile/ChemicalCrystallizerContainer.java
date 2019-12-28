package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ChemicalCrystallizerContainer extends MekanismTileContainer<TileEntityChemicalCrystallizer> {

    public ChemicalCrystallizerContainer(int id, PlayerInventory inv, TileEntityChemicalCrystallizer tile) {
        super(MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, id, inv, tile);
    }

    public ChemicalCrystallizerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalCrystallizer.class));
    }
}