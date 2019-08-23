package mekanism.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.gauge.GuiNumberGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.LaserAmplifierContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, LaserAmplifierContainer> {

    private TextFieldWidget minField;
    private TextFieldWidget maxField;
    private TextFieldWidget timerField;

    public GuiLaserAmplifier(LaserAmplifierContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.energyIcon;
            }

            @Override
            public double getLevel() {
                return tileEntity.collectedEnergy;
            }

            @Override
            public double getMaxLevel() {
                return TileEntityLaserAmplifier.MAX_ENERGY;
            }

            @Override
            public ITextComponent getText(double level) {
                return TextComponentUtil.build(Translation.of("mekanism.gui.storing"), ": ", EnergyDisplay.of(level, tileEntity.getMaxEnergy()));
            }
        }, Type.STANDARD, this, resource, 6, 10));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiAmplifierTab(this, tileEntity, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 55, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        if (tileEntity.time > 0) {
            drawString(TextComponentUtil.build(Translation.of("gui.delay"), ": " + tileEntity.time + "t"), 26, 30, 0x404040);
        } else {
            drawString(TextComponentUtil.translate("gui.noDelay"), 26, 30, 0x404040);
        }
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.min"), ": ", EnergyDisplay.of(tileEntity.minThreshold)), 26, 45, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.max"), ": ", EnergyDisplay.of(tileEntity.maxThreshold)), 26, 60, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        //TODO: Figure out what the parameters do
        minField.renderButton(0, 0, 0);
        maxField.renderButton(0, 0, 0);
        timerField.renderButton(0, 0, 0);
        MekanismRenderer.resetColor();
    }

    @Override
    public void tick() {
        super.tick();
        minField.tick();
        maxField.tick();
        timerField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        minField.mouseClicked(mouseX, mouseY, button);
        maxField.mouseClicked(mouseX, mouseY, button);
        timerField.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (!(minField.isFocused() || maxField.isFocused() || timerField.isFocused()) || i == GLFW.GLFW_KEY_ESCAPE) {
            return super.charTyped(c, i);
        }

        if (i == GLFW.GLFW_KEY_ENTER) {
            if (minField.isFocused()) {
                setMinThreshold();
                return true;
            } else if (maxField.isFocused()) {
                setMaxThreshold();
                return true;
            } else if (timerField.isFocused()) {
                setTime();
                return true;
            }
        }

        if (Character.isDigit(c) || c == '.' || c == 'E' || isTextboxKey(c, i)) {
            if (minField.charTyped(c, i)) {
                return true;
            }
            if (maxField.charTyped(c, i)) {
                return true;
            }
            if (c != '.' && c != 'E') {
                return timerField.charTyped(c, i);
            }
        }
        return false;
    }

    private void setMinThreshold() {
        if (!minField.getText().isEmpty()) {
            double toUse;
            try {
                toUse = Math.max(0, Double.parseDouble(minField.getText()));
            } catch (Exception e) {
                minField.setText("");
                return;
            }
            TileNetworkList data = TileNetworkList.withContents(0, toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            minField.setText("");
        }
    }

    private void setMaxThreshold() {
        if (!maxField.getText().isEmpty()) {
            double toUse;
            try {
                toUse = Math.max(0, Double.parseDouble(maxField.getText()));
            } catch (Exception e) {
                maxField.setText("");
                return;
            }
            TileNetworkList data = TileNetworkList.withContents(1, toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            maxField.setText("");
        }
    }

    private void setTime() {
        if (!timerField.getText().isEmpty()) {
            int toUse = Math.max(0, Integer.parseInt(timerField.getText()));
            TileNetworkList data = TileNetworkList.withContents(2, toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            timerField.setText("");
        }
    }

    @Override
    public void init() {
        super.init();
        String prevTime = timerField != null ? timerField.getText() : "";
        timerField = new TextFieldWidget(font, guiLeft + 96, guiTop + 28, 36, 11, prevTime);
        timerField.setMaxStringLength(4);

        String prevMin = minField != null ? minField.getText() : "";
        minField = new TextFieldWidget(font, guiLeft + 96, guiTop + 43, 72, 11, prevMin);
        minField.setMaxStringLength(10);

        String prevMax = maxField != null ? maxField.getText() : "";
        maxField = new TextFieldWidget(font, guiLeft + 96, guiTop + 58, 72, 11, prevMax);
        maxField.setMaxStringLength(10);
    }
}