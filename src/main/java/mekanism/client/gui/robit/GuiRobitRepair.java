package mekanism.client.gui.robit;

import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.RepairRobitContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GuiRobitRepair extends GuiRobit<RepairRobitContainer> implements ContainerListener {

    //Use the vanilla anvil's gui texture
    private static final Identifier TEXT_FIELD_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field");
    private static final Identifier TEXT_FIELD_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field_disabled");
    private static final Identifier ERROR_SPRITE = Identifier.withDefaultNamespace("container/anvil/error");
    private static final Identifier ANVIL_LOCATION = Identifier.withDefaultNamespace("textures/gui/container/anvil.png");
    private static final int ITEM_NAME_X = 60;
    private static final int ITEM_NAME_WIDTH = 103;

    private final Player player;
    @Nullable
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
        itemNameField = addRenderableWidget(new GuiTextField(this, ITEM_NAME_X, 21, ITEM_NAME_WIDTH, 12))
              .setCanLoseFocus(false)
              .setTextColor(-1)
              .setTextColorUneditable(-1)
              .setBackground(BackgroundType.NONE)
              .setMaxLength(AnvilMenu.MAX_NAME_LENGTH)
              .setResponder(this::onNameChanged)
              .setEditable(menu.getSlot(0).hasItem());
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
            Slot slot = menu.getSlot(AnvilMenu.INPUT_SLOT);
            if (slot.hasItem() && !slot.getItem().has(DataComponents.CUSTOM_NAME) && newText.equals(slot.getItem().getHoverName().getString())) {
                newText = "";
            }
            if (menu.setItemName(newText)) {
                minecraft.player.connection.send(new ServerboundRenameItemPacket(newText));
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
        renderTitleTextWithOffset(guiGraphics, ITEM_NAME_X, ITEM_NAME_X + ITEM_NAME_WIDTH + 4, 0, TextAlignment.CENTER);
        renderInventoryText(guiGraphics, 60);
        int maximumCost = menu.getCost();
        if (maximumCost > 0) {
            if (msDisplayCost == 0) {
                msDisplayCost = Util.getMillis();
            }
            int textColor = 0xFF80FF20;
            Component component = MekanismLang.REPAIR_COST.translate(maximumCost);
            if (maximumCost >= 40 && !player.getAbilities().instabuild) {
                component = MekanismLang.REPAIR_EXPENSIVE.translate();
                textColor = 0xFFFF6060;
            } else {
                Slot slot = menu.getSlot(AnvilMenu.RESULT_SLOT);
                if (!slot.hasItem()) {
                    component = null;
                    msDisplayCost = 0;
                } else if (!slot.mayPickup(player)) {
                    textColor = 0xFFFF6060;
                }
            }

            if (component != null) {
                int min = Math.max(ITEM_NAME_X, imageWidth - font().width(component) - 10);
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
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, menu.getSlot(AnvilMenu.INPUT_SLOT).hasItem() ? TEXT_FIELD_SPRITE : TEXT_FIELD_DISABLED_SPRITE, leftPos + 59, topPos + 20, 110, 16);
        if ((menu.getSlot(AnvilMenu.INPUT_SLOT).hasItem() || menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).hasItem()) && !menu.getSlot(AnvilMenu.RESULT_SLOT).hasItem()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, leftPos + 99, topPos + 45, 28, 21);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotID, ItemStack stack) {
        if (slotID == AnvilMenu.INPUT_SLOT && itemNameField != null) {
            itemNameField.setText(stack.isEmpty() ? "" : stack.getHoverName().getString());
            itemNameField.setEditable(!stack.isEmpty());
            setFocused(itemNameField);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int slotID, int value) {
    }
}