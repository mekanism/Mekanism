package mekanism.common.inventory.container.tile.energy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class InductionMatrixContainer extends MekanismEnergyContainer<TileEntityInductionCasing> {

    public InductionMatrixContainer(int id, PlayerInventory inv, TileEntityInductionCasing tile) {
        super(MekanismContainerTypes.INDUCTION_MATRIX, id, inv, tile);
    }

    public InductionMatrixContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityInductionCasing.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotCharge(tile, 0, 146, 20));
        addSlot(new SlotDischarge(tile, 1, 146, 51));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new InductionMatrixContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.induction_matrix");
    }
}