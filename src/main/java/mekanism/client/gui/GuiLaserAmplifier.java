package mekanism.client.gui;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.client.gui.element.GuiAmplifierTab;
import mekanism.client.gui.element.GuiGauge.Type;
import mekanism.client.gui.element.GuiNumberGauge;
import mekanism.client.gui.element.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.inventory.container.ContainerLaserAmplifier;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier> {

    private GuiTextField minField;
    private GuiTextField maxField;
    private GuiTextField timerField;

    public GuiLaserAmplifier(InventoryPlayer inventory, TileEntityLaserAmplifier tile) {
        super(tile, new ContainerLaserAmplifier(inventory, tile));
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
            public String getText(double level) {
                return LangUtils.localize("gui.storing") + ": " + MekanismUtils
                      .getEnergyDisplay(level, tileEntity.getMaxEnergy());
            }
        }, Type.STANDARD, this, resource, 6, 10));
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiAmplifierTab(this, tileEntity, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), 55, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(tileEntity.time > 0 ? LangUtils.localize("gui.delay") + ": " + tileEntity.time + "t"
              : LangUtils.localize("gui.noDelay"), 26, 30, 0x404040);
        fontRenderer.drawString(
              LangUtils.localize("gui.min") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.minThreshold), 26, 45,
              0x404040);
        fontRenderer.drawString(
              LangUtils.localize("gui.max") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.maxThreshold), 26, 60,
              0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        minField.drawTextBox();
        maxField.drawTextBox();
        timerField.drawTextBox();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        minField.updateCursorCounter();
        maxField.updateCursorCounter();
        timerField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);

        minField.mouseClicked(mouseX, mouseY, button);
        maxField.mouseClicked(mouseX, mouseY, button);
        timerField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png");
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!(minField.isFocused() || maxField.isFocused() || timerField.isFocused()) || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }

        if (i == Keyboard.KEY_RETURN) {
            if (minField.isFocused()) {
                setMinThreshold();
            }
            if (maxField.isFocused()) {
                setMaxThreshold();
            }
            if (timerField.isFocused()) {
                setTime();
            }
        }

        if (Character.isDigit(c) || c == '.' || c == 'E' || isTextboxKey(c, i)) {
            minField.textboxKeyTyped(c, i);
            maxField.textboxKeyTyped(c, i);
            timerField.textboxKeyTyped(c, i);
        }
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

            Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

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

            Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

            maxField.setText("");
        }
    }

    private void setTime() {
        if (!timerField.getText().isEmpty()) {
            int toUse = Math.max(0, Integer.parseInt(timerField.getText()));

            TileNetworkList data = TileNetworkList.withContents(2, toUse);

            Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

            timerField.setText("");
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;

        String prevTime = timerField != null ? timerField.getText() : "";

        timerField = new GuiTextField(0, fontRenderer, guiWidth + 96, guiHeight + 28, 36, 11);
        timerField.setMaxStringLength(4);
        timerField.setText(prevTime);

        String prevMin = minField != null ? minField.getText() : "";
        minField = new GuiTextField(1, fontRenderer, guiWidth + 96, guiHeight + 43, 72, 11);
        minField.setMaxStringLength(10);
        minField.setText(prevMin);

        String prevMax = maxField != null ? maxField.getText() : "";

        maxField = new GuiTextField(2, fontRenderer, guiWidth + 96, guiHeight + 58, 72, 11);
        maxField.setMaxStringLength(10);
        maxField.setText(prevMax);
    }
}