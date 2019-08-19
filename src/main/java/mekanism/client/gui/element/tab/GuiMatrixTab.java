package mekanism.client.gui.element.tab;

import java.util.function.Function;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.MatrixStatsContainer;
import mekanism.common.inventory.container.tile.energy.InductionMatrixContainer;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMatrixTab extends GuiTabElementType<TileEntityInductionCasing, MatrixTab> {

    public GuiMatrixTab(IGuiWrapper gui, TileEntityInductionCasing tile, MatrixTab type, ResourceLocation def) {
        super(gui, tile, type, def);
    }

    public enum MatrixTab implements TabType<TileEntityInductionCasing> {
        MAIN("energy.png", "mekanism.gui.main", tile ->
              new ContainerProvider("mekanism.container.induction_matrix", (i, inv, player) -> new InductionMatrixContainer(i, inv, tile))),
        STAT("stats.png", "mekanism.gui.stats", tile ->
              new ContainerProvider("mekanism.container.matrix_stats", (i, inv, player) -> new MatrixStatsContainer(i, inv, tile)));

        private final Function<TileEntityInductionCasing, INamedContainerProvider> provider;
        private final String description;
        private final String path;

        MatrixTab(String path, String desc, Function<TileEntityInductionCasing, INamedContainerProvider> provider) {
            this.path = path;
            description = desc;
            this.provider = provider;
        }

        @Override
        public ResourceLocation getResource() {
            return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, path);
        }

        @Override
        public INamedContainerProvider getProvider(TileEntityInductionCasing tile) {
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