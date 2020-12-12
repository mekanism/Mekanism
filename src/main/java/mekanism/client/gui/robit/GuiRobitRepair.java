package mekanism.client.gui.robit;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiRobitRepair extends GuiRobit<RepairRobitContainer> implements IContainerListener {

    //Use the vanilla anvil's gui texture
    private static final ResourceLocation ANVIL_RESOURCE = new ResourceLocation("textures/gui/container/anvil.png");
    private TextFieldWidget itemNameField;

    public GuiRobitRepair(RepairRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        playerInventoryTitleY += 1;
        titleX = 60;
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);
        addButton(itemNameField = new TextFieldWidget(font, guiLeft + 62, guiTop + 24, 103, 12, StringTextComponent.EMPTY));
        itemNameField.setCanLoseFocus(false);
        itemNameField.changeFocus(true);
        itemNameField.setTextColor(-1);
        itemNameField.setDisabledTextColour(-1);
        itemNameField.setEnableBackgroundDrawing(false);
        itemNameField.setMaxStringLength(35);
        itemNameField.setResponder(this::onTextUpdate);
        container.removeListener(this);
        container.addListener(this);
    }

    @Override
    public void init(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        //Handle initial initialization when it will be null, as well as later initializations
        // such as going back to the screen from JEI
        String previousName = itemNameField == null ? "" : itemNameField.getText();
        super.init(minecraft, scaledWidth, scaledHeight);
        itemNameField.setText(previousName);
    }

    private void onTextUpdate(String newText) {
        if (!newText.isEmpty()) {
            Slot slot = container.getSlot(0);
            if (slot.getHasStack() && !slot.getStack().hasDisplayName() && newText.equals(slot.getStack().getDisplayName().getString())) {
                newText = "";
            }
            container.updateItemName(newText);
            getMinecraft().player.connection.sendPacket(new CRenameItemPacket(newText));
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardListener.enableRepeatEvents(false);
        container.removeListener(this);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT_REPAIR.translate(), titleX, titleY, titleTextColor());
        int maximumCost = container.getMaximumCost();
        if (maximumCost > 0) {
            int k = 0x80FF20;
            boolean flag = true;
            ITextComponent component = MekanismLang.REPAIR_COST.translate(maximumCost);
            if (maximumCost >= 40 && !getMinecraft().player.isCreative()) {
                component = MekanismLang.REPAIR_EXPENSIVE.translate();
                k = 0xFF6060;
            } else {
                Slot slot = container.getSlot(2);
                if (!slot.getHasStack()) {
                    flag = false;
                } else if (!slot.canTakeStack(playerInventory.player)) {
                    k = 0xFF6060;
                }
            }

            if (flag) {
                int width = xSize - 8 - getStringWidth(component) - 2;
                fill(matrix, width - 2, 67, xSize - 8, 79, 0x4F000000);
                getFont().func_243246_a(matrix, component, width, 69.0F, k);
                MekanismRenderer.resetColor();
            }
        }
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (itemNameField.canWrite()) {
            return itemNameField.charTyped(c, keyCode);
        }
        return super.charTyped(c, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE && itemNameField.canWrite()) {
            return itemNameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.REPAIR;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(@Nonnull MatrixStack matrix, float partialTick, int mouseX, int mouseY) {
        getMinecraft().textureManager.bindTexture(ANVIL_RESOURCE);
        blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize);
        blit(matrix, guiLeft + 59, guiTop + 20, 0, ySize + (container.getSlot(0).getHasStack() ? 0 : 16), 110, 16);
        if ((container.getSlot(0).getHasStack() || container.getSlot(1).getHasStack()) && !container.getSlot(2).getHasStack()) {
            blit(matrix, guiLeft + 99, guiTop + 45, xSize, 0, 28, 21);
        }
    }

    @Override
    public void sendAllContents(@Nonnull Container container, @Nonnull NonNullList<ItemStack> list) {
        sendSlotContents(container, 0, container.getSlot(0).getStack());
    }

    @Override
    public void sendSlotContents(@Nonnull Container container, int slotID, @Nonnull ItemStack stack) {
        if (slotID == 0) {
            itemNameField.setText(stack.isEmpty() ? "" : stack.getDisplayName().getString());
            itemNameField.setEnabled(!stack.isEmpty());
        }
    }

    @Override
    public void sendWindowProperty(@Nonnull Container container, int varToUpdate, int newValue) {
    }
}