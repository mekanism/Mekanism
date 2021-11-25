package mekanism.client.gui.element.window.filter.miner;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiMaterialFilter;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostBlockItemConsumer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.miner.MinerMaterialFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

public class GuiMinerMaterialFilter extends GuiMaterialFilter<MinerMaterialFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {

    public static GuiMinerMaterialFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerMaterialFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, null);
    }

    public static GuiMinerMaterialFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerMaterialFilter filter) {
        return new GuiMinerMaterialFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, filter);
    }

    private GuiMinerMaterialFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, @Nullable MinerMaterialFilter origFilter) {
        super(gui, x, y, 173, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addMinerDefaults(gui(), filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected MinerMaterialFilter createNewFilter() {
        return new MinerMaterialFilter();
    }

    @Nullable
    @Override
    protected IGhostBlockItemConsumer getGhostHandler() {
        return new IGhostBlockItemConsumer() {
            @Override
            public boolean supportsIngredient(Object ingredient) {
                //Note: The miner requires the player to actually get targets, unless configured server side to be "easy"
                return MekanismConfig.general.easyMinerFilters.get() && IGhostBlockItemConsumer.super.supportsIngredient(ingredient);
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