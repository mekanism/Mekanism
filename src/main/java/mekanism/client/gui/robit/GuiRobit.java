package mekanism.client.gui.robit;

import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiButtonPress.ClickedEntityButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class GuiRobit<CONTAINER extends AbstractContainerMenu & IEntityContainer<EntityRobit>> extends GuiMekanism<CONTAINER> {

    protected final EntityRobit robit;

    protected GuiRobit(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
        robit = container.getEntity();
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiSecurityTab(this, robit, 120));
        addRenderableWidget(GuiSideHolder.create(this, 176, 6, 106, false, false, SpecialColors.TAB_ROBIT_MENU));
        addRenderableWidget(new MekanismImageButton(this, 179, 10, 18, getButtonLocation("main"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_MAIN, robit)),
              getOnHover(MekanismLang.ROBIT)));
        addRenderableWidget(new MekanismImageButton(this, 179, 30, 18, getButtonLocation("crafting"), () -> {
            if (shouldOpenGui(RobitGuiType.CRAFTING)) {
                Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit));
            }
        }, getOnHover(MekanismLang.ROBIT_CRAFTING)));
        addRenderableWidget(new MekanismImageButton(this, 179, 50, 18, getButtonLocation("inventory"), () -> {
            if (shouldOpenGui(RobitGuiType.INVENTORY)) {
                Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit));
            }
        }, getOnHover(MekanismLang.ROBIT_INVENTORY)));
        addRenderableWidget(new MekanismImageButton(this, 179, 70, 18, getButtonLocation("smelting"), () -> {
            if (shouldOpenGui(RobitGuiType.SMELTING)) {
                Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit));
            }
        }, getOnHover(MekanismLang.ROBIT_SMELTING)));
        addRenderableWidget(new MekanismImageButton(this, 179, 90, 18, getButtonLocation("repair"), () -> {
            if (shouldOpenGui(RobitGuiType.REPAIR)) {
                Mekanism.packetHandler().sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit));
            }
        }, getOnHover(MekanismLang.ROBIT_REPAIR)));
    }

    protected abstract boolean shouldOpenGui(RobitGuiType guiType);

    public enum RobitGuiType {
        CRAFTING,
        INVENTORY,
        SMELTING,
        REPAIR
    }
}