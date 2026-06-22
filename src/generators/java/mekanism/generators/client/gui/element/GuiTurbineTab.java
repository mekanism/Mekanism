package mekanism.generators.client.gui.element;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketUtils;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.network.to_server.PacketGeneratorsTileButtonPress;
import mekanism.generators.common.network.to_server.PacketGeneratorsTileButtonPress.ClickedGeneratorsTileButton;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiTurbineTab extends GuiTabElementType<TileEntityTurbineCasing, TurbineTab> {

    public GuiTurbineTab(IGuiWrapper gui, TileEntityTurbineCasing tile, TurbineTab type) {
        super(gui, tile, type);
    }

    public enum TurbineTab implements TabType<TileEntityTurbineCasing> {
        MAIN(TransmissionType.CHEMICAL.guiTexture(), MekanismLang.MAIN_TAB, ClickedGeneratorsTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
        STAT(Mekanism.rl("button/stats"), GeneratorsLang.TURBINE_STATS, ClickedGeneratorsTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ClickedGeneratorsTileButton button;
        private final ColorRegistryObject colorRO;
        private final ILangEntry description;
        private final Identifier resource;

        TurbineTab(Identifier resource, ILangEntry description, ClickedGeneratorsTileButton button, ColorRegistryObject colorRO) {
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
        public void onClick(TileEntityTurbineCasing tile) {
            PacketUtils.sendToServer(new PacketGeneratorsTileButtonPress(button, tile.getBlockPos()));
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