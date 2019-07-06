package mekanism.generators.client.gui;

import java.io.IOException;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.render.GLSMHelper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReactorLogicAdapter extends GuiMekanismTile<TileEntityReactorLogicAdapter> {

    public GuiReactorLogicAdapter(InventoryPlayer inventory, final TileEntityReactorLogicAdapter tile) {
        super(tile, new ContainerNull(inventory.player, tile));
    }

    private boolean overCooling(int xAxis, int yAxis) {
        return xAxis >= 23 && xAxis <= 34 && yAxis >= 19 && yAxis <= 30;
    }

    private boolean overType(int xAxis, int yAxis, ReactorLogic type) {
        return xAxis >= 24 && xAxis <= 152 && yAxis >= 32 + (22 * type.ordinal()) && yAxis <= 32 + 22 + (22 * type.ordinal());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        renderScaledText(LangUtils.localize("gui.coolingMeasurements") + ": " + EnumColor.RED + LangUtils.transOnOff(tileEntity.activeCooled), 36, 20, 0x404040, 117);
        renderScaledText(LangUtils.localize("gui.redstoneOutputMode") + ": " + EnumColor.RED + tileEntity.logicType.getLocalizedName(), 23, 123, 0x404040, 130);
        String text = LangUtils.localize("gui.status") + ": " + EnumColor.RED + LangUtils.localize("gui." + (tileEntity.checkMode() ? "outputting" : "idle"));
        fontRenderer.drawString(text, (xSize / 2) - (fontRenderer.getStringWidth(text) / 2), 136, 0x404040);
        for (ReactorLogic type : ReactorLogic.values()) {
            renderItem(type.getRenderStack(), 27, 35 + (22 * type.ordinal()));
            fontRenderer.drawString(EnumColor.WHITE + type.getLocalizedName(), 46, 34 + (22 * type.ordinal()), 0x404040);
        }
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (ReactorLogic type : ReactorLogic.values()) {
            if (overType(xAxis, yAxis, type)) {
                displayTooltips(MekanismUtils.splitTooltip(type.getDescription(), ItemStack.EMPTY), xAxis, yAxis);
            }
        }
        if (overCooling(xAxis, yAxis)) {
            drawHoveringText(LangUtils.localize("gui.toggleCooling"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        for (ReactorLogic type : ReactorLogic.values()) {
            GLSMHelper.INSTANCE.color(EnumColor.RED);
            drawTexturedModalRect(guiLeft + 24, guiTop + 32 + (22 * type.ordinal()), 0, 166 + (type == tileEntity.logicType ? 22 : 0), 128, 22);
            GLSMHelper.INSTANCE.resetColor();
        }
        drawTexturedModalRect(guiLeft + 23, guiTop + 19, 176, overCooling(xAxis, yAxis), 11);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (overCooling(xAxis, yAxis)) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                TileNetworkList data = TileNetworkList.withContents(0);
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
                return;
            }
            for (ReactorLogic type : ReactorLogic.values()) {
                if (overType(xAxis, yAxis, type) && type != tileEntity.logicType) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                    TileNetworkList data = TileNetworkList.withContents(1, type.ordinal());
                    Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
                    return;
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiReactorLogicAdapter.png");
    }
}