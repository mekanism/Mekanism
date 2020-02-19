package mekanism.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.button.GuiGasMode;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiHorizontalChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiGasTank extends GuiMekanismTile<TileEntityGasTank, MekanismTileContainer<TileEntityGasTank>> {

    public GuiGasTank(MekanismTileContainer<TileEntityGasTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiHorizontalChemicalBar<>(this, GuiVerticalChemicalBar.getProvider(tile.gasTank), 42, 16, 116, 10));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiGasMode(this, getGuiLeft() + 159, getGuiTop() + 72, true, () -> tile.dumping,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0)))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        //TODO: Convert to GuiElement or maybe "modernize" the look to make it look more "technical" and use GuiInnerScreen
        // and change the color of this text to green
        ITextComponent component;
        if (tile.gasTank.getStored() == Integer.MAX_VALUE) {
            component = MekanismLang.INFINITE.translate();
        } else {
            component = MekanismLang.GENERIC_FRACTION.translate(tile.gasTank.getStored(),
                  tile.tier.getStorage() == Integer.MAX_VALUE ? MekanismLang.INFINITE : tile.tier.getStorage());
        }
        drawString(component, 45, 40, 0x404040);
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
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "gas_tank.png");
    }
}