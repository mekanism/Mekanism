package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class QuantumEntangloporterContainer extends MekanismTileContainer<TileEntityQuantumEntangloporter> implements IEmptyContainer {

    public QuantumEntangloporterContainer(int id, PlayerInventory inv, TileEntityQuantumEntangloporter tile) {
        super(MekanismContainerTypes.QUANTUM_ENTANGLOPORTER, id, inv, tile);
    }

    public QuantumEntangloporterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityQuantumEntangloporter.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}