package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiMekanismTile<TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> extends GuiMekanism<CONTAINER> {

    protected final TILE tile;

    protected GuiMekanismTile(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        tile = container.getTileEntity();
    }

    public TILE getTileEntity() {
        return tile;
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        if (tile instanceof ISideConfiguration) {
            ItemStack stack = getMinecraft().player.inventory.getItemStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator) {
                for (int i = 0; i < container.inventorySlots.size(); i++) {
                    Slot slot = container.inventorySlots.get(i);
                    if (isMouseOverSlot(slot, mouseX, mouseY)) {
                        DataType data = getFromSlot(slot);
                        if (data != null) {
                            displayTooltip(matrix, MekanismLang.GENERIC_PARENTHESIS.translateColored(data.getColor(), data.getColor().getName()), mouseX - getGuiLeft(), mouseY - getGuiTop());
                        }
                        break;
                    }
                }
            }
        }
    }

    public void renderTitleText(MatrixStack matrix, int y) {
        drawTitleText(matrix, tile.getName(), y);
    }

    public void renderTitleText(MatrixStack matrix) {
        renderTitleText(matrix, 6);
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