package mekanism.client.gui.qio;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IGuiColorFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.ITileGuiFrequencySelector;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiQIOTileFrequencySelect extends GuiMekanismTile<TileEntityQIOComponent, EmptyTileContainer<TileEntityQIOComponent>> implements
      IGuiColorFrequencySelector<QIOFrequency>, ITileGuiFrequencySelector<QIOFrequency, TileEntityQIOComponent> {

    public GuiQIOTileFrequencySelect(EmptyTileContainer<TileEntityQIOComponent> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight -= 11;
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFrequencySelector<>(this, 17));
        addRenderableWidget(new MekanismImageButton(this, 6, 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
    }

    @Override
    protected void addGenericTabs() {
        //Don't add the generic tabs when we are selecting a frequency
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public FrequencyType<QIOFrequency> getFrequencyType() {
        return FrequencyType.QIO;
    }

    @Override
    public TileEntityQIOComponent getTileEntity() {
        return tile;
    }

    @Override
    public void drawTitleText(PoseStack matrix, Component text, float y) {
        //Adjust spacing for back button
        int leftShift = 15;
        int xSize = getXSize() - leftShift;
        int maxLength = xSize - 12;
        float textWidth = getStringWidth(text);
        float scale = Math.min(1, maxLength / textWidth);
        drawScaledCenteredText(matrix, text, leftShift + xSize / 2F, y, titleTextColor(), scale);
    }
}