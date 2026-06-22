package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.button.PacketTileButtonPress;
import mekanism.common.network.to_server.button.PacketTileButtonPress.ClickedTileButton;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiBoilerTab extends GuiTabElementType<TileEntityBoilerCasing, BoilerTab> {

    public GuiBoilerTab(IGuiWrapper gui, TileEntityBoilerCasing tile, BoilerTab type) {
        super(gui, tile, type);
    }

    public enum BoilerTab implements TabType<TileEntityBoilerCasing> {
        MAIN(TransmissionType.CHEMICAL.guiTexture(), MekanismLang.MAIN_TAB, ClickedTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
        STAT(Mekanism.rl("button/stats"), MekanismLang.BOILER_STATS, ClickedTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ColorRegistryObject colorRO;
        private final ClickedTileButton button;
        private final ILangEntry description;
        private final Identifier resource;

        BoilerTab(Identifier resource, ILangEntry description, ClickedTileButton button, ColorRegistryObject colorRO) {
            this.resource = resource;
            this.description = description;
            this.button = button;
            this.colorRO = colorRO;
        }

        @Override
        public Identifier getResource() {
            return resource;
        }

        @Override
        public void onClick(TileEntityBoilerCasing tile) {
            PacketUtils.sendToServer(new PacketTileButtonPress(button, tile));
        }

        @Override
        public Component getDescription() {
            return description.translate();
        }

        @Override
        public ColorRegistryObject getTabColor() {
            return colorRO;
        }
    }
}