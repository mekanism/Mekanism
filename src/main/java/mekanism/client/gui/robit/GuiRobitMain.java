package mekanism.client.gui.robit;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.button.DisableableImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiRobitMain extends GuiMekanism<MainRobitContainer> {

    private final EntityRobit robit;

    private boolean displayNameChange;
    private TextFieldWidget nameChangeField;
    private Button confirmName;

    public GuiRobitMain(MainRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize += 25;
        robit = container.getEntity();
    }

    private void toggleNameChange() {
        displayNameChange = !displayNameChange;
        confirmName.visible = displayNameChange;
        nameChangeField.setFocused2(displayNameChange);
    }

    private void changeName() {
        if (!nameChangeField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), TextComponentUtil.getString(nameChangeField.getText())));
            toggleNameChange();
            nameChangeField.setText("");
        }
    }

    @Override
    public void init() {
        super.init();
        addButton(confirmName = new TranslationButton(guiLeft + 58, guiTop + 47, 60, 20, "gui.confirm", onPress -> changeName()));
        confirmName.visible = displayNameChange;

        nameChangeField = new TextFieldWidget(font, guiLeft + 48, guiTop + 21, 80, 12, "");
        nameChangeField.setMaxStringLength(12);
        nameChangeField.setFocused2(true);

        addButton(new DisableableImageButton(guiLeft + 6, guiTop + 16, 18, 18, 219, 54, -18, getGuiLocation(), onPress -> {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.GO_HOME, robit.getEntityId()));
            minecraft.displayGuiScreen(null);
        }, getOnHover("mekanism.gui.robit.teleport")));
        addButton(new DisableableImageButton(guiLeft + 6, guiTop + 35, 18, 18, 219, 90, -18, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.DROP_PICKUP, robit.getEntityId())),
              getOnHover("mekanism.gui.robit.togglePickup")));
        addButton(new DisableableImageButton(guiLeft + 6, guiTop + 54, 18, 18, 201, 234, -18, getGuiLocation(),
              onPress -> toggleNameChange(), getOnHover("mekanism.gui.robit.rename")));
        addButton(new DisableableImageButton(guiLeft + 152, guiTop + 54, 18, 18, 201, 198, -18, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.FOLLOW, robit.getEntityId())),
              getOnHover("mekanism.gui.robit.toggleFollow")));
        addButton(new DisableableImageButton(guiLeft + 179, guiTop + 10, 18, 18, 201, 18, -18, getGuiLocation(), onPress -> {
            //Clicking main button doesn't do anything while already on the main GUI
        }));
        addButton(new DisableableImageButton(guiLeft + 179, guiTop + 30, 18, 18, 201, 54, -18, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit.getEntityId()))));
        addButton(new DisableableImageButton(guiLeft + 179, guiTop + 50, 18, 18, 201, 90, -18, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit.getEntityId()))));
        addButton(new DisableableImageButton(guiLeft + 179, guiTop + 70, 18, 18, 201, 126, -18, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit.getEntityId()))));
        addButton(new DisableableImageButton(guiLeft + 179, guiTop + 90, 18, 18, 201, 162, -18, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit.getEntityId()))));
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (!displayNameChange) {
            return super.charTyped(c, i);
        }
        if (i == GLFW.GLFW_KEY_ENTER) {
            changeName();
            return true;
        } else if (i == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeScreen();
            return true;
        }
        return nameChangeField.charTyped(c, i);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("mekanism.gui.robit"), 76, 6, 0x404040);

        if (!displayNameChange) {
            CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.robit.greeting"), " " + robit.getName() + "!"), 29, 18, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.energy"), ": ", EnergyDisplay.of(robit.getEnergy(), robit.MAX_ELECTRICITY)),
                  29, 36 - 4, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.robit.following"), ": " + robit.getFollowing()), 29, 45 - 4, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.robit.dropPickup"), ": " + robit.getDropPickup()), 29, 54 - 4, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("mekanism.gui.robit.owner"), ": " + owner), 29, 63 - 4, 0x00CD00);
        }

        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 28 && xAxis <= 148 && yAxis >= 75 && yAxis <= 79) {
            displayTooltip(EnergyDisplay.of(robit.getEnergy(), robit.MAX_ELECTRICITY).getTextComponent(), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 28, guiTop + 75, 0, 166, getScaledEnergyLevel(120), 4);
        if (displayNameChange) {
            drawTexturedRect(guiLeft + 28, guiTop + 17, 0, 166 + 4, 120, 54);
            //TODO: Figure out what the parameters do
            nameChangeField.renderButton(0, 0, 0);
            MekanismRenderer.resetColor();
        }
    }

    private int getScaledEnergyLevel(int i) {
        return (int) (robit.getEnergy() * i / robit.MAX_ELECTRICITY);
    }

    @Override
    public void tick() {
        super.tick();
        nameChangeField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        nameChangeField.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "robit_main.png");
    }
}