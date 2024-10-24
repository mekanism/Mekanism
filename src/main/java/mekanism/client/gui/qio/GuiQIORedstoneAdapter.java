package mekanism.client.gui.qio;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget.IGhostItemConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteractionItem;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiQIORedstoneAdapter extends GuiMekanismTile<TileEntityQIORedstoneAdapter, MekanismTileContainer<TileEntityQIORedstoneAdapter>> {

    private GuiTextField text;

    public GuiQIORedstoneAdapter(MekanismTileContainer<TileEntityQIORedstoneAdapter> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 26;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiQIOFrequencyTab(this, tile));
        addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 7, 30).setRenderHover(true)).click((element, mouseX, mouseY) -> {
            GuiQIORedstoneAdapter gui = (GuiQIORedstoneAdapter) element.gui();
            ItemStack stack = gui.getCarriedItem();
            if (stack.isEmpty() == hasShiftDown()) {
                //If the stack is empty and shift is being held, clear it
                // otherwise if the stack is not empty and shift is not being held set it
                gui.updateStack(stack);
                return true;
            }
            return false;
        }, 1.0F, () -> hasShiftDown() ? MekanismSounds.BEEP_OFF.get() : MekanismSounds.BEEP_ON.get()).setGhostHandler((IGhostItemConsumer) ingredient -> {
            updateStack((ItemStack) ingredient);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(MekanismSounds.BEEP_ON.get(), 1.0F, 1.0F));
        });
        addRenderableWidget(new ToggleButton(this, 9, 64, 14, tile::isInverted,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.INVERSE_BUTTON, ((GuiQIORedstoneAdapter) element.gui()).tile))))
              .setTooltip(MekanismLang.REDSTONE_ADAPTER_TOGGLE_SIGNAL);
        addRenderableWidget(new MekanismImageButton(this, 9, 80, 14, getButtonLocation("fuzzy"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_REDSTONE_ADAPTER_FUZZY, ((GuiQIORedstoneAdapter) element.gui()).tile))))
              .setTooltip(MekanismLang.FUZZY_MODE);
        addRenderableWidget(new GuiInnerScreen(this, 7, 16, imageWidth - 15, 12, GuiQIOFilterHandler.getFrequencyText(tile))
              .tooltip(GuiQIOFilterHandler.getFrequencyTooltip(tile)));
        addRenderableWidget(new GuiInnerScreen(this, 27, 30, imageWidth - 27 - 8, 64, () -> {
            List<Component> list = new ArrayList<>();
            ItemStack itemType = tile.getItemType();
            list.add(itemType.isEmpty() ? MekanismLang.QIO_ITEM_TYPE_UNDEFINED.translate() : itemType.getHoverName());
            ILangEntry match = tile.isInverted() ? MekanismLang.GENERIC_LESS_THAN : MekanismLang.GENERIC_GREATER_EQUAL;
            list.add(match.translate(MekanismLang.QIO_TRIGGER_COUNT, TextUtils.format(tile.getCount())));
            if (!itemType.isEmpty() && tile.getQIOFrequency() != null) {
                list.add(MekanismLang.QIO_STORED_COUNT.translate(TextUtils.format(tile.getStoredCount())));
            }
            list.add(MekanismLang.QIO_FUZZY_MODE.translate(tile.getFuzzyMode()));
            return list;
        }).clearFormat());
        text = addRenderableWidget(new GuiTextField(this, 29, 80, imageWidth - 39, 12));
        text.setInputValidator(InputValidator.DIGIT)
              .configureDigitalInput(this::setCount)
              .setMaxLength(10);
        setInitialFocus(text);
    }

    private void updateStack(ItemStack stack) {
        //Note: Empty stack will be returned as empty by StackUtils#size, so we do not have to special case it
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, stack.copyWithCount(1)));
    }

    private void setCount() {
        if (!text.getText().isEmpty()) {
            long count = Long.parseLong(text.getText());
            PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_REDSTONE_ADAPTER_COUNT, tile, (int) Math.min(count, Integer.MAX_VALUE)));
            text.setText("");
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        renderItem(guiGraphics, tile.getItemType(), 8, 31);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}