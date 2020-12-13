package mekanism.client.gui.element.window.filter.miner;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiMaterialFilter;
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
        addMinerDefaults(gui(), filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected MinerMaterialFilter createNewFilter() {
        return new MinerMaterialFilter();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderReplaceStack(matrix, gui(), filter);
    }

    @Nullable
    @Override
    protected IGhostBlockItemConsumer getGhostHandler() {
        //Note: The miner requires the player to actually get targets
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return tryClickReplaceStack(gui(), mouseX, mouseY, button, getSlotOffset(), filter) || super.mouseClicked(mouseX, mouseY, button);
    }
}