package mekanism.client.gui.robit;

import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitSmelting extends GuiRobit<SmeltingRobitContainer> {

    public GuiRobitSmelting(SmeltingRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return robit.getScaledProgress();
            }
        }, ProgressBar.GREEN, this, 77, 37));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.ROBIT_SMELTING.translate(), 8, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, getYSize() - 93, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.SMELTING;
    }
}