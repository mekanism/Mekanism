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
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiGasTank extends GuiMekanismTile<TileEntityGasTank, MekanismTileContainer<TileEntityGasTank>> {

    public GuiGasTank(MekanismTileContainer<TileEntityGasTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiSlot(SlotType.OUTPUT, this, 7, 7).with(SlotOverlay.PLUS));
        addButton(new GuiSlot(SlotType.INPUT, this, 7, 39).with(SlotOverlay.MINUS));
        addButton(new GuiGasMode(this, 159, 72, true, () -> tile.dumping,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0)))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        ITextComponent component;
        if (tile.gasTank.getStored() == Integer.MAX_VALUE) {
            component = MekanismLang.INFINITE.translate();
        } else {
            component = MekanismLang.GENERIC_FRACTION.translate(tile.gasTank.getStored(),
                  tile.tier.getStorage() == Integer.MAX_VALUE ? MekanismLang.INFINITE : tile.tier.getStorage());
        }
        drawString(component, 45, 40, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        GasStack gasStack = tile.gasTank.getStack();
        if (!gasStack.isEmpty()) {
            renderScaledText(MekanismLang.GAS.translate(gasStack), 45, 49, 0x404040, 112);
        } else {
            renderScaledText(MekanismLang.GAS.translate(MekanismLang.NONE), 45, 49, 0x404040, 112);
        }
        drawString(MekanismLang.INVENTORY.translate(), 8, getYSize() - 96 + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (!tile.gasTank.isEmpty()) {
            //TODO: 1.14 Convert to GuiElement, and make it draw the gas texture instead of the bar (will make it easier at a glance to see what is going on)
            // If we make GuiBar be able to stretch then we can use that as the bar background and do something similar to the InfuseBar
            // The other option which may make more sense is to make it be a GuiGauge
            int scale = (int) (((double) tile.gasTank.getStored() / tile.tier.getStorage()) * 72);
            TextureAtlasSprite icon = MekanismRenderer.getChemicalTexture(tile.gasTank.getType());
            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            MekanismRenderer.color(tile.gasTank.getStack());
            int start = 0;
            int x = getGuiLeft() + 65;
            int y = getGuiTop() + 17;
            while (scale > 0) {
                int renderRemaining;
                if (scale > 16) {
                    renderRemaining = 16;
                    scale -= 16;
                } else {
                    renderRemaining = scale;
                    scale = 0;
                }
                drawTexturedRectFromIcon(x + start, y, icon, renderRemaining, 10);
                start += 16;
            }
            MekanismRenderer.resetColor();
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "gas_tank.png");
    }
}