package mekanism.client.gui.robit;

import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.robit.ContainerRobitCrafting;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRobitCrafting extends GuiRobit<ContainerRobitCrafting> {

    public GuiRobitCrafting(PlayerInventory inventory, EntityRobit entity) {
        super(entity, new ContainerRobitCrafting(inventory, entity), inventory, TextComponentUtil.translate("mekanism.gui.robit.crafting"));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("mekanism.gui.robit.crafting"), 8, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, ySize - 93, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected String getBackgroundImage() {
        return "GuiRobitCrafting.png";
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.CRAFTING;
    }
}