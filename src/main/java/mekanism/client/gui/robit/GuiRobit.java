package mekanism.client.gui.robit;

import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.button.PacketEntityButtonPress;
import mekanism.common.network.to_server.button.PacketEntityButtonPress.ClickedEntityButton;
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
        addRenderableWidget(GuiSideHolder.create(this, imageWidth, 6, 106, false, false, SpecialColors.TAB_ROBIT_MENU));
        addRenderableWidget(new MekanismImageButton(this, imageWidth + 3, 10, 18, getButtonLocation("main"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketEntityButtonPress(ClickedEntityButton.ROBIT_MAIN, ((GuiRobit<?>) element.gui()).robit))))
              .setTooltip(GuiRobitMain.ROBIT);
        addRenderableWidget(new MekanismImageButton(this, imageWidth + 3, 30, 18, getButtonLocation("crafting"), (element, mouseX, mouseY) ->
              shouldOpenGui(RobitGuiType.CRAFTING) && PacketUtils.sendToServer(new PacketEntityButtonPress(ClickedEntityButton.ROBIT_CRAFTING, ((GuiRobit<?>) element.gui()).robit))))
              .setTooltip(GuiRobitMain.ROBIT_CRAFTING);
        addRenderableWidget(new MekanismImageButton(this, imageWidth + 3, 50, 18, getButtonLocation("inventory"), (element, mouseX, mouseY) ->
              shouldOpenGui(RobitGuiType.INVENTORY) && PacketUtils.sendToServer(new PacketEntityButtonPress(ClickedEntityButton.ROBIT_INVENTORY, ((GuiRobit<?>) element.gui()).robit))))
              .setTooltip(GuiRobitMain.ROBIT_INVENTORY);
        addRenderableWidget(new MekanismImageButton(this, imageWidth + 3, 70, 18, getButtonLocation("smelting"), (element, mouseX, mouseY) ->
              shouldOpenGui(RobitGuiType.SMELTING) && PacketUtils.sendToServer(new PacketEntityButtonPress(ClickedEntityButton.ROBIT_SMELTING, ((GuiRobit<?>) element.gui()).robit))))
              .setTooltip(GuiRobitMain.ROBIT_SMELTING);
        addRenderableWidget(new MekanismImageButton(this, imageWidth + 3, 90, 18, getButtonLocation("repair"), (element, mouseX, mouseY) ->
              shouldOpenGui(RobitGuiType.REPAIR) && PacketUtils.sendToServer(new PacketEntityButtonPress(ClickedEntityButton.ROBIT_REPAIR, ((GuiRobit<?>) element.gui()).robit))))
              .setTooltip(GuiRobitMain.ROBIT_REPAIR);
    }

    protected abstract boolean shouldOpenGui(RobitGuiType guiType);

    public enum RobitGuiType {
        CRAFTING,
        INVENTORY,
        SMELTING,
        REPAIR
    }
}