package mekanism.client.gui.element.window.filter.miner;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget.IGhostItemConsumer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GuiMinerItemStackFilter extends GuiItemStackFilter<MinerItemStackFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {

    public static GuiMinerItemStackFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerItemStackFilter(gui, (gui.getXSize() - MINER_FILTER_WIDTH) / 2, 30, tile, null);
    }

    public static GuiMinerItemStackFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerItemStackFilter filter) {
        return new GuiMinerItemStackFilter(gui, (gui.getXSize() - MINER_FILTER_WIDTH) / 2, 30, tile, filter);
    }

    private GuiMinerItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, @Nullable MinerItemStackFilter origFilter) {
        super(gui, x, y, MINER_FILTER_WIDTH, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addMinerDefaults(gui(), getSlotOffset(), this::addChild);
    }

    @Override
    protected MinerItemStackFilter createNewFilter() {
        return new MinerItemStackFilter();
    }

    @Nullable
    @Override
    protected IGhostItemConsumer getGhostHandler() {
        return new IGhostItemConsumer() {
            @Nullable
            @Override
            public ItemStack supportedTarget(Object ingredient) {
                //Note: The miner requires the player to actually get targets, unless configured server side to be "easy"
                return MekanismConfig.general.easyMinerFilters.get() ? IGhostItemConsumer.super.supportedTarget(ingredient) : null;
            }

            @Override
            public void accept(Object ingredient) {
                setFilterStackWithSound(((ItemStack) ingredient).copyWithCount(1));
            }
        };
    }
}