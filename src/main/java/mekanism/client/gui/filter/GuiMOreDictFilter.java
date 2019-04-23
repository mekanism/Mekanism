package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiMOreDictFilter extends GuiOreDictFilter<MOreDictFilter, TileEntityDigitalMiner> {

    public GuiMOreDictFilter(EntityPlayer player, TileEntityDigitalMiner tile, int index) {
        super(player, tile);
        origFilter = (MOreDictFilter) tileEntity.filters.get(index);
        filter = ((MOreDictFilter) tileEntity.filters.get(index)).clone();
        updateStackList(filter.getOreDictName());
    }

    public GuiMOreDictFilter(EntityPlayer player, TileEntityDigitalMiner tile) {
        super(player, tile);
        isNew = true;
        filter = new MOreDictFilter();
    }

    @Override
    protected void addButtons(int guiWidth, int guiHeight) {
        buttonList.add(new GuiButton(0, guiWidth + 27, guiHeight + 62, 60, 20, LangUtils.localize("gui.save")));
        buttonList.add(new GuiButton(1, guiWidth + 89, guiHeight + 62, 60, 20, LangUtils.localize("gui.delete")));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler
              .sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(
              (isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " + LangUtils
                    .localize("gui.oredictFilter"), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
        renderScaledText(LangUtils.localize("gui.key") + ": " + filter.getOreDictName(), 35, 32, 0x00CD00, 107);
        if (!renderStack.isEmpty()) {
            try {
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(renderStack, 12, 19);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            } catch (Exception ignored) {
            }
        }
        if (!filter.replaceStack.isEmpty()) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            itemRender.renderItemAndEffectIntoGUI(filter.replaceStack, 149, 19);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils
                  .transYesNo(filter.requireStack), xAxis, yAxis);
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
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
            drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 0, 11, 11);
        } else {
            drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 11, 11, 11);
        }
        if (xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59) {
            drawTexturedModalRect(guiWidth + 131, guiHeight + 47, 176 + 11, 0, 12, 12);
        } else {
            drawTexturedModalRect(guiWidth + 131, guiHeight + 47, 176 + 11, 12, 12, 12);
        }
        if (xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59) {
            drawTexturedModalRect(guiWidth + 148, guiHeight + 45, 176 + 23, 0, 14, 14);
        } else {
            drawTexturedModalRect(guiWidth + 148, guiHeight + 45, 176 + 23, 14, 14, 14);
        }
        oreDictText.drawTextBox();
        if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            int x = guiWidth + 149;
            int y = guiHeight + 19;
            drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        oreDictText.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = (mouseX - (width - xSize) / 2);
            int yAxis = (mouseY - (height - ySize) / 2);
            if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(
                      new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), isNew ? 5 : 0, 0, 0));
            }
            if (xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                setOreDictKey();
            }
            if (xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                filter.requireStack = !filter.requireStack;
            }
            if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
                boolean doNull = false;
                ItemStack stack = mc.player.inventory.getItemStack();
                ItemStack toUse = ItemStack.EMPTY;
                if (!stack.isEmpty() && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    if (stack.getItem() instanceof ItemBlock) {
                        if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                            toUse = stack.copy();
                            toUse.setCount(1);
                        }
                    }
                } else if (stack.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    doNull = true;
                }
                if (!toUse.isEmpty() || doNull) {
                    filter.replaceStack = toUse;
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiMOreDictFilter.png");
    }

    @Override
    protected void updateStackList(String oreName) {
        iterStacks = OreDictCache.getOreDictStacks(oreName, true);
        stackSwitch = 0;
        stackIndex = -1;
    }
}