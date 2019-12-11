package mekanism.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiGasMode;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.GasTankContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiGasTank extends GuiMekanismTile<TileEntityGasTank, GasTankContainer> {

    public GuiGasTank(GasTankContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiSideConfigurationTab(this, tile, resource));
        addButton(new GuiTransporterConfigTab(this, tile, resource));
        addButton(new GuiSlot(SlotType.OUTPUT, this, resource, 7, 7).with(SlotOverlay.PLUS));
        addButton(new GuiSlot(SlotType.INPUT, this, resource, 7, 39).with(SlotOverlay.MINUS));
        addButton(new GuiGasMode(this, resource, 159, 72, true, () -> tile.dumping,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0)))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        ITextComponent component;
        if (tile.gasTank.getStored() == Integer.MAX_VALUE) {
            component = TextComponentUtil.translate("gui.mekanism.infinite");
        } else if (tile.tier.getStorage() == Integer.MAX_VALUE) {
            component = TextComponentUtil.build(tile.gasTank.getStored(), "/", Translation.of("gui.mekanism.infinite"));
        } else {
            component = TextComponentUtil.getString(tile.gasTank.getStored() + "/" + tile.tier.getStorage());
        }
        drawString(component, 45, 40, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        GasStack gasStack = tile.gasTank.getStack();
        if (!gasStack.isEmpty()) {
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.gas"), ": ", gasStack), 45, 49, 0x404040, 112);
        } else {
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.gas"), ": ", Translation.of("gui.mekanism.none")), 45, 49, 0x404040, 112);
        }
        drawString(TextComponentUtil.translate("container.inventory"), 8, ySize - 96 + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (!tile.gasTank.isEmpty()) {
            //TODO: 1.14 Convert to GuiElement, and make it draw the gas texture instead of the bar (will make it easier at a glance to see what is going on)
            // If we make GuiBar be able to stretch then we can use that as the bar background and do something similar to the InfuseBar
            // The other option which may make more sense is to make it be a GuiGauge
            //TODO: Figure out why it is going from right to left
            int scale = (int) (((double) tile.gasTank.getStored() / tile.tier.getStorage()) * 72);
            TextureAtlasSprite icon = MekanismRenderer.getChemicalTexture(tile.gasTank.getType());
            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            MekanismRenderer.color(tile.gasTank.getStack());
            drawTexturedRectFromIcon(guiLeft + 65, guiTop + 17, icon, scale, 10);
            int start = 0;
            int x = guiLeft + 65;
            int y = guiTop + 17;
            while (scale > 0) {
                int renderRemaining;
                if (scale > 16) {
                    renderRemaining = 16;
                    scale -= 16;
                } else {
                    renderRemaining = scale;
                    scale = 0;
                }
                drawTexturedRectFromIcon(x + 72 - renderRemaining - start, y, icon, renderRemaining, 10);
                start += 16;
                if (scale == 0) {
                    break;
                }
            }
            MekanismRenderer.resetColor();
            //Reset the texture location, even though it technically isn't needed
            minecraft.textureManager.bindTexture(getGuiLocation());
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "gas_tank.png");
    }
}