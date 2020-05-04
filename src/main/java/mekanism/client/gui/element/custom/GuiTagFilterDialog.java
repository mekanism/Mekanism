package mekanism.client.gui.element.custom;

import java.util.Collections;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.TagCache;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;

public class GuiTagFilterDialog extends GuiFilterDialog<QIOTagFilter> {

    protected MekanismButton checkboxButton;
    protected TextFieldWidget text;
    protected GuiSequencedSlotDisplay slotDisplay;

    public <TILE extends TileEntityMekanism & ITileFilterHolder<QIOTagFilter>>
    GuiTagFilterDialog(IGuiWrapper gui, int x, int y, TILE tile) {
        super(gui, x, y, 116, 90, MekanismLang.TAG_FILTER.translate());

        addChild(new GuiSlot(SlotType.NORMAL, gui, 11, 18).setRenderHover(true));
        addChild(new GuiInnerScreen(gui, 33, 18, 111, 43));
        addChild(new TranslationButton(gui, gui.getLeft() + 27, gui.getTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getTagName() != null && !filter.getTagName().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                }
                Mekanism.packetHandler.sendToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
            } else {
                status = MekanismLang.TAG_FILTER_NO_TAG.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addChild(new TranslationButton(gui, gui.getLeft() + 89, gui.getTop() + 62, 60, 20, isNew ? MekanismLang.BUTTON_CANCEL : MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            gui.removeElement(this);
        }));
        addChild(slotDisplay = new GuiSequencedSlotDisplay(gui, relativeX + 12, relativeY + 19, this::getRenderStacks));

        text = new TextFieldWidget(getFont(), gui.getLeft() + 35, gui.getTop() + 47, 95, 12, "");

        if (filter.getTagName() != null && !filter.getTagName().isEmpty()) {
            slotDisplay.updateStackList();
        }
    }

    @Override
    public void renderBackgroundOverlay(int mouseX, int mouseY) {
        super.renderBackgroundOverlay(mouseX, mouseY);
        text.renderButton(mouseX, mouseY, 0);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawScaledText(MekanismLang.TAG_FILTER_TAG.translate(filter.getTagName()), 35, 32, screenTextColor(), 107);
    }

    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = MekanismLang.TAG_FILTER_NO_TAG.translateColored(EnumColor.DARK_RED);
            return;
        } else if (name.equals(filter.getTagName())) {
            status = MekanismLang.TAG_FILTER_SAME_TAG.translateColored(EnumColor.DARK_RED);
            return;
        }
        filter.setTagName(name);
        slotDisplay.updateStackList();
        text.setText("");
    }

    @Override
    public void tick() {
        super.tick();
        text.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (text.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                text.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                setText();
                return true;
            }
            return text.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (text.canWrite()) {
            if (Character.isLetter(c) || Character.isDigit(c) || TransporterFilter.SPECIAL_CHARS.contains(c) || c == ':' || c == '/') {
                return text.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean ret = text.mouseClicked(mouseX, mouseY, button);
        return ret || super.mouseClicked(mouseX, mouseY, button);
    }

    private List<ItemStack> getRenderStacks() {
        if (filter.getTagName() == null || filter.getTagName().isEmpty()) {
            return Collections.emptyList();
        }
        return TagCache.getItemTagStacks(filter.getTagName());
    }
}
