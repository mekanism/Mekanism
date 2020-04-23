package mekanism.client.gui;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.TagCache;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiDigitalMinerConfig extends GuiFilterHolder<MinerFilter<?>, TileEntityDigitalMiner, EmptyTileContainer<TileEntityDigitalMiner>> {

    private TextFieldWidget radiusField;
    private TextFieldWidget minField;
    private TextFieldWidget maxField;

    public GuiDigitalMinerConfig(EmptyTileContainer<TileEntityDigitalMiner> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void tick() {
        super.tick();
        radiusField.tick();
        minField.tick();
        maxField.tick();
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 38, 66, 13, 13));
        addButton(new GuiInnerScreen(this, 38, 91, 13, 13));
        addButton(new GuiInnerScreen(this, 38, 116, 13, 13));
        addButton(new TranslationButton(this, getGuiLeft() + 56, getGuiTop() + 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_SELECT_FILTER_TYPE, tile))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 39, getGuiTop() + 67, 11, 12, getButtonLocation("checkmark"), this::setRadius));
        addButton(new MekanismImageButton(this, getGuiLeft() + 39, getGuiTop() + 92, 11, 12, getButtonLocation("checkmark"), this::setMinY));
        addButton(new MekanismImageButton(this, getGuiLeft() + 39, getGuiTop() + 117, 11, 12, getButtonLocation("checkmark"), this::setMaxY));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 141, 14, getButtonLocation("strict_input"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.INVERSE_BUTTON, tile)), getOnHover(MekanismLang.MINER_INVERSE)));
        addButton(radiusField = new TextFieldWidget(font, getGuiLeft() + 12, getGuiTop() + 67, 26, 11, ""));
        radiusField.setMaxStringLength(Integer.toString(MekanismConfig.general.minerMaxRadius.get()).length());
        addButton(minField = new TextFieldWidget(font, getGuiLeft() + 12, getGuiTop() + 92, 26, 11, ""));
        minField.setMaxStringLength(3);
        addButton(maxField = new TextFieldWidget(font, getGuiLeft() + 12, getGuiTop() + 117, 26, 11, ""));
        maxField.setMaxStringLength(3);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String prevRad = radiusField.getText();
        String prevMin = minField.getText();
        String prevMax = maxField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        radiusField.setText(prevRad);
        minField.setText(prevMin);
        maxField.setText(prevMax);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.MINER_CONFIG.translate(), 43, 6, titleTextColor());
        drawString(MekanismLang.FILTERS.translate(), 11, 19, screenTextColor());
        drawString(MekanismLang.FILTER_COUNT.translate(getFilters().size()), 11, 28, screenTextColor());
        drawString(MekanismLang.MINER_IS_INVERSE.translate(OnOff.of(tile.inverse)), 11, 131, screenTextColor());
        drawString(MekanismLang.MINER_RADIUS.translate(tile.getRadius()), 11, 58, screenTextColor());
        drawString(MekanismLang.MIN.translate(tile.getMinY()), 11, 83, screenTextColor());
        drawString(MekanismLang.MAX.translate(tile.getMaxY()), 11, 108, screenTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_ITEMSTACK, tile, index));
        } else if (filter instanceof ITagFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_TAG, tile, index));
        } else if (filter instanceof IMaterialFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_MATERIAL, tile, index));
        } else if (filter instanceof IModIDFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_MOD_ID, tile, index));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                focusedField.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (radiusField.canWrite()) {
                    setRadius();
                } else if (minField.canWrite()) {
                    setMinY();
                } else if (maxField.canWrite()) {
                    setMaxY();
                }
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
        if (radiusField.canWrite()) {
            return radiusField;
        } else if (minField.canWrite()) {
            return minField;
        } else if (maxField.canWrite()) {
            return maxField;
        }
        return null;
    }

    private void setRadius() {
        if (!radiusField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SET_RADIUS, tile, Integer.parseInt(radiusField.getText())));
            radiusField.setText("");
        }
    }

    private void setMinY() {
        if (!minField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SET_MIN_Y, tile, Integer.parseInt(minField.getText())));
            minField.setText("");
        }
    }

    private void setMaxY() {
        if (!maxField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SET_MAX_Y, tile, Integer.parseInt(maxField.getText())));
            maxField.setText("");
        }
    }

    @Override
    protected List<ItemStack> getTagStacks(String tagName) {
        return TagCache.getBlockTagStacks(tagName);
    }
}