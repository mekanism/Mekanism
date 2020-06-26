package mekanism.client.gui.robit;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.custom.GuiRobitRename;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitMain extends GuiMekanism<MainRobitContainer> {

    private final EntityRobit robit;
    private MekanismImageButton renameButton;

    public GuiRobitMain(MainRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        robit = container.getEntity();
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiSideHolder(this, 176, 6, 106, false));
        func_230480_a_(new GuiInnerScreen(this, 27, 16, 122, 56));
        func_230480_a_(new GuiHorizontalPowerBar(this, robit.getEnergyContainer(), 27, 74, 120));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 16, 18, getButtonLocation("home"), () -> {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.GO_HOME, robit.getEntityId()));
            getMinecraft().displayGuiScreen(null);
        }, getOnHover(MekanismLang.ROBIT_TELEPORT)));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 35, 18, getButtonLocation("drop"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.DROP_PICKUP, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_TOGGLE_PICKUP)));
        func_230480_a_(renameButton = new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 54, 18, getButtonLocation("rename"), () -> {
            GuiWindow window = new GuiRobitRename(this, 27, 16, robit);
            window.setListenerTab(() -> renameButton);
            renameButton.field_230693_o_ = false;
            addWindow(window);
        }, getOnHover(MekanismLang.ROBIT_RENAME)));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 152, getGuiTop() + 54, 18, getButtonLocation("follow"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.FOLLOW, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_TOGGLE_FOLLOW)));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 10, 18, getButtonLocation("main"), () -> {
            //Clicking main button doesn't do anything while already on the main GUI
        }, getOnHover(MekanismLang.ROBIT)));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 30, 18, getButtonLocation("crafting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_CRAFTING)));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 50, 18, getButtonLocation("inventory"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_INVENTORY)));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 70, 18, getButtonLocation("smelting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_SMELTING)));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 90, 18, getButtonLocation("repair"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_REPAIR)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.ROBIT.translate(), 76, 6, titleTextColor());
        drawTextScaledBound(MekanismLang.ROBIT_GREETING.translate(robit.getName()), 29, 18, screenTextColor(), 119);
        drawTextScaledBound(MekanismLang.ENERGY.translate(EnergyDisplay.of(robit.getEnergyContainer().getEnergy(), robit.getEnergyContainer().getMaxEnergy())), 29, 36 - 4, screenTextColor(), 119);
        drawTextScaledBound(MekanismLang.ROBIT_FOLLOWING.translate(robit.getFollowing()), 29, 45 - 4, screenTextColor(), 119);
        drawTextScaledBound(MekanismLang.ROBIT_DROP_PICKUP.translate(robit.getDropPickup()), 29, 54 - 4, screenTextColor(), 119);
        CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
        drawTextScaledBound(MekanismLang.ROBIT_OWNER.translate(owner), 29, 63 - 4, screenTextColor(), 119);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}