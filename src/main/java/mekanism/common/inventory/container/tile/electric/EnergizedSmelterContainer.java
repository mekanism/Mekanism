package mekanism.common.inventory.container.tile.electric;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class EnergizedSmelterContainer extends ElectricMachineContainer<SmeltingRecipe, TileEntityEnergizedSmelter> {

    public EnergizedSmelterContainer(int id, PlayerInventory inv, TileEntityEnergizedSmelter tile) {
        super(MekanismContainerTypes.ENERGIZED_SMELTER, id, inv, tile);
    }

    public EnergizedSmelterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityEnergizedSmelter.class));
    }
}