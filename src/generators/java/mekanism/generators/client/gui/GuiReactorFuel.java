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
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiEnergyInfo(() -> tileEntity.isFormed() ? Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.storing"), ": ", EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy())),
              TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ",
                    EnergyDisplay.of(tileEntity.getReactor().getPassiveGeneration(false, true)), "/t")) : Collections.emptyList(), this, resource));
        addButton(new GuiGasGauge(() -> tileEntity.deuteriumTank, Type.SMALL, this, resource, 25, 64));
        addButton(new GuiGasGauge(() -> tileEntity.fuelTank, Type.STANDARD, this, resource, 79, 50));
        addButton(new GuiGasGauge(() -> tileEntity.tritiumTank, Type.SMALL, this, resource, 133, 64));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getActive() ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 45, 75));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getActive() ? 1 : 0;
            }
        }, ProgressBar.SMALL_LEFT, this, resource, 99, 75));
        addButton(new GuiReactorTab(this, tileEntity, ReactorTab.HEAT, resource));
        addButton(new GuiReactorTab(this, tileEntity, ReactorTab.STAT, resource));

        String prevRad = injectionRateField != null ? injectionRateField.getText() : "";
        addButton(injectionRateField = new TextFieldWidget(font, guiLeft + 98, guiTop + 115, 26, 11, ""));
        injectionRateField.setMaxStringLength(2);
        injectionRateField.setText(prevRad);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawString(tileEntity.getName(), 46, 6, 0x404040);
        //TODO: Lang key for None
        drawCenteredText(TextComponentUtil.build(Translation.of("mekanism.gui.reactor.injectionRate"),
              ": " + (tileEntity.getReactor() == null ? "None" : tileEntity.getReactor().getInjectionRate())), 0, xSize, 35, 0x404040);
        drawString("Edit Rate" + ":", 50, 117, 0x404040);
    }

    @Override
    public void tick() {
        super.tick();
        injectionRateField.tick();
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
}