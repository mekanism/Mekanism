package mekanism.client.gui.robit;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.gui.button.MekanismImageButton;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiRobitMain extends GuiMekanism<MainRobitContainer> {

    private final EntityRobit robit;

    private TextFieldWidget nameChangeField;
    private MekanismButton confirmName;

    public GuiRobitMain(MainRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize += 25;
        robit = container.getEntity();
    }

    private void toggleNameChange() {
        nameChangeField.visible = !nameChangeField.visible;
        confirmName.visible = nameChangeField.visible;
        nameChangeField.setFocused2(nameChangeField.visible);
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
        addButton(confirmName = new TranslationButton(this, guiLeft + 58, guiTop + 47, 60, 20, "gui.mekanism.confirm", this::changeName));
        confirmName.visible = false;

        addButton(nameChangeField = new TextFieldWidget(font, guiLeft + 48, guiTop + 21, 80, 12, ""));
        nameChangeField.setMaxStringLength(12);
        nameChangeField.setFocused2(true);
        nameChangeField.visible = false;

        addButton(new MekanismImageButton(this, guiLeft + 6, guiTop + 16, 18, getButtonLocation("home"), () -> {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.GO_HOME, robit.getEntityId()));
            minecraft.displayGuiScreen(null);
        }, getOnHover("gui.mekanism.robit.teleport")));
        addButton(new MekanismImageButton(this, guiLeft + 6, guiTop + 35, 18, getButtonLocation("drop"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.DROP_PICKUP, robit.getEntityId())),
              getOnHover("gui.mekanism.robit.togglePickup")));
        addButton(new MekanismImageButton(this, guiLeft + 6, guiTop + 54, 18, getButtonLocation("rename"),
              this::toggleNameChange, getOnHover("gui.mekanism.robit.rename")));
        addButton(new MekanismImageButton(this, guiLeft + 152, guiTop + 54, 18, getButtonLocation("follow"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.FOLLOW, robit.getEntityId())),
              getOnHover("gui.mekanism.robit.toggleFollow")));
        addButton(new MekanismImageButton(this, guiLeft + 179, guiTop + 10, 18, getButtonLocation("main"), () -> {
            //Clicking main button doesn't do anything while already on the main GUI
        }));
        addButton(new MekanismImageButton(this, guiLeft + 179, guiTop + 30, 18, getButtonLocation("crafting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit.getEntityId()))));
        addButton(new MekanismImageButton(this, guiLeft + 179, guiTop + 50, 18, getButtonLocation("inventory"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit.getEntityId()))));
        addButton(new MekanismImageButton(this, guiLeft + 179, guiTop + 70, 18, getButtonLocation("smelting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit.getEntityId()))));
        addButton(new MekanismImageButton(this, guiLeft + 179, guiTop + 90, 18, getButtonLocation("repair"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit.getEntityId()))));
    }

    @Override
    public boolean charTyped(char c, int i) {
        //TODO: FIXME
        if (!nameChangeField.visible) {
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
        drawString(TextComponentUtil.translate("gui.mekanism.robit"), 76, 6, 0x404040);

        if (!nameChangeField.visible) {
            CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.robit.greeting"), " ", robit.getName(), "!"), 29, 18, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.energy"), ": ", EnergyDisplay.of(robit.getEnergy(), robit.MAX_ELECTRICITY)),
                  29, 36 - 4, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.robit.following"), ": " + robit.getFollowing()), 29, 45 - 4, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.robit.dropPickup"), ": " + robit.getDropPickup()), 29, 54 - 4, 0x00CD00);
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.robit.owner"), ": " + owner), 29, 63 - 4, 0x00CD00);
        }
        //TODO: 1.14 Convert to GuiElement
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
        if (nameChangeField.visible) {
            drawTexturedRect(guiLeft + 28, guiTop + 17, 0, 166 + 4, 120, 54);
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
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "robit_main.png");
    }
}