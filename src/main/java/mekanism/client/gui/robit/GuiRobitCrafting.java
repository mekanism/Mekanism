package mekanism.client.gui.robit;

import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.robit.ContainerRobitCrafting;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiRobitCrafting extends GuiRobit {

    public GuiRobitCrafting(PlayerInventory inventory, EntityRobit entity) {
        super(entity, new ContainerRobitCrafting(inventory, entity));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(LangUtils.localize("gui.robit.crafting"), 8, 6, 0x404040);
        font.drawString(LangUtils.localize("container.inventory"), 8, ySize - 93, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected String getBackgroundImage() {
        return "GuiRobitCrafting.png";
    }

    @Override
    protected boolean shouldOpenGui(int id) {
        return id != 1;
    }
}