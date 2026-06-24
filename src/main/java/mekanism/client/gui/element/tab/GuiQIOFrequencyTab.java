package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.button.PacketItemButtonPress;
import mekanism.common.network.to_server.button.PacketItemButtonPress.ClickedItemButton;
import mekanism.common.network.to_server.button.PacketTileButtonPress;
import mekanism.common.network.to_server.button.PacketTileButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.Nullable;

public abstract class GuiQIOFrequencyTab<DATA_SOURCE extends @Nullable Object> extends GuiInsetElement<DATA_SOURCE> {

    private static final Identifier FREQUENCY = Mekanism.rl("button/frequency");

    protected GuiQIOFrequencyTab(IGuiWrapper gui, DATA_SOURCE dataSource) {
        super(FREQUENCY, gui, dataSource, -26, 6, 26, 18, true);
        setTooltip(MekanismLang.SET_FREQUENCY);
    }

    @Override
    protected int getTabColor() {
        return SpecialColors.TAB_QIO_FREQUENCY.argb();
    }

    @Override
    public abstract void onClick(MouseButtonEvent event, boolean isDoubleClick);

    public static class GuiQIOFrequencyTileTab extends GuiQIOFrequencyTab<TileEntityMekanism> {

        public GuiQIOFrequencyTileTab(IGuiWrapper gui, TileEntityMekanism tile) {
            super(gui, tile);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
            PacketUtils.sendToServer(new PacketTileButtonPress(ClickedTileButton.QIO_FREQUENCY_SELECT, dataSource));
        }
    }

    public static class GuiQIOFrequencyItemTab extends GuiQIOFrequencyTab<InteractionHand> {

        public GuiQIOFrequencyItemTab(IGuiWrapper gui, InteractionHand hand) {
            super(gui, hand);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
            PacketUtils.sendToServer(new PacketItemButtonPress(ClickedItemButton.QIO_FREQUENCY_SELECT, dataSource));
        }
    }
}
