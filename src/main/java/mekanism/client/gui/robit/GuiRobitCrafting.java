package mekanism.client.gui.robit;

import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.robit.ContainerRobitCrafting;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRobitCrafting extends GuiRobit {

    public GuiRobitCrafting(InventoryPlayer inventory, EntityRobit entity) {
        super(entity, new ContainerRobitCrafting(inventory, entity));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("gui.robit.crafting"), 8, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, ySize - 93, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected String getBackgroundImage() {
        return "GuiRobitCrafting.png";
    }

    @Override
    protected boolean openGui(int id) {
        return id != 1;
    }
}