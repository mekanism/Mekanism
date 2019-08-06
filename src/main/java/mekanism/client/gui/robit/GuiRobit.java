package mekanism.client.gui.robit;

import java.io.IOException;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiRobit extends GuiMekanism {

    protected final EntityRobit robit;
    private GuiButton mainButton;
    private GuiButton craftingButton;
    private GuiButton inventoryButton;
    private GuiButton smeltingButton;
    private GuiButton repairButton;

    protected GuiRobit(EntityRobit robit, Container container) {
        super(container);
        this.robit = robit;
        xSize += 25;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(mainButton = new GuiButtonDisableableImage(0, guiLeft + 179, guiTop + 10, 18, 18, 201, 18, -18, getGuiLocation()));
        buttonList.add(craftingButton = new GuiButtonDisableableImage(1, guiLeft + 179, guiTop + 30, 18, 18, 201, 54, -18, getGuiLocation()));
        buttonList.add(inventoryButton = new GuiButtonDisableableImage(2, guiLeft + 179, guiTop + 50, 18, 18, 201, 90, -18, getGuiLocation()));
        buttonList.add(smeltingButton = new GuiButtonDisableableImage(3, guiLeft + 179, guiTop + 70, 18, 18, 201, 126, -18, getGuiLocation()));
        buttonList.add(repairButton = new GuiButtonDisableableImage(4, guiLeft + 179, guiTop + 90, 18, 18, 201, 162, -18, getGuiLocation()));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (!shouldOpenGui(guibutton.id)) {
            //Don't do anything when the button is the same one as the one we are on
            return;
        }
        if (guibutton.id == mainButton.id) {
            Mekanism.packetHandler.sendToServer(new RobitMessage(robit.getEntityId(), 21));
        } else if (guibutton.id == craftingButton.id) {
            Mekanism.packetHandler.sendToServer(new RobitMessage(robit.getEntityId(), 22));
        } else if (guibutton.id == inventoryButton.id) {
            Mekanism.packetHandler.sendToServer(new RobitMessage(robit.getEntityId(), 23));
        } else if (guibutton.id == smeltingButton.id) {
            Mekanism.packetHandler.sendToServer(new RobitMessage(robit.getEntityId(), 24));
        } else if (guibutton.id == repairButton.id) {
            Mekanism.packetHandler.sendToServer(new RobitMessage(robit.getEntityId(), 25));
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, getBackgroundImage());
    }

    protected abstract String getBackgroundImage();

    protected abstract boolean shouldOpenGui(int id);
}