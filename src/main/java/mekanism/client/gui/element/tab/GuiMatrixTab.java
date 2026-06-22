package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.button.PacketTileButtonPress;
import mekanism.common.network.to_server.button.PacketTileButtonPress.ClickedTileButton;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiMatrixTab extends GuiTabElementType<TileEntityInductionCasing, MatrixTab> {

    public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tile, MatrixTab type) {
        super(gui, tile, type);
    }

    public enum MatrixTab implements TabType<TileEntityInductionCasing> {
        MAIN(TransmissionType.ENERGY.guiTexture(), MekanismLang.MAIN_TAB, ClickedTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
        STAT(Mekanism.rl("button/stats"), MekanismLang.MATRIX_STATS, ClickedTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ColorRegistryObject colorRO;
        private final ClickedTileButton button;
        private final ILangEntry description;
        private final Identifier resource;

        MatrixTab(Identifier resource, ILangEntry description, ClickedTileButton button, ColorRegistryObject colorRO) {
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
        public void onClick(TileEntityInductionCasing tile) {
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