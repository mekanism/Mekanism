package mekanism.common.inventory.container.tile.fluid;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class DynamicTankContainer extends MekanismFluidContainer<TileEntityDynamicTank> {

    public DynamicTankContainer(int id, PlayerInventory inv, TileEntityDynamicTank tile) {
        super(MekanismContainerTypes.DYNAMIC_TANK, id, inv, tile);
    }

    public DynamicTankContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDynamicTank.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 146, 20));
        addSlot(new SlotOutput(tile, 1, 146, 51));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.dynamic_tank");
    }
}