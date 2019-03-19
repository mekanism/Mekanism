package mekanism.client.gui;

import java.io.IOException;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.sound.SoundHandler;
import mekanism.common.inventory.container.ContainerDictionary;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiDictionary extends GuiMekanism {

    public ItemStack itemType = ItemStack.EMPTY;

    public GuiScrollList scrollList;

    public GuiDictionary(InventoryPlayer inventory) {
        super(new ContainerDictionary(inventory));
        addGuiElement(scrollList = new GuiScrollList(this, getGuiLocation(), 8, 30, 160, 4));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("item.Dictionary.name"), 64, 5, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, ySize - 96 + 2, 0x404040);
        if (!itemType.isEmpty()) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            itemRender.renderItemAndEffectIntoGUI(itemType, 6, 6);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int xAxis = mouseX - guiWidth;
        int yAxis = mouseY - guiHeight;
        if (xAxis >= 6 && xAxis <= 22 && yAxis >= 6 && yAxis <= 22) {
            GlStateManager.pushMatrix();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            int x = guiWidth + 6;
            int y = guiHeight + 6;
            drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GlStateManager.popMatrix();
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);

        if (button == 0) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                Slot hovering = null;

                for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
                    Slot slot = inventorySlots.inventorySlots.get(i);

                    if (isMouseOverSlot(slot, mouseX, mouseY)) {
                        hovering = slot;
                        break;
                    }
                }

                if (hovering != null) {
                    ItemStack stack = hovering.getStack();

                    if (!stack.isEmpty()) {
                        itemType = stack.copy();
                        itemType.setCount(1);

                        scrollList.setText(MekanismUtils.getOreDictName(itemType));
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                        return;
                    }
                }
            }

            if (xAxis >= 6 && xAxis <= 22 && yAxis >= 6 && yAxis <= 22) {
                ItemStack stack = mc.player.inventory.getItemStack();

                if (!stack.isEmpty() && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    itemType = stack.copy();
                    itemType.setCount(1);

                    scrollList.setText(MekanismUtils.getOreDictName(itemType));
                } else if (stack.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    itemType = ItemStack.EMPTY;

                    scrollList.setText(null);
                }

                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDictionary.png");
    }
}