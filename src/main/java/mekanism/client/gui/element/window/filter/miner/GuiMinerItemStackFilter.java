package mekanism.client.gui.element.window.filter.miner;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostItemConsumer;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;

public class GuiMinerItemStackFilter extends GuiItemStackFilter<MinerItemStackFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {

    public static GuiMinerItemStackFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, null);
    }

    public static GuiMinerItemStackFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerItemStackFilter filter) {
        return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, filter);
    }

    private GuiMinerItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, MinerItemStackFilter origFilter) {
        super(gui, x, y, 173, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addMinerDefaults(guiObj, filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected MinerItemStackFilter createNewFilter() {
        return new MinerItemStackFilter();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderReplaceStack(matrix, guiObj, filter);
    }

    @Nullable
    @Override
    protected IGhostItemConsumer getGhostHandler() {
        //Note: The miner requires the player to actually get targets
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return tryClickReplaceStack(guiObj, mouseX, mouseY, button, getSlotOffset(), filter) || super.mouseClicked(mouseX, mouseY, button);
    }
}