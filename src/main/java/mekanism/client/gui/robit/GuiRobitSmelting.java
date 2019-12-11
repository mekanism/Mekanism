package mekanism.client.gui.robit;

import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitSmelting extends GuiRobit<SmeltingRobitContainer> {

    public GuiRobitSmelting(SmeltingRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        //TODO: Use a progress bar instead of the way it used to do it with the cooking progress
        /*addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getScaledProgress();
            }
        }, ProgressBar.GREEN, this, getGuiLocation(), 77, 37));*/
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("gui.mekanism.robit.smelting"), 8, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, ySize - 93, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected String getBackgroundImage() {
        return "robit_smelting.png";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt;
        if (robit.furnaceBurnTime > 0) {
            displayInt = getBurnTimeRemainingScaled(12);
            drawTexturedRect(guiLeft + 56, guiTop + 36 + 12 - displayInt, 176 + 25 + 18, 36 + 12 - displayInt, 14, displayInt + 2);
        }
        displayInt = getCookProgressScaled(24);
        drawTexturedRect(guiLeft + 79, guiTop + 34, 176 + 25 + 18, 36 + 14, displayInt + 1, 16);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.SMELTING;
    }

    private int getCookProgressScaled(int i) {
        return robit.furnaceCookTime * i / 200;
    }

    private int getBurnTimeRemainingScaled(int i) {
        if (robit.currentItemBurnTime == 0) {
            robit.currentItemBurnTime = 200;
        }
        return robit.furnaceBurnTime * i / robit.currentItemBurnTime;
    }
}