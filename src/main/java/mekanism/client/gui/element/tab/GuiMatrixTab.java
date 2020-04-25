package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiMatrixTab extends GuiTabElementType<TileEntityInductionCasing, MatrixTab> {

    public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tile, MatrixTab type) {
        super(gui, tile, type);
    }

    public enum MatrixTab implements TabType<TileEntityInductionCasing> {
        MAIN("energy.png", MekanismLang.MAIN_TAB, ClickedTileButton.TAB_MAIN),
        STAT("stats.png", MekanismLang.MATRIX_STATS, ClickedTileButton.TAB_STATS);

        private final ClickedTileButton button;
        private final ILangEntry description;
        private final String path;

        MatrixTab(String path, ILangEntry description, ClickedTileButton button) {
            this.path = path;
            this.description = description;
            this.button = button;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI, path);
        }

        @Override
        public void onClick(TileEntityInductionCasing tile) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(button, tile));
        }

        @Override
        public ITextComponent getDescription() {
            return description.translate();
        }

        @Override
        public int getYPos() {
            return 6;
        }
    }
}