package mekanism.client.gui.robit;

import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GuiRobitRepair extends GuiRobit<RepairRobitContainer> implements ContainerListener {

    //Use the vanilla anvil's gui texture
    private static final Identifier TEXT_FIELD_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field");
    private static final Identifier TEXT_FIELD_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field_disabled");
    private static final Identifier ERROR_SPRITE = Identifier.withDefaultNamespace("container/anvil/error");
    private static final Identifier ANVIL_LOCATION = Identifier.withDefaultNamespace("textures/gui/container/anvil.png");
    private final Player player;
    private GuiTextField itemNameField;
    private long msDisplayCost;

    public GuiRobitRepair(RepairRobitContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        this.player = inv.player;
        inventoryLabelY += 1;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        itemNameField = addRenderableWidget(new GuiTextField(this, 60, 21, 103, 12));
        itemNameField.setCanLoseFocus(false);
        itemNameField.setTextColor(-1);
        itemNameField.setTextColorUneditable(-1);
        itemNameField.setBackground(BackgroundType.NONE);
        itemNameField.setMaxLength(50);
        itemNameField.setResponder(this::onNameChanged);
        itemNameField.setEditable(menu.getSlot(0).hasItem());
        setInitialFocus(itemNameField);
        menu.removeSlotListener(this);
        menu.addSlotListener(this);
    }

    @Override
    protected void setInitialFocus(GuiEventListener listener) {
        //Always capture even when leaving JEI
        initialFocusSet = false;
        super.setInitialFocus(listener);
    }

    private void onNameChanged(String newText) {
        if (!newText.isEmpty()) {
            Slot slot = menu.getSlot(0);
            if (slot.hasItem() && !slot.getItem().has(DataComponents.CUSTOM_NAME) && newText.equals(slot.getItem().getHoverName().getString())) {
                newText = "";
            }
            if (menu.setItemName(newText)) {
                getMinecraft().player.connection.send(new ServerboundRenameItemPacket(newText));
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        menu.removeSlotListener(this);
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, itemNameField.getRelativeX(), itemNameField.getRelativeRight() + 4, 0, TextAlignment.CENTER);
        renderInventoryText(guiGraphics, 60);
        int maximumCost = menu.getCost();
        if (maximumCost > 0) {
            if (msDisplayCost == 0) {
                msDisplayCost = Util.getMillis();
            }
            int textColor = 0xFF80FF20;
            Component component = MekanismLang.REPAIR_COST.translate(maximumCost);
            if (maximumCost >= 40 && !getMinecraft().player.getAbilities().instabuild) {
                component = MekanismLang.REPAIR_EXPENSIVE.translate();
                textColor = 0xFFFF6060;
            } else {
                Slot slot = menu.getSlot(2);
                if (!slot.hasItem()) {
                    component = null;
                    msDisplayCost = 0;
                } else if (!slot.mayPickup(player)) {
                    textColor = 0xFFFF6060;
                }
            }

            if (component != null) {
                int min = Math.max(itemNameField.getRelativeX(), imageWidth - font().width(component) - 10);
                int max = imageWidth - 8;
                guiGraphics.fill(min, 67, max, 79, 0x4F000000);
                drawScrollingString(guiGraphics, component, min, 69, TextAlignment.RIGHT, textColor, max - min, 1, true, msDisplayCost);
            }
        } else {
            msDisplayCost = 0;
        }
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.REPAIR;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ANVIL_LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.menu.getSlot(0).hasItem() ? TEXT_FIELD_SPRITE : TEXT_FIELD_DISABLED_SPRITE, this.leftPos + 59, this.topPos + 20, 110, 16);
        if ((menu.getSlot(0).hasItem() || menu.getSlot(1).hasItem()) && !menu.getSlot(2).hasItem()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, leftPos + 99, topPos + 45, 28, 21);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotID, ItemStack stack) {
        if (slotID == 0) {
            itemNameField.setText(stack.isEmpty() ? "" : stack.getHoverName().getString());
            itemNameField.setEditable(!stack.isEmpty());
            setFocused(itemNameField);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int slotID, int value) {
    }
}