package mekanism.client.gui;

import java.util.Set;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanism<CONTAINER> {

    //TODO: Potentially replace usages of this with getTileEntity() and make getTileEntity return container.getTileEntity()
    protected final TILE tile;

    protected GuiMekanismTile(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        tile = container.getTileEntity();
    }

    public TILE getTileEntity() {
        return tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (tile instanceof ISideConfiguration) {
            ItemStack stack = minecraft.player.inventory.getItemStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                for (int i = 0; i < container.inventorySlots.size(); i++) {
                    Slot slot = container.inventorySlots.get(i);
                    if (isMouseOverSlot(slot, mouseX, mouseY)) {
                        DataType data = getFromSlot(slot);
                        if (data != null) {
                            displayTooltip(TextComponentUtil.build(data.getColor(), data, " (", data.getColor().getColoredName(), ")"),
                                  mouseX - guiLeft, mouseY - guiTop);
                        }
                        break;
                    }
                }
            }
        }
    }

    private DataType getFromSlot(Slot slot) {
        if (slot.slotNumber < tile.getSlots() && slot instanceof InventoryContainerSlot) {
            ISideConfiguration config = (ISideConfiguration) tile;
            ConfigInfo info = config.getConfig().getConfig(TransmissionType.ITEM);
            if (info != null) {
                Set<DataType> supportedDataTypes = info.getSupportedDataTypes();
                IInventorySlot inventorySlot = ((InventoryContainerSlot) slot).getInventorySlot();
                for (DataType type : supportedDataTypes) {
                    ISlotInfo slotInfo = info.getSlotInfo(type);
                    if (slotInfo instanceof InventorySlotInfo && ((InventorySlotInfo) slotInfo).hasSlot(inventorySlot)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }
}