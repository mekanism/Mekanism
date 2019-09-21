package mekanism.generators.common.inventory.container.fuel;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.FuelHandler;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class GasBurningGeneratorContainer extends FuelGeneratorContainer<TileEntityGasGenerator> {

    public GasBurningGeneratorContainer(int id, PlayerInventory inv, TileEntityGasGenerator tile) {
        super(GeneratorsContainerTypes.GAS_BURNING_GENERATOR, id, inv, tile);
    }

    public GasBurningGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityGasGenerator.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 17, 35));
        addSlot(new SlotCharge(tile, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        if (slotStack.getItem() instanceof IGasItem) {
            GasStack gasStack = ((IGasItem) slotStack.getItem()).getGas(slotStack);
            return !gasStack.isEmpty() && !FuelHandler.getFuel(gasStack.getGas()).isEmpty();
        }
        return false;
    }
}