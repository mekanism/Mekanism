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
        inventoryLabelY += 1;
        titleLabelX = 60;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
        itemNameField = addButton(new TextFieldWidget(font, leftPos + 62, topPos + 24, 103, 12, StringTextComponent.EMPTY));
        itemNameField.setCanLoseFocus(false);
        itemNameField.changeFocus(true);
        itemNameField.setTextColor(-1);
        itemNameField.setTextColorUneditable(-1);
        itemNameField.setBordered(false);
        itemNameField.setMaxLength(35);
        itemNameField.setResponder(this::onTextUpdate);
        menu.removeSlotListener(this);
        menu.addSlotListener(this);
    }

    @Override
    public void init(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        //Handle initial initialization when it will be null, as well as later initializations
        // such as going back to the screen from JEI
        String previousName = itemNameField == null ? "" : itemNameField.getValue();
        super.init(minecraft, scaledWidth, scaledHeight);
        itemNameField.setValue(previousName);
    }

    private void onTextUpdate(String newText) {
        if (!newText.isEmpty()) {
            Slot slot = menu.getSlot(0);
            if (slot.hasItem() && !slot.getItem().hasCustomHoverName() && newText.equals(slot.getItem().getHoverName().getString())) {
                newText = "";
            }
            menu.setItemName(newText);
            getMinecraft().player.connection.send(new CRenameItemPacket(newText));
        }
    }

    @Override
    public void removed() {
        super.removed();
        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
        menu.removeSlotListener(this);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT_REPAIR.translate(), titleLabelX, titleLabelY, titleTextColor());
        int maximumCost = menu.getCost();
        if (maximumCost > 0) {
            int k = 0x80FF20;
            boolean flag = true;
            ITextComponent component = MekanismLang.REPAIR_COST.translate(maximumCost);
            if (maximumCost >= 40 && !getMinecraft().player.isCreative()) {
                component = MekanismLang.REPAIR_EXPENSIVE.translate();
                k = 0xFF6060;
            } else {
                Slot slot = menu.getSlot(2);
                if (!slot.hasItem()) {
                    flag = false;
                } else if (!slot.mayPickup(inventory.player)) {
                    k = 0xFF6060;
                }
            }

            if (flag) {
                int width = imageWidth - 8 - getStringWidth(component) - 2;
                fill(matrix, width - 2, 67, imageWidth - 8, 79, 0x4F000000);
                getFont().drawShadow(matrix, component, width, 69.0F, k);
                MekanismRenderer.resetColor();
            }
        }
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (itemNameField.canConsumeInput()) {
            return itemNameField.charTyped(c, keyCode);
        }
        return super.charTyped(c, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE && itemNameField.canConsumeInput()) {
            return itemNameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.REPAIR;
    }

    @Override
    protected void renderBg(@Nonnull MatrixStack matrix, float partialTick, int mouseX, int mouseY) {
        getMinecraft().textureManager.bind(ANVIL_RESOURCE);
        blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        blit(matrix, leftPos + 59, topPos + 20, 0, imageHeight + (menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
        if ((menu.getSlot(0).hasItem() || menu.getSlot(1).hasItem()) && !menu.getSlot(2).hasItem()) {
            blit(matrix, leftPos + 99, topPos + 45, imageWidth, 0, 28, 21);
        }
    }

    @Override
    public void refreshContainer(@Nonnull Container container, @Nonnull NonNullList<ItemStack> list) {
        slotChanged(container, 0, container.getSlot(0).getItem());
    }

    @Override
    public void slotChanged(@Nonnull Container container, int slotID, @Nonnull ItemStack stack) {
        if (slotID == 0) {
            itemNameField.setValue(stack.isEmpty() ? "" : stack.getHoverName().getString());
            itemNameField.setEditable(!stack.isEmpty());
        }
    }

    @Override
    public void setContainerData(@Nonnull Container container, int varToUpdate, int newValue) {
    }
}