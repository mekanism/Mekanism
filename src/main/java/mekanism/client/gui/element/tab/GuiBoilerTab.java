package mekanism.client.gui.element.tab;

import java.util.function.Function;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.BoilerStatsContainer;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBoilerTab extends GuiTabElementType<TileEntityBoilerCasing, BoilerTab> {

    public GuiBoilerTab(IGuiWrapper gui, TileEntityBoilerCasing tile, BoilerTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum BoilerTab implements TabType<TileEntityBoilerCasing> {
        MAIN("gases.png", "gui.mekanism.main", tile ->
              new ContainerProvider("mekanism.container.thermoelectric_boiler", (i, inv, player) -> new ThermoelectricBoilerContainer(i, inv, tile))),
        STAT("stats.png", "gui.mekanism.stats", tile ->
              new ContainerProvider("mekanism.container.boiler_stats", (i, inv, player) -> new BoilerStatsContainer(i, inv, tile)));

        private final Function<TileEntityBoilerCasing, INamedContainerProvider> provider;
        private final String description;
        private final String path;

        BoilerTab(String path, String desc, Function<TileEntityBoilerCasing, INamedContainerProvider> provider) {
            this.path = path;
            description = desc;
            this.provider = provider;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        @Override
        public INamedContainerProvider getProvider(TileEntityBoilerCasing tile) {
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