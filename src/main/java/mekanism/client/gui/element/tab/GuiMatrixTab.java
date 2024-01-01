package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.button.PacketTileButtonPress;
import mekanism.common.network.to_server.button.PacketTileButtonPress.ClickedTileButton;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiMatrixTab extends GuiTabElementType<TileEntityInductionCasing, MatrixTab> {

    public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tile, MatrixTab type) {
        super(gui, tile, type);
    }

    public enum MatrixTab implements TabType<TileEntityInductionCasing> {
        MAIN("energy.png", MekanismLang.MAIN_TAB, ClickedTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
        STAT("stats.png", MekanismLang.MATRIX_STATS, ClickedTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ColorRegistryObject colorRO;
        private final ClickedTileButton button;
        private final ILangEntry description;
        private final String path;

        MatrixTab(String path, ILangEntry description, ClickedTileButton button, ColorRegistryObject colorRO) {
            this.path = path;
            this.description = description;
            this.button = button;
            this.colorRO = colorRO;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI, path);
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