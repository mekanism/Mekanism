package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.inventory.container.reactor.info.ReactorFuelContainer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiReactorFuel extends GuiReactorInfo<ReactorFuelContainer> {

    private TextFieldWidget injectionRateField;

    public GuiReactorFuel(ReactorFuelContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.storing"), ": ", EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())),
              TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ",
                    EnergyDisplay.of(tileEntity.getReactor().getPassiveGeneration(false, true)), "/t")) : Collections.emptyList(), this, resource));
        addGuiElement(new GuiGasGauge(() -> tileEntity.deuteriumTank, Type.SMALL, this, resource, 25, 64));
        addGuiElement(new GuiGasGauge(() -> tileEntity.fuelTank, Type.STANDARD, this, resource, 79, 50));
        addGuiElement(new GuiGasGauge(() -> tileEntity.tritiumTank, Type.SMALL, this, resource, 133, 64));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getActive() ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 45, 75));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getActive() ? 1 : 0;
            }
        }, ProgressBar.SMALL_LEFT, this, resource, 99, 75));
        addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.HEAT, resource));
        addGuiElement(new GuiReactorTab(this, tileEntity, ReactorTab.STAT, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawString(tileEntity.getName(), 46, 6, 0x404040);
        //TODO: Lang key for None
        drawCenteredText(TextComponentUtil.build(Translation.of("gui.reactor.injectionRate"),
              ": " + (tileEntity.getReactor() == null ? "None" : tileEntity.getReactor().getInjectionRate())), 0, xSize, 35, 0x404040);
        drawString("Edit Rate" + ":", 50, 117, 0x404040);
    }

    //TODO: Draw TextBox
    /*@Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        injectionRateField.drawTextBox();
        MekanismRenderer.resetColor();
    }*/

    @Override
    public void tick() {
        super.tick();
        injectionRateField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        injectionRateField.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (!injectionRateField.isFocused() || i == GLFW.GLFW_KEY_ESCAPE) {
            return super.charTyped(c, i);
        }
        if (i == GLFW.GLFW_KEY_ENTER && injectionRateField.isFocused()) {
            setInjection();
            return true;
        }
        if (Character.isDigit(c) || isTextboxKey(c, i)) {
            return injectionRateField.charTyped(c, i);
        }
        return false;
    }

    private void setInjection() {
        if (!injectionRateField.getText().isEmpty()) {
            int toUse = Math.max(0, Integer.parseInt(injectionRateField.getText()));
            toUse -= toUse % 2;
            TileNetworkList data = TileNetworkList.withContents(0, toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            injectionRateField.setText("");
        }
    }

    @Override
    public void init() {
        super.init();
        String prevRad = injectionRateField != null ? injectionRateField.getText() : "";
        injectionRateField = new TextFieldWidget(font, guiLeft + 98, guiTop + 115, 26, 11, "");
        injectionRateField.setMaxStringLength(2);
        injectionRateField.setText(prevRad);
    }
}