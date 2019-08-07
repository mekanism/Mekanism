package mekanism.client.gui.robit;

import java.io.IOException;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.robit.ContainerRobitMain;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.input.Keyboard;

@OnlyIn(Dist.CLIENT)
public class GuiRobitMain extends GuiMekanism {

    private final EntityRobit robit;

    private boolean displayNameChange;
    private TextFieldWidget nameChangeField;
    private Button confirmName;
    private Button teleportHomeButton;
    private Button pickupButton;
    private Button renameButton;
    private Button followButton;
    private Button mainButton;
    private Button craftingButton;
    private Button inventoryButton;
    private Button smeltingButton;
    private Button repairButton;

    public GuiRobitMain(PlayerInventory inventory, EntityRobit entity) {
        super(new ContainerRobitMain(inventory, entity));
        xSize += 25;
        robit = entity;
    }

    private void toggleNameChange() {
        displayNameChange = !displayNameChange;
        confirmName.visible = displayNameChange;
        nameChangeField.setFocused(displayNameChange);
    }

    private void changeName() {
        if (!nameChangeField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), nameChangeField.getText()));
            toggleNameChange();
            nameChangeField.setText("");
        }
    }

    @Override
    protected void actionPerformed(Button guibutton) {
        if (guibutton.id == confirmName.id) {
            changeName();
        } else if (guibutton.id == teleportHomeButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.GO_HOME, robit.getEntityId()));
            mc.displayGuiScreen(null);
        } else if (guibutton.id == pickupButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.DROP_PICKUP, robit.getEntityId()));
        } else if (guibutton.id == renameButton.id) {
            toggleNameChange();
        } else if (guibutton.id == followButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.FOLLOW, robit.getEntityId()));
        } else if (guibutton.id == mainButton.id) {
            //Clicking main button doesn't do anything while already on the main GUI
        } else if (guibutton.id == craftingButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), 22));
        } else if (guibutton.id == inventoryButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), 23));
        } else if (guibutton.id == smeltingButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), 24));
        } else if (guibutton.id == repairButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), 25));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(confirmName = new Button(0, guiLeft + 58, guiTop + 47, 60, 20, LangUtils.localize("gui.confirm")));
        confirmName.visible = displayNameChange;

        nameChangeField = new TextFieldWidget(1, fontRenderer, guiLeft + 48, guiTop + 21, 80, 12);
        nameChangeField.setMaxStringLength(12);
        nameChangeField.setFocused(true);

        buttonList.add(teleportHomeButton = new GuiButtonDisableableImage(2, guiLeft + 6, guiTop + 16, 18, 18, 219, 54, -18, getGuiLocation()));
        buttonList.add(pickupButton = new GuiButtonDisableableImage(3, guiLeft + 6, guiTop + 35, 18, 18, 219, 90, -18, getGuiLocation()));
        buttonList.add(renameButton = new GuiButtonDisableableImage(4, guiLeft + 6, guiTop + 54, 18, 18, 201, 234, -18, getGuiLocation()));
        buttonList.add(followButton = new GuiButtonDisableableImage(5, guiLeft + 152, guiTop + 54, 18, 18, 201, 198, -18, getGuiLocation()));
        buttonList.add(mainButton = new GuiButtonDisableableImage(6, guiLeft + 179, guiTop + 10, 18, 18, 201, 18, -18, getGuiLocation()));
        buttonList.add(craftingButton = new GuiButtonDisableableImage(7, guiLeft + 179, guiTop + 30, 18, 18, 201, 54, -18, getGuiLocation()));
        buttonList.add(inventoryButton = new GuiButtonDisableableImage(8, guiLeft + 179, guiTop + 50, 18, 18, 201, 90, -18, getGuiLocation()));
        buttonList.add(smeltingButton = new GuiButtonDisableableImage(9, guiLeft + 179, guiTop + 70, 18, 18, 201, 126, -18, getGuiLocation()));
        buttonList.add(repairButton = new GuiButtonDisableableImage(10, guiLeft + 179, guiTop + 90, 18, 18, 201, 162, -18, getGuiLocation()));
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!displayNameChange) {
            super.keyTyped(c, i);
        } else {
            if (i == Keyboard.KEY_RETURN) {
                changeName();
            } else if (i == Keyboard.KEY_ESCAPE) {
                mc.player.closeScreen();
            }
            nameChangeField.textboxKeyTyped(c, i);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("gui.robit"), 76, 6, 0x404040);

        if (!displayNameChange) {
            CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
            fontRenderer.drawString(LangUtils.localize("gui.robit.greeting") + " " + robit.getName() + "!", 29, 18, 0x00CD00);
            fontRenderer.drawString(LangUtils.localize("gui.energy") + ": " + MekanismUtils.getEnergyDisplay(robit.getEnergy(), robit.MAX_ELECTRICITY),
                  29, 36 - 4, 0x00CD00);
            fontRenderer.drawString(LangUtils.localize("gui.robit.following") + ": " + robit.getFollowing(), 29, 45 - 4, 0x00CD00);
            fontRenderer.drawString(LangUtils.localize("gui.robit.dropPickup") + ": " + robit.getDropPickup(), 29, 54 - 4, 0x00CD00);
            fontRenderer.drawString(LangUtils.localize("gui.robit.owner") + ": " + owner, 29, 63 - 4, 0x00CD00);
        }

        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 28 && xAxis <= 148 && yAxis >= 75 && yAxis <= 79) {
            displayTooltip(MekanismUtils.getEnergyDisplay(robit.getEnergy(), robit.MAX_ELECTRICITY), xAxis, yAxis);
        } else if (followButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.robit.toggleFollow"), xAxis, yAxis);
        } else if (renameButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.robit.rename"), xAxis, yAxis);
        } else if (teleportHomeButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.robit.teleport"), xAxis, yAxis);
        } else if (pickupButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.robit.togglePickup"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedModalRect(guiLeft + 28, guiTop + 75, 0, 166, getScaledEnergyLevel(120), 4);
        if (displayNameChange) {
            drawTexturedModalRect(guiLeft + 28, guiTop + 17, 0, 166 + 4, 120, 54);
            nameChangeField.drawTextBox();
            MekanismRenderer.resetColor();
        }
    }

    private int getScaledEnergyLevel(int i) {
        return (int) (robit.getEnergy() * i / robit.MAX_ELECTRICITY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        nameChangeField.updateCursorCounter();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        nameChangeField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiRobitMain.png");
    }
}