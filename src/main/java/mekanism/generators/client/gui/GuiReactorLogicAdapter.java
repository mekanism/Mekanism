package mekanism.generators.client.gui;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorLogicAdapter extends GuiMekanismTile<TileEntityReactorLogicAdapter> {

    public GuiReactorLogicAdapter(InventoryPlayer inventory, final TileEntityReactorLogicAdapter tile) {
        super(tile, new ContainerNull(inventory.player, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    6, 0x404040);
        renderScaledText(LangUtils.localize("gui.coolingMeasurements") + ": " + EnumColor.RED + LangUtils
              .transOnOff(tileEntity.activeCooled), 36, 20, 0x404040, 117);
        renderScaledText(LangUtils.localize("gui.redstoneOutputMode") + ": " + EnumColor.RED + tileEntity.logicType
              .getLocalizedName(), 23, 123, 0x404040, 130);
        String text = LangUtils.localize("gui.status") + ": " + EnumColor.RED + LangUtils
              .localize("gui." + (tileEntity.checkMode() ? "outputting" : "idle"));
        fontRenderer.drawString(text, (xSize / 2) - (fontRenderer.getStringWidth(text) / 2), 136, 0x404040);
        for (ReactorLogic type : ReactorLogic.values()) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            itemRender.renderItemAndEffectIntoGUI(type.getRenderStack(), 27, 35 + (22 * type.ordinal()));
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            fontRenderer
                  .drawString(EnumColor.WHITE + type.getLocalizedName(), 46, 34 + (22 * type.ordinal()), 0x404040);
        }
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        for (ReactorLogic type : ReactorLogic.values()) {
            if (xAxis >= 24 && xAxis <= 152 && yAxis >= 32 + (22 * type.ordinal()) && yAxis <= 32 + 22 + (22 * type
                  .ordinal())) {
                displayTooltips(MekanismUtils.splitTooltip(type.getDescription(), ItemStack.EMPTY), xAxis, yAxis);
            }
        }
        if (xAxis >= 23 && xAxis <= 34 && yAxis >= 19 && yAxis <= 30) {
            drawHoveringText(LangUtils.localize("gui.toggleCooling"), xAxis, yAxis);
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
        for (ReactorLogic type : ReactorLogic.values()) {
            MekanismRenderer.color(EnumColor.RED);
            drawTexturedModalRect(guiWidth + 24, guiHeight + 32 + (22 * type.ordinal()), 0,
                  166 + (type == tileEntity.logicType ? 22 : 0), 128, 22);
            MekanismRenderer.resetColor();
        }
        if (xAxis >= 23 && xAxis <= 34 && yAxis >= 19 && yAxis <= 30) {
            drawTexturedModalRect(guiWidth + 23, guiHeight + 19, 176, 0, 11, 11);
        } else {
            drawTexturedModalRect(guiWidth + 23, guiHeight + 19, 176, 11, 11, 11);
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = (mouseX - (width - xSize) / 2);
            int yAxis = (mouseY - (height - ySize) / 2);
            if (xAxis >= 23 && xAxis <= 34 && yAxis >= 19 && yAxis <= 30) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                TileNetworkList data = TileNetworkList.withContents(0);
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                return;
            }
            for (ReactorLogic type : ReactorLogic.values()) {
                if (xAxis >= 24 && xAxis <= 152 && yAxis >= 32 + (22 * type.ordinal()) && yAxis <= 32 + 22 + (22 * type
                      .ordinal())) {
                    if (type != tileEntity.logicType) {
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        TileNetworkList data = TileNetworkList.withContents(1, type.ordinal());
                        Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiReactorLogicAdapter.png");
    }
}