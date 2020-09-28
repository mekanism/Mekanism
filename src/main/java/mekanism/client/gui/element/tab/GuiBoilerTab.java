package mekanism.client.gui.element.tab;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiBoilerTab extends GuiTabElementType<TileEntityBoilerCasing, BoilerTab> {

    public GuiBoilerTab(IGuiWrapper gui, TileEntityBoilerCasing tile, BoilerTab type) {
        super(gui, tile, type);
    }

    public enum BoilerTab implements TabType<TileEntityBoilerCasing> {
        MAIN("gases.png", MekanismLang.MAIN_TAB, ClickedTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
        STAT("stats.png", MekanismLang.BOILER_STATS, ClickedTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ColorRegistryObject colorRO;
        private final ClickedTileButton button;
        private final ILangEntry description;
        private final String path;

        BoilerTab(String path, ILangEntry description, ClickedTileButton button, ColorRegistryObject colorRO) {
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
        public void onClick(TileEntityBoilerCasing tile) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(button, tile));
        }

        @Override
        public ITextComponent getDescription() {
            return description.translate();
        }

        @Override
        public ColorRegistryObject getTabColor() {
            return colorRO;
        }
    }
}