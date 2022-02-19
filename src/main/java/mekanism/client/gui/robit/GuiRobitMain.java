package mekanism.client.gui.robit;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.window.GuiRobitRename;
import mekanism.client.gui.element.window.GuiRobitSkinSelect;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.network.to_server.PacketRobit;
import mekanism.common.network.to_server.PacketRobit.RobitPacketType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitMain extends GuiMekanism<MainRobitContainer> {

    private final EntityRobit robit;
    private MekanismImageButton renameButton;
    private MekanismImageButton skinButton;

    public GuiRobitMain(MainRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        robit = container.getEntity();
        dynamicSlots = true;
        titleLabelX = 76;
    }

    private void openWindow(GuiWindow window, Supplier<? extends GuiElement> elementSupplier) {
        window.setListenerTab(elementSupplier);
        elementSupplier.get().active = false;
        addWindow(window);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiSecurityTab(this, robit, 120));
        addButton(GuiSideHolder.create(this, 176, 6, 106, false, false, SpecialColors.TAB_ROBIT_MENU));
        addButton(new GuiInnerScreen(this, 27, 16, 122, 56));
        addButton(new GuiHorizontalPowerBar(this, robit.getEnergyContainer(), 27, 74, 120));
        addButton(new MekanismImageButton(this, 6, 16, 18, getButtonLocation("home"), () -> {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.GO_HOME, robit));
            getMinecraft().setScreen(null);
        }, getOnHover(MekanismLang.ROBIT_TELEPORT)));
        renameButton = addButton(new MekanismImageButton(this, 6, 35, 18, getButtonLocation("rename"),
              () -> openWindow(new GuiRobitRename(this, 27, 16, robit), () -> renameButton), getOnHover(MekanismLang.ROBIT_RENAME)));
        skinButton = addButton(new MekanismImageButton(this, 6, 54, 18, getButtonLocation("skin"),
              () -> openWindow(new GuiRobitSkinSelect(this, 4, -12, robit), () -> skinButton), getOnHover(MekanismLang.ROBIT_SKIN_SELECT)));
        addButton(new MekanismImageButton(this, 152, 35, 18, getButtonLocation("drop"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.DROP_PICKUP, robit)),
              getOnHover(MekanismLang.ROBIT_TOGGLE_PICKUP)));
        addButton(new MekanismImageButton(this, 152, 54, 18, getButtonLocation("follow"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.FOLLOW, robit)),
              getOnHover(MekanismLang.ROBIT_TOGGLE_FOLLOW)));
        addButton(new MekanismImageButton(this, 179, 10, 18, getButtonLocation("main"), () -> {
            //Clicking main button doesn't do anything while already on the main GUI
        }, getOnHover(MekanismLang.ROBIT)));
        addButton(new MekanismImageButton(this, 179, 30, 18, getButtonLocation("crafting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit)),
              getOnHover(MekanismLang.ROBIT_CRAFTING)));
        addButton(new MekanismImageButton(this, 179, 50, 18, getButtonLocation("inventory"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit)),
              getOnHover(MekanismLang.ROBIT_INVENTORY)));
        addButton(new MekanismImageButton(this, 179, 70, 18, getButtonLocation("smelting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit)),
              getOnHover(MekanismLang.ROBIT_SMELTING)));
        addButton(new MekanismImageButton(this, 179, 90, 18, getButtonLocation("repair"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit)),
              getOnHover(MekanismLang.ROBIT_REPAIR)));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, MekanismLang.ROBIT.translate(), titleLabelX, titleLabelY, titleTextColor());
        drawTextScaledBound(matrix, MekanismLang.ROBIT_GREETING.translate(robit.getName()), 29, 18, screenTextColor(), 119);
        drawTextScaledBound(matrix, MekanismLang.ENERGY.translate(EnergyDisplay.of(robit.getEnergyContainer())), 29, 36 - 4, screenTextColor(), 119);
        drawTextScaledBound(matrix, MekanismLang.ROBIT_FOLLOWING.translate(robit.getFollowing()), 29, 45 - 4, screenTextColor(), 119);
        drawTextScaledBound(matrix, MekanismLang.ROBIT_DROP_PICKUP.translate(robit.getDropPickup()), 29, 54 - 4, screenTextColor(), 119);
        CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
        drawTextScaledBound(matrix, MekanismLang.ROBIT_OWNER.translate(owner), 29, 63 - 4, screenTextColor(), 119);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}