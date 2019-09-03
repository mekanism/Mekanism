package mekanism.client.gui.robit;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiRobit<CONTAINER extends Container & IEntityContainer<EntityRobit>> extends GuiMekanism<CONTAINER> {

    protected final EntityRobit robit;

    protected GuiRobit(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        robit = container.getEntity();
        xSize += 25;
    }

    @Override
    public void init() {
        super.init();
        addButton(new MekanismImageButton(guiLeft + 179, guiTop + 10, 18, false, getButtonLocation("main"),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_MAIN, robit.getEntityId()))));
        addButton(new MekanismImageButton(guiLeft + 179, guiTop + 30, 18, false, getButtonLocation("crafting"),
              onPress -> {
                  if (shouldOpenGui(RobitGuiType.CRAFTING)) {
                      Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit.getEntityId()));
                  }
              }));
        addButton(new MekanismImageButton(guiLeft + 179, guiTop + 50, 18, false, getButtonLocation("inventory"),
              onPress -> {
                  if (shouldOpenGui(RobitGuiType.INVENTORY)) {
                      Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit.getEntityId()));
                  }
              }));
        addButton(new MekanismImageButton(guiLeft + 179, guiTop + 70, 18, false, getButtonLocation("smelting"),
              onPress -> {
                  if (shouldOpenGui(RobitGuiType.SMELTING)) {
                      Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit.getEntityId()));
                  }
              }));
        addButton(new MekanismImageButton(guiLeft + 179, guiTop + 90, 18, false, getButtonLocation("repair"),
              onPress -> {
                  if (shouldOpenGui(RobitGuiType.REPAIR)) {
                      Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit.getEntityId()));
                  }
              }));
    }

    private ResourceLocation getButtonLocation(String name) {
        return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "robit/" + name + ".png");
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, getBackgroundImage());
    }

    protected abstract String getBackgroundImage();

    protected abstract boolean shouldOpenGui(RobitGuiType guiType);

    public enum RobitGuiType {
        CRAFTING,
        INVENTORY,
        SMELTING,
        REPAIR
    }
}