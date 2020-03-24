package mekanism.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.api.chemical.gas.GasStack;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiGasTank extends GuiMekanismTile<TileEntityGasTank, MekanismTileContainer<TileEntityGasTank>> {

    public GuiGasTank(MekanismTileContainer<TileEntityGasTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(tile.gasTank), 42, 16, 116, 10, true));
        addButton(new GuiInnerScreen(this, 42, 37, 118, 28));
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
        drawString(MekanismLang.INVENTORY.translate(), 8, getYSize() - 96 + 2, 0x404040);
        ITextComponent component;
        GasStack gasStack = tile.gasTank.getStack();
        if (gasStack.isEmpty()) {
            component = MekanismLang.GAS.translate(MekanismLang.NONE);
        } else {
            component = MekanismLang.GAS.translate(gasStack);
        }
        renderScaledText(component, 45, 40, 0x00CD00, 112);
        if (!tile.gasTank.isEmpty() && tile.tier == GasTankTier.CREATIVE) {
            component = MekanismLang.INFINITE.translate();
        } else {
            component = MekanismLang.GENERIC_FRACTION.translate(tile.gasTank.getStored(), tile.tier == GasTankTier.CREATIVE ? MekanismLang.INFINITE : tile.tier.getStorage());
        }
        drawString(component, 45, 49, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}