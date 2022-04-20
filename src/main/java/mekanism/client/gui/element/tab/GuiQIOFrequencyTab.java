package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedItemButton;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class GuiQIOFrequencyTab extends GuiInsetElement<TileEntityMekanism> {

    private static final ResourceLocation FREQUENCY = MekanismUtils.getResource(ResourceType.GUI, "frequency.png");

    private final InteractionHand currentHand;
    private boolean isItem;

    public GuiQIOFrequencyTab(IGuiWrapper gui, TileEntityMekanism tile) {
        super(FREQUENCY, gui, tile, -26, 6, 26, 18, true);
        this.currentHand = InteractionHand.MAIN_HAND;
    }

    public GuiQIOFrequencyTab(IGuiWrapper gui, InteractionHand hand) {
        super(FREQUENCY, gui, null, -26, 6, 26, 18, true);
        isItem = true;
        currentHand = hand;
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_QIO_FREQUENCY);
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, MekanismLang.SET_FREQUENCY.translate());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isItem) {
            Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedItemButton.QIO_FREQUENCY_SELECT, currentHand));
        } else {
            Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedTileButton.QIO_FREQUENCY_SELECT, dataSource));
        }
    }
}
