package mekanism.common.inventory.container.tile.energy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.tile.energy_cube.TileEntityEnergyCube;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class EnergyCubeContainer extends MekanismEnergyContainer<TileEntityEnergyCube> {

    public EnergyCubeContainer(int id, PlayerInventory inv, TileEntityEnergyCube tile) {
        super(MekanismContainerTypes.ENERGY_CUBE, id, inv, tile);
    }

    public EnergyCubeContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityEnergyCube.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotCharge(tile, 0, 143, 35));
        addSlot(new SlotDischarge(tile, 1, 17, 35));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new EnergyCubeContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.energy_cube");
    }
}