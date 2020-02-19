package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.Chemical;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiVerticalProgress;
import mekanism.client.gui.element.bar.GuiHorizontalChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar.ChemicalInfoProvider;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiSortingTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiFactory extends GuiMekanismTile<TileEntityFactory<?>, MekanismTileContainer<TileEntityFactory<?>>> {

    public GuiFactory(MekanismTileContainer<TileEntityFactory<?>> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tile.hasSecondaryResourceBar()) {
            ySize += 11;
        } else if (tile instanceof TileEntitySawingFactory) {
            ySize += 21;
        }
        if (tile.tier == FactoryTier.ULTIMATE) {
            xSize += 34;
        }
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiSortingTab(this, tile));
        //TODO: Make the power bar taller for the sawing factories
        addButton(new GuiVerticalPowerBar(this, tile, getXSize() - 12, 16));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.lastUsage)),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy()))), this));
        if (tile.hasSecondaryResourceBar()) {
            ChemicalInfoProvider<? extends Chemical<?>> provider = null;
            if (tile instanceof TileEntityMetallurgicInfuserFactory) {
                provider = GuiVerticalChemicalBar.getProvider(((TileEntityMetallurgicInfuserFactory) tile).getInfusionTank());
            } else if (tile instanceof TileEntityItemStackGasToItemStackFactory) {
                provider = GuiVerticalChemicalBar.getProvider(((TileEntityItemStackGasToItemStackFactory) tile).getGasTank());
            }
            if (provider != null) {
                int barX = tile.tier == FactoryTier.ULTIMATE ? 25 : 7;
                addButton(new GuiHorizontalChemicalBar<>(this, provider, barX, 76, 138, 4));
                //TODO: Move left and make wider for ultimate factory??
                addButton(new GuiDumpButton<>(this, tile, barX + 141, 76,
                      () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1)))));
            }
        }

        int baseX = tile.tier == FactoryTier.BASIC ? 55 : tile.tier == FactoryTier.ADVANCED ? 35 : tile.tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tile.tier == FactoryTier.BASIC ? 38 : tile.tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tile.tier.processes; i++) {
            int cacheIndex = i;
            addButton(new GuiVerticalProgress(this, new IProgressInfoHandler() {
                @Override
                public double getProgress() {
                    return tile.getScaledProgress(1, cacheIndex);
                }
            }, 4 + baseX + (i * baseXMult), 33));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), tile.tier == FactoryTier.ULTIMATE ? 26 : 8,
              tile.hasSecondaryResourceBar() ? 85 : tile instanceof TileEntitySawingFactory ? 95 : 75, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tile.hasSecondaryResourceBar()) {
            if (button == 0 || InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                double xAxis = mouseX - getGuiLeft();
                double yAxis = mouseY - getGuiTop();
                //TODO: Hovering over the secondary bar??
                if (xAxis > 8 && xAxis < 168 && yAxis > 78 && yAxis < 83) {
                    ItemStack stack = minecraft.player.inventory.getItemStack();
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1)));
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}