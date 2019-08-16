package mekanism.client.gui.robit;

import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRobitInventory extends GuiRobit<InventoryRobitContainer> {

    public GuiRobitInventory(InventoryRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("mekanism.gui.robit.inventory"), 8, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, ySize - 93, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected String getBackgroundImage() {
        return "GuiRobitInventory.png";
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.INVENTORY;
    }
}