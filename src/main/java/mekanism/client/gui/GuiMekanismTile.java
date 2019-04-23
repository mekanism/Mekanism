package mekanism.client.gui;

import java.util.List;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiMekanismTile<TILE extends TileEntityContainerBlock> extends GuiMekanism {

    protected TILE tileEntity;

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
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (tileEntity instanceof ISideConfiguration) {
            Slot hovering = null;
            for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
                Slot slot = inventorySlots.inventorySlots.get(i);
                if (isMouseOverSlot(slot, mouseX, mouseY)) {
                    hovering = slot;
                    break;
                }
            }

            ItemStack stack = mc.player.inventory.getItemStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && hovering != null) {
                SideData data = getFromSlot(hovering);
                if (data != null) {
                    drawHoveringText(data.color + data.localize() + " (" + data.color.getColoredName() + ")", xAxis,
                          yAxis);
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