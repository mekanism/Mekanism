package mekanism.client.gui.element.filter.miner;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.filter.GuiMaterialFilter;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostBlockItemConsumer;
import mekanism.common.content.miner.MinerMaterialFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;

public class GuiMinerMaterialFilter extends GuiMaterialFilter<MinerMaterialFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {

    public static GuiMinerMaterialFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerMaterialFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, null);
    }

    public static GuiMinerMaterialFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerMaterialFilter filter) {
        return new GuiMinerMaterialFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, filter);
    }

    private GuiMinerMaterialFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, MinerMaterialFilter origFilter) {
        super(gui, x, y, 173, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addMinerDefaults(guiObj, filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected MinerMaterialFilter createNewFilter() {
        return new MinerMaterialFilter();
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        renderReplaceStack(guiObj, filter);
    }

    @Nullable
    @Override
    protected IGhostBlockItemConsumer getGhostHandler() {
        //Note: The miner requires the player to actually get targets
        return null;
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        return tryClickReplaceStack(guiObj, mouseX, mouseY, button, getSlotOffset(), filter) || super.func_231044_a_(mouseX, mouseY, button);
    }
}