package mekanism.generators.client.gui.element;

import java.util.function.Function;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.inventory.container.reactor.info.ReactorFuelContainer;
import mekanism.generators.common.inventory.container.reactor.info.ReactorHeatContainer;
import mekanism.generators.common.inventory.container.reactor.info.ReactorStatsContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorTab extends GuiTabElementType<TileEntityReactorController, ReactorTab> {

    public GuiReactorTab(IGuiWrapper gui, TileEntityReactorController tile, ReactorTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum ReactorTab implements TabType<TileEntityReactorController> {
        HEAT("GuiHeatTab.png", "mekanism.gui.heat", 6, tile ->
              new ContainerProvider("mekanism.container.reactor_heat", (i, inv, player) -> new ReactorHeatContainer(i, inv, tile))),
        FUEL("GuiFuelTab.png", "mekanism.gui.fuel", 34, tile ->
              new ContainerProvider("mekanism.container.reactor_fuel", (i, inv, player) -> new ReactorFuelContainer(i, inv, tile))),
        STAT("GuiStatsTab.png", "mekanism.gui.stats", 62, tile ->
              new ContainerProvider("mekanism.container.reactor_stats", (i, inv, player) -> new ReactorStatsContainer(i, inv, tile)));

        private final Function<TileEntityReactorController, INamedContainerProvider> provider;
        private final String description;
        private final String path;
        private final int yPos;

        ReactorTab(String path, String desc, int y, Function<TileEntityReactorController, INamedContainerProvider> provider) {
            this.path = path;
            description = desc;
            yPos = y;
            this.provider = provider;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        @Override
        public INamedContainerProvider getProvider(TileEntityReactorController tile) {
            return provider.apply(tile);
        }

        @Override
        public ITextComponent getDescription() {
            return TextComponentUtil.translate(description);
        }

        @Override
        public int getYPos() {
            return yPos;
        }
    }
}