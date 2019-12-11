package mekanism.client.gui.robit;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitRepair extends GuiRobit<RepairRobitContainer> implements IContainerListener {

    private TextFieldWidget itemNameField;

    public GuiRobitRepair(RepairRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        minecraft.keyboardListener.enableRepeatEvents(true);
        addButton(itemNameField = new TextFieldWidget(font, guiLeft + 62, guiTop + 24, 103, 12, ""));
        itemNameField.setTextColor(-1);
        itemNameField.setDisabledTextColour(-1);
        itemNameField.setEnableBackgroundDrawing(false);
        itemNameField.setMaxStringLength(30);
        container.removeListener(this);
        container.addListener(this);
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.keyboardListener.enableRepeatEvents(false);
        container.removeListener(this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        drawString(TextComponentUtil.translate("container.repair"), 60, 6, 0x404040);

        int maximumCost = container.getMaximumCost();
        if (maximumCost > 0) {
            //TODO: Verify this works as intended
            int k = 0x80FF20;
            boolean flag = true;
            ITextComponent component = TextComponentUtil.translate("container.repair.cost", maximumCost);
            if (maximumCost >= 40 && !minecraft.player.isCreative()) {
                component = TextComponentUtil.translate("container.repair.expensive");
                k = 0xFF6060;
            } else if (!container.getSlot(2).getHasStack()) {
                flag = false;
            } else if (!container.getSlot(2).canTakeStack(playerInventory.player)) {
                k = 0xFF6060;
            }

            if (flag) {
                int width = this.xSize - 8 - getStringWidth(component) - 2;
                fill(width - 2, 67, this.xSize - 8, 79, 0x4F000000);
                font.drawStringWithShadow(component.getFormattedText(), (float) width, 69.0F, k);
            }
        }
        GlStateManager.enableLighting();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (itemNameField.charTyped(c, i)) {
            container.updateItemName(itemNameField.getText());
            minecraft.player.connection.sendPacket(new CRenameItemPacket(itemNameField.getText()));
            return true;
        }
        return super.charTyped(c, i);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.REPAIR;
    }

    @Override
    protected String getBackgroundImage() {
        return "robit_repair.png";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 59, guiTop + 20, 0, ySize + (container.getSlot(0).getHasStack() ? 0 : 16), 110, 16);
        if ((container.getSlot(0).getHasStack() || container.getSlot(1).getHasStack()) && !container.getSlot(2).getHasStack()) {
            drawTexturedRect(guiLeft + 99, guiTop + 45, xSize + 18, 36, 28, 21);
        }
    }

    @Override
    public void sendAllContents(@Nonnull Container container, @Nonnull NonNullList<ItemStack> list) {
        sendSlotContents(container, 0, container.getSlot(0).getStack());
    }

    @Override
    public void sendSlotContents(@Nonnull Container container, int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            itemNameField.setText(itemstack.isEmpty() ? "" : itemstack.getDisplayName().getFormattedText());
            itemNameField.setEnabled(!itemstack.isEmpty());
            if (!itemstack.isEmpty()) {
                this.container.updateItemName(itemNameField.getText());
                minecraft.player.connection.sendPacket(new CRenameItemPacket(itemNameField.getText()));
            }
        }
    }

    @Override
    public void sendWindowProperty(@Nonnull Container containerIn, int varToUpdate, int newValue) {
    }
}