package mekanism.generators.client.gui.element;

import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiFissionReactorTab.FissionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGuiButtonPress;
import mekanism.generators.common.network.PacketGeneratorsGuiButtonPress.ClickedGeneratorsTileButton;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiFissionReactorTab extends GuiTabElementType<TileEntityFissionReactorCasing, FissionReactorTab> {

    public GuiFissionReactorTab(IGuiWrapper gui, TileEntityFissionReactorCasing tile, FissionReactorTab type) {
        super(gui, tile, type);
    }

    public enum FissionReactorTab implements TabType<TileEntityFissionReactorCasing> {
        MAIN("radioactive.png", MekanismLang.MAIN_TAB, ClickedGeneratorsTileButton.TAB_MAIN, SpecialColors.TAB_MULTIBLOCK_MAIN),
        STAT("stats.png", GeneratorsLang.STATS_TAB, ClickedGeneratorsTileButton.TAB_STATS, SpecialColors.TAB_MULTIBLOCK_STATS);

        private final ClickedGeneratorsTileButton button;
        private final ColorRegistryObject colorRO;
        private final ILangEntry description;
        private final String path;

        FissionReactorTab(String path, ILangEntry description, ClickedGeneratorsTileButton button, ColorRegistryObject colorRO) {
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
        public void onClick(TileEntityFissionReactorCasing tile) {
            MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiButtonPress(button, tile.getPos()));
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