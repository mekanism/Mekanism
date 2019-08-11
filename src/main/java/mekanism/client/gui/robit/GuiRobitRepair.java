package mekanism.client.gui.robit;

import com.mojang.blaze3d.platform.GlStateManager;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.robit.ContainerRobitRepair;
import mekanism.common.util.LangUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.java.games.input.Keyboard;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRobitRepair extends GuiRobit implements IContainerListener {

    private final RepairContainer repairContainer;
    private final PlayerInventory playerInventory;

    private TextFieldWidget itemNameField;

    public GuiRobitRepair(PlayerInventory inventory, EntityRobit entity) {
        super(entity, new ContainerRobitRepair(inventory, entity));
        playerInventory = inventory;
        repairContainer = (ContainerRobitRepair) inventorySlots;
    }

    @Override
    public void init() {
        super.init();
        Keyboard.enableRepeatEvents(true);
        itemNameField = new TextFieldWidget(font, guiLeft + 62, guiTop + 24, 103, 12, "");
        itemNameField.setTextColor(-1);
        itemNameField.setDisabledTextColour(-1);
        itemNameField.setEnableBackgroundDrawing(false);
        itemNameField.setMaxStringLength(30);
        inventorySlots.removeListener(this);
        inventorySlots.addListener(this);
    }

    @Override
    public void onClose() {
        super.onClose();
        Keyboard.enableRepeatEvents(false);
        inventorySlots.removeListener(this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        drawString(TextComponentUtil.build(Translation.of("container.repair")), 60, 6, 0x404040);

        if (repairContainer.maximumCost > 0) {
            int k = 8453920;
            boolean flag = true;
            String s = I18n.format("container.repair.cost", repairContainer.maximumCost);

            if (repairContainer.maximumCost >= 40 && !minecraft.player.isCreative()) {
                s = LangUtils.localize("container.repair.expensive");
                k = 16736352;
            } else if (!repairContainer.getSlot(2).getHasStack()) {
                flag = false;
            } else if (!repairContainer.getSlot(2).canTakeStack(playerInventory.player)) {
                k = 16736352;
            }

            if (flag) {
                int l = -16777216 | (k & 16579836) >> 2 | k & -16777216;
                int i1 = xSize - 25 - 8 - getStringWidth(s);
                byte b0 = 67;

                if (font.getUnicodeFlag()) {
                    drawRect(i1 - 3, b0 - 2, xSize - 25 - 7, b0 + 10, 0xFF000000);
                    drawRect(i1 - 2, b0 - 1, xSize - 25 - 8, b0 + 9, 0xFF3B3B3B);
                } else {
                    drawString(s, i1, b0 + 1, l);
                    drawString(s, i1 + 1, b0, l);
                    drawString(s, i1 + 1, b0 + 1, l);
                }
                drawString(s, i1, b0, k);
            }
        }
        GlStateManager.enableLighting();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (itemNameField.charTyped(c, i)) {
            repairContainer.updateItemName(itemNameField.getText());
            minecraft.player.connection.sendPacket(new CCustomPayloadPacket("MC|ItemName", new PacketBuffer(Unpooled.buffer()).writeString(itemNameField.getText())));
            return true;
        }
        return super.charTyped(c, i);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        itemNameField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.REPAIR;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        super.render(mouseX, mouseY, partialTick);
        GlStateManager.disableLighting();
        itemNameField.drawTextBox();
        GlStateManager.enableLighting();
    }

    @Override
    protected String getBackgroundImage() {
        return "GuiRobitRepair.png";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 59, guiTop + 20, 0, ySize + (repairContainer.getSlot(0).getHasStack() ? 0 : 16), 110, 16);
        if ((repairContainer.getSlot(0).getHasStack() || repairContainer.getSlot(1).getHasStack()) && !repairContainer.getSlot(2).getHasStack()) {
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
            itemNameField.setText(itemstack.isEmpty() ? "" : itemstack.getDisplayName());
            itemNameField.setEnabled(!itemstack.isEmpty());
            if (!itemstack.isEmpty()) {
                repairContainer.updateItemName(itemNameField.getText());
                minecraft.player.connection.sendPacket(new CCustomPayloadPacket("MC|ItemName", new PacketBuffer(Unpooled.buffer()).writeString(itemNameField.getText())));
            }
        }
    }

    @Override
    public void sendWindowProperty(@Nonnull Container containerIn, int varToUpdate, int newValue) {
    }

    @Override
    public void sendAllWindowProperties(@Nonnull Container containerIn, @Nonnull IInventory inventory) {
    }
}