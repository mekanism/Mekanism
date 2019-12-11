package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiBoilerTab extends GuiTabElementType<TileEntityBoilerCasing, BoilerTab> {

    public GuiBoilerTab(IGuiWrapper gui, TileEntityBoilerCasing tile, BoilerTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum BoilerTab implements TabType<TileEntityBoilerCasing> {
        MAIN("gases.png", "gui.mekanism.main", ClickedTileButton.TAB_MAIN),
        STAT("stats.png", "gui.mekanism.stats", ClickedTileButton.TAB_STATS);

        private final ClickedTileButton button;
        private final String description;
        private final String path;

        BoilerTab(String path, String desc, ClickedTileButton button) {
            this.path = path;
            description = desc;
            this.button = button;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        @Override
        public void onClick(TileEntityBoilerCasing tile) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(button, tile.getPos()));
        }

        @Override
        public ITextComponent getDescription() {
            return TextComponentUtil.translate(description);
        }

        @Override
        public int getYPos() {
            return 6;
        }
    }
}