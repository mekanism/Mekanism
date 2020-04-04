package mekanism.client.gui.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.inventory.container.tile.filter.LSItemStackFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiTItemStackFilter extends GuiItemStackFilter<TItemStackFilter, TileEntityLogisticalSorter, LSItemStackFilterContainer> {

    private TextFieldWidget minField;
    private TextFieldWidget maxField;

    public GuiTItemStackFilter(LSItemStackFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 18).setRenderHover(true));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 43));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 47, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!filter.getItemStack().isEmpty() && !minField.getText().isEmpty() && !maxField.getText().isEmpty()) {
                int min = Integer.parseInt(minField.getText());
                int max = Integer.parseInt(maxField.getText());
                if (max >= min && max <= 64) {
                    filter.min = Integer.parseInt(minField.getText());
                    filter.max = Integer.parseInt(maxField.getText());
                    if (isNew) {
                        Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                    } else {
                        Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                    }
                    sendPacketToServer(ClickedTileButton.BACK_BUTTON);
                } else if (min > max) {
                    status = MekanismLang.ITEM_FILTER_MAX_LESS_THAN_MIN.translateColored(EnumColor.DARK_RED);
                    ticker = 20;
                } else { //if(max > 64 || min > 64)
                    status = MekanismLang.ITEM_FILTER_OVER_SIZED.translateColored(EnumColor.DARK_RED);
                    ticker = 20;
                }
            } else if (filter.getItemStack().isEmpty()) {
                status = MekanismLang.ITEM_FILTER_NO_ITEM.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            } else if (minField.getText().isEmpty() || maxField.getText().isEmpty()) {
                status = MekanismLang.ITEM_FILTER_SIZE_MISSING.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 109, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(isNew ? ClickedTileButton.LS_SELECT_FILTER_TYPE : ClickedTileButton.BACK_BUTTON)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 62, 10, getButtonLocation("default"),
              () -> filter.allowDefault = !filter.allowDefault, getOnHover(MekanismLang.FILTER_ALLOW_DEFAULT)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 72, 10, getButtonLocation("fuzzy"),
              () -> filter.fuzzyMode = !filter.fuzzyMode, getOnHover(MekanismLang.FUZZY_MODE)));
        addButton(new ColorButton(this, getGuiLeft() + 12, getGuiTop() + 44, 16, 16, () -> filter.color,
              () -> filter.color = hasShiftDown() ? null : TransporterUtils.increment(filter.color), () -> filter.color = TransporterUtils.decrement(filter.color)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 128, getGuiTop() + 44, 11, 14, getButtonLocation("silk_touch"),
              () -> filter.sizeMode = !filter.sizeMode,
              (onHover, xAxis, yAxis) -> {
                  if (tile.singleItem && filter.sizeMode) {
                      displayTooltip(MekanismLang.SIZE_MODE_CONFLICT.translate(), xAxis, yAxis);
                  } else {
                      displayTooltip(MekanismLang.SIZE_MODE.translate(), xAxis, yAxis);
                  }
              }));
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 33, 18, 93, 43));
        addButton(minField = new TextFieldWidget(font, getGuiLeft() + 149, getGuiTop() + 19, 20, 11, ""));
        minField.setMaxStringLength(2);
        minField.setText("" + filter.min);
        addButton(maxField = new TextFieldWidget(font, getGuiLeft() + 149, getGuiTop() + 31, 20, 11, ""));
        maxField.setMaxStringLength(2);
        maxField.setText("" + filter.max);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String min = minField.getText();
        String max = maxField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        minField.setText(min);
        maxField.setText(max);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                focusedField.setFocused2(false);
                return true;
            }
            return focusedField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (Character.isDigit(c)) {
                return focusedField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    @Nullable
    private TextFieldWidget getFocusedField() {
        if (minField.canWrite()) {
            return minField;
        } else if (maxField.canWrite()) {
            return maxField;
        }
        return null;
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.MIN.translate(""), 128, 20, 0x404040);
        drawString(MekanismLang.MAX.translate(""), 128, 32, 0x404040);
        if (tile.singleItem && filter.sizeMode) {
            drawString(MekanismLang.ITEM_FILTER_SIZE_MODE.translateColored(EnumColor.RED, OnOff.of(filter.sizeMode)), 141, 46, 0x404040);
        } else {
            drawString(OnOff.of(filter.sizeMode).getTextComponent(), 141, 46, 0x404040);
        }
        drawString(OnOff.of(filter.fuzzyMode).getTextComponent(), 24, 74, 0x404040);
        drawTransporterForegroundLayer(filter.getItemStack());
        if (!filter.getItemStack().isEmpty()) {
            renderScaledText(filter.getItemStack().getDisplayName(), 35, 41, 0x00CD00, 89);
        }
    }

    @Override
    protected void drawTransporterForegroundLayer(@Nonnull ItemStack stack) {
        drawString(OnOff.of(filter.allowDefault).getTextComponent(), 24, 64, 0x404040);
        renderItem(stack, 12, 19);
    }

    @Override
    public void tick() {
        super.tick();
        minField.tick();
        maxField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && overTypeInput(mouseX - getGuiLeft(), mouseY - getGuiTop())) {
            ItemStack stack = minecraft.player.inventory.getItemStack();
            if (!stack.isEmpty() && !hasShiftDown()) {
                filter.setItemStack(stack.copy());
                filter.getItemStack().setCount(1);
            } else if (stack.isEmpty() && hasShiftDown()) {
                filter.setItemStack(ItemStack.EMPTY);
            }
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
        return true;
    }
}