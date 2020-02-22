package mekanism.client.gui;

import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.OreDictCache;
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
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
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
    protected void upButtonPress(int index) {
        if (index > 0) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(11, index)));
        }
    }

    @Override
    protected void downButtonPress(int index) {
        if (index < getFilters().size() - 1) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(12, index)));
        }
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 38, 66, 13, 13));
        addButton(new GuiInnerScreen(this, 38, 91, 13, 13));
        addButton(new GuiInnerScreen(this, 38, 116, 13, 13));
        addButton(new TranslationButton(this, getGuiLeft() + 56, getGuiTop() + 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_SELECT_FILTER_TYPE, tile.getPos()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile.getPos()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 39, getGuiTop() + 67, 11, 12, getButtonLocation("checkmark"), this::setRadius));
        addButton(new MekanismImageButton(this, getGuiLeft() + 39, getGuiTop() + 92, 11, 12, getButtonLocation("checkmark"), this::setMinY));
        addButton(new MekanismImageButton(this, getGuiLeft() + 39, getGuiTop() + 117, 11, 12, getButtonLocation("checkmark"), this::setMaxY));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 141, 14, getButtonLocation("strict_input"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(10))), getOnHover(MekanismLang.MINER_INVERSE)));

        String prevRad = radiusField != null ? radiusField.getText() : "";
        String prevMin = minField != null ? minField.getText() : "";
        String prevMax = maxField != null ? maxField.getText() : "";

        addButton(radiusField = new TextFieldWidget(font, getGuiLeft() + 12, getGuiTop() + 67, 26, 11, ""));
        radiusField.setMaxStringLength(Integer.toString(MekanismConfig.general.digitalMinerMaxRadius.get()).length());
        radiusField.setText(prevRad);

        addButton(minField = new TextFieldWidget(font, getGuiLeft() + 12, getGuiTop() + 92, 26, 11, ""));
        minField.setMaxStringLength(3);
        minField.setText(prevMin);

        addButton(maxField = new TextFieldWidget(font, getGuiLeft() + 12, getGuiTop() + 117, 26, 11, ""));
        maxField.setMaxStringLength(3);
        maxField.setText(prevMax);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        HashList<MinerFilter<?>> filters = getFilters();
        drawString(MekanismLang.MINER_CONFIG.translate(), 43, 6, 0x404040);
        drawString(MekanismLang.FILTERS.translate(), 11, 19, 0x00CD00);
        drawString(MekanismLang.FILTER_COUNT.translate(filters.size()), 11, 28, 0x00CD00);
        drawString(MekanismLang.MINER_IS_INVERSE.translate(OnOff.of(tile.inverse)), 11, 131, 0x00CD00);
        drawString(MekanismLang.MINER_RADIUS.translate(tile.getRadius()), 11, 58, 0x00CD00);
        drawString(MekanismLang.MIN.translate(tile.minY), 11, 83, 0x00CD00);
        drawString(MekanismLang.MAX.translate(tile.maxY), 11, 108, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_ITEMSTACK, tile.getPos(), index));
        } else if (filter instanceof ITagFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_TAG, tile.getPos(), index));
        } else if (filter instanceof IMaterialFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_MATERIAL, tile.getPos(), index));
        } else if (filter instanceof IModIDFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_MOD_ID, tile.getPos(), index));
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
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (radiusField.isFocused()) {
                    setRadius();
                } else if (minField.isFocused()) {
                    setMinY();
                } else if (maxField.isFocused()) {
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
        if (radiusField.isFocused()) {
            return radiusField;
        } else if (minField.isFocused()) {
            return minField;
        } else if (maxField.isFocused()) {
            return maxField;
        }
        return null;
    }

    private void setRadius() {
        if (!radiusField.getText().isEmpty()) {
            int toUse = Math.max(0, Math.min(Integer.parseInt(radiusField.getText()), MekanismConfig.general.digitalMinerMaxRadius.get()));
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(6, toUse)));
            radiusField.setText("");
        }
    }

    private void setMinY() {
        if (!minField.getText().isEmpty()) {
            int toUse = Math.max(0, Math.min(Integer.parseInt(minField.getText()), tile.maxY));
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(7, toUse)));
            minField.setText("");
        }
    }

    private void setMaxY() {
        if (!maxField.getText().isEmpty()) {
            int toUse = Math.max(tile.minY, Math.min(Integer.parseInt(maxField.getText()), 255));
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(8, toUse)));
            maxField.setText("");
        }
    }

    @Override
    protected List<ItemStack> getTagStacks(String tagName) {
        return OreDictCache.getBlockTagStacks(tagName);
    }
}