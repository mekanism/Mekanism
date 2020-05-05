package mekanism.client.gui.qio;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.PacketGuiInteract.GuiInteractionItem;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.StackUtils;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiQIORedstoneAdapter extends GuiMekanismTile<TileEntityQIORedstoneAdapter, MekanismTileContainer<TileEntityQIORedstoneAdapter>> {

    private TextFieldWidget text;

    public GuiQIORedstoneAdapter(MekanismTileContainer<TileEntityQIORedstoneAdapter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 16;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiSlot(SlotType.NORMAL, this, 7, 30).setRenderHover(true));
        addButton(new GuiInnerScreen(this, 7, 16, xSize - 15, 12, () -> {
            List<ITextComponent> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq != null) {
                list.add(MekanismLang.FREQUENCY.translate(freq.getKey()));
            } else {
                list.add(MekanismLang.NO_FREQUENCY.translate());
            }
            return list;
        }).tooltip(() -> {
            List<ITextComponent> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq != null) {
                list.add(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      QIOFrequency.formatItemCount(freq.getTotalItemCount()), QIOFrequency.formatItemCount(freq.getTotalItemCountCapacity())));
                list.add(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      QIOFrequency.formatItemCount(freq.getTotalItemTypes(true)), QIOFrequency.formatItemCount(freq.getTotalItemTypeCapacity())));
            }
            return list;
        }));
        addButton(new GuiInnerScreen(this, 27, 30, xSize - 27 - 8, 54, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(!tile.getItemType().isEmpty() ? tile.getItemType().getStack().getDisplayName() : MekanismLang.QIO_ITEM_TYPE_UNDEFINED.translate());
            list.add(MekanismLang.QIO_TRIGGER_COUNT.translate(QIOFrequency.formatItemCount(tile.getCount())));
            if (!tile.getItemType().isEmpty() && tile.getQIOFrequency() != null) {
                list.add(MekanismLang.QIO_STORED_COUNT.translate(QIOFrequency.formatItemCount(tile.getStoredCount())));
            }
            return list;
        }).clearFormat());
        addButton(text = new TextFieldWidget(font, getGuiLeft() + 29, getGuiTop() + 70, xSize - 27 - 12, 12, ""));
        text.setMaxStringLength(10);
        text.changeFocus(true);
        addButton(new MekanismImageButton(this, getGuiLeft() + xSize - 10 - 12, getGuiTop() + 70, 12, getButtonLocation("checkmark"), this::setCount));
    }

    private void setCount() {
        if (!text.getText().isEmpty()) {
            long count = Long.parseLong(text.getText());
            Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_REDSTONE_ADAPTER_COUNT, tile, (int) Math.min(count, Integer.MAX_VALUE)));
            text.setText("");
        }
    }

    @Override
    public void tick() {
        super.tick();
        text.tick();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        if (tile.getItemType() != null) {
            renderItem(tile.getItemType(), 8, 31);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - getGuiLeft();
            double yAxis = mouseY - getGuiTop();
            if (xAxis >= 8 && xAxis < 24 && yAxis >= 31 && yAxis < 47) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && !hasShiftDown()) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, StackUtils.size(stack, 1)));
                } else if (stack.isEmpty() && hasShiftDown()) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, ItemStack.EMPTY));
                }
                SoundHandler.playSound(MekanismSounds.BEEP.get());
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (text.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                text.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                setCount();
                return true;
            }
            return text.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (text.canWrite()) {
            if (Character.isDigit(c)) {
                return text.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }
}