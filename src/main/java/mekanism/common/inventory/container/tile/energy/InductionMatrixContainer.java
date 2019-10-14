package mekanism.common.inventory.container.tile.energy;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityInductionCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class InductionMatrixContainer extends MekanismEnergyContainer<TileEntityInductionCasing> {

    public InductionMatrixContainer(int id, PlayerInventory inv, TileEntityInductionCasing tile) {
        super(MekanismContainerTypes.INDUCTION_MATRIX, id, inv, tile);
    }

    public InductionMatrixContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityInductionCasing.class));
    }
}