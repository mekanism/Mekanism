package mekanism.generators.common.inventory.container.fuel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.FuelHandler;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

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
            return gasStack != null && FuelHandler.getFuel(gasStack.getGas()) != null;
        }
        return false;
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new GasBurningGeneratorContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.gas_burning_generator");
    }
}