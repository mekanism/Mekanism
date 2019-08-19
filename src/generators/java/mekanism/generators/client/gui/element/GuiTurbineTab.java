package mekanism.generators.client.gui.element;

import java.util.function.Function;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiTabElementType;
import mekanism.client.gui.element.tab.TabType;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.inventory.container.turbine.TurbineContainer;
import mekanism.generators.common.inventory.container.turbine.TurbineStatsContainer;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTurbineTab extends GuiTabElementType<TileEntityTurbineCasing, TurbineTab> {

    public GuiTurbineTab(IGuiWrapper gui, TileEntityTurbineCasing tile, TurbineTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum TurbineTab implements TabType<TileEntityTurbineCasing> {
        MAIN("gases.png", "mekanism.gui.main", tile ->
              new ContainerProvider("mekanism.container.industrial_turbine", (i, inv, player) -> new TurbineContainer(i, inv, tile))),
        STAT("stats.png", "mekanism.gui.stats", tile ->
              new ContainerProvider("mekanism.container.turbine_stats", (i, inv, player) -> new TurbineStatsContainer(i, inv, tile)));

        private final Function<TileEntityTurbineCasing, INamedContainerProvider> provider;
        private final String description;
        private final String path;

        TurbineTab(String path, String desc, Function<TileEntityTurbineCasing, INamedContainerProvider> provider) {
            this.path = path;
            description = desc;
            this.provider = provider;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        @Override
        public INamedContainerProvider getProvider(TileEntityTurbineCasing tile) {
            return provider.apply(tile);
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