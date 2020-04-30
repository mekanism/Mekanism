package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedItemButton;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

public class GuiQIOFrequencyTab extends GuiInsetElement<TileEntityMekanism> {

    private static final ResourceLocation FREQUENCY = MekanismUtils.getResource(ResourceType.GUI, "frequency.png");

    private final Hand currentHand;
    private boolean isItem;

    public GuiQIOFrequencyTab(IGuiWrapper gui, TileEntityMekanism tile) {
        super(FREQUENCY, gui, tile, -26, 6, 26, 18);
        this.currentHand = Hand.MAIN_HAND;
    }

    public GuiQIOFrequencyTab(IGuiWrapper gui, Hand hand) {
        super(FREQUENCY, gui, null, -26, 6, 26, 18);
        isItem = true;
        currentHand = hand;
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(MekanismLang.SET_FREQUENCY.translate(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (MekanismConfig.general.allowProtection.get()) {
            if (isItem) {
                Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedItemButton.QIO_FREQUENCY_SELECT, currentHand));
            } else {
                Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.QIO_FREQUENCY_SELECT, tile));
            }
        }
    }
}
