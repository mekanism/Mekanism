package mekanism.client.gui.element.window.filter.miner;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostItemConsumer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.StackUtils;
import net.minecraft.world.item.ItemStack;

public class GuiMinerItemStackFilter extends GuiItemStackFilter<MinerItemStackFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {

    public static GuiMinerItemStackFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, null);
    }

    public static GuiMinerItemStackFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerItemStackFilter filter) {
        return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, filter);
    }

    private GuiMinerItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, @Nullable MinerItemStackFilter origFilter) {
        super(gui, x, y, 173, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addMinerDefaults(gui(), filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected MinerItemStackFilter createNewFilter() {
        return new MinerItemStackFilter();
    }

    @Nullable
    @Override
    protected IGhostItemConsumer getGhostHandler() {
        return new IGhostItemConsumer() {
            @Override
            public boolean supportsIngredient(Object ingredient) {
                //Note: The miner requires the player to actually get targets, unless configured server side to be "easy"
                return MekanismConfig.general.easyMinerFilters.get() && IGhostItemConsumer.super.supportsIngredient(ingredient);
            }

            @Override
            public void accept(Object ingredient) {
                setFilterStack(StackUtils.size((ItemStack) ingredient, 1));
            }
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return tryClickReplaceStack(gui(), mouseX, mouseY, button, getSlotOffset(), filter) || super.mouseClicked(mouseX, mouseY, button);
    }
}