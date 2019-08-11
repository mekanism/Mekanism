package mekanism.client.gui;

import java.util.List;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiMekanismTile<TILE extends TileEntityMekanism> extends GuiMekanism {

    protected final TILE tileEntity;

    public GuiMekanismTile(TILE tile, Container container) {
        super(container);
        tileEntity = tile;
    }

    public TILE getTileEntity() {
        return tileEntity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (tileEntity instanceof ISideConfiguration) {
            Slot hovering = null;
            for (int i = 0; i < container.inventorySlots.size(); i++) {
                Slot slot = container.inventorySlots.get(i);
                if (isMouseOverSlot(slot, mouseX, mouseY)) {
                    hovering = slot;
                    break;
                }
            }

            ItemStack stack = minecraft.player.inventory.getItemStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && hovering != null) {
                SideData data = getFromSlot(hovering);
                if (data != null) {
                    displayTooltip(TextComponentUtil.build(data.color, data, " (", data.color.getColoredName(), ")"), xAxis, yAxis);
                }
            }
        }
    }

    private SideData getFromSlot(Slot slot) {
        if (slot.slotNumber < tileEntity.getSizeInventory()) {
            ISideConfiguration config = (ISideConfiguration) tileEntity;
            List<SideData> datas = config.getConfig().getOutputs(TransmissionType.ITEM);
            if (datas != null) {
                for (SideData data : datas) {
                    for (int id : data.availableSlots) {
                        if (id == slot.getSlotIndex()) {
                            return data;
                        }
                    }
                }
            }
        }
        return null;
    }
}