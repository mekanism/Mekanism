package mekanism.client.gui.robit;

import mekanism.common.inventory.container.entity.robit.SmeltingRobitContainer;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRobitSmelting extends GuiRobit<SmeltingRobitContainer> {

    public GuiRobitSmelting(SmeltingRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
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