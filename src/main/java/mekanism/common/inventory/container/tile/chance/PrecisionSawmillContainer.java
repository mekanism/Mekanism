package mekanism.common.inventory.container.tile.chance;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class PrecisionSawmillContainer extends ChanceMachineContainer<SawmillRecipe, TileEntityPrecisionSawmill> {

    public PrecisionSawmillContainer(int id, PlayerInventory inv, TileEntityPrecisionSawmill tile) {
        super(MekanismContainerTypes.PRECISION_SAWMILL, id, inv, tile);
    }

    public PrecisionSawmillContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityPrecisionSawmill.class));
    }
}