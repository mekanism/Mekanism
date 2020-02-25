package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiBoilerTab extends GuiTabElementType<TileEntityBoilerCasing, BoilerTab> {

    public GuiBoilerTab(IGuiWrapper gui, TileEntityBoilerCasing tile, BoilerTab type) {
        super(gui, tile, type);
    }

    public enum BoilerTab implements TabType<TileEntityBoilerCasing> {
        MAIN("gases.png", MekanismLang.MAIN_TAB, ClickedTileButton.TAB_MAIN),
        STAT("stats.png", MekanismLang.BOILER_STATS, ClickedTileButton.TAB_STATS);

        private final ClickedTileButton button;
        private final ILangEntry description;
        private final String path;

        BoilerTab(String path, ILangEntry desc, ClickedTileButton button) {
            this.path = path;
            description = desc;
            this.button = button;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI, path);
        }

        @Override
        public void onClick(TileEntityBoilerCasing tile) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(button, tile.getPos()));
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