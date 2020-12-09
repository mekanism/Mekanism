package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiFilterHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.window.filter.miner.GuiMinerFilerSelect;
import mekanism.client.gui.element.window.filter.miner.GuiMinerItemStackFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerMaterialFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerModIDFilter;
import mekanism.client.gui.element.window.filter.miner.GuiMinerTagFilter;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerMaterialFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiDigitalMinerConfig extends GuiFilterHolder<MinerFilter<?>, TileEntityDigitalMiner, MekanismTileContainer<TileEntityDigitalMiner>> {

    private GuiTextField radiusField, minField, maxField;

    public GuiDigitalMinerConfig(MekanismTileContainer<TileEntityDigitalMiner> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new TranslationButton(this, guiLeft + 56, guiTop + 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> addWindow(new GuiMinerFilerSelect(this, tile))));
        addButton(new MekanismImageButton(this, guiLeft + 5, guiTop + 5, 11, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
        addButton(new MekanismImageButton(this, guiLeft + 11, guiTop + 141, 14, getButtonLocation("strict_input"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.INVERSE_BUTTON, tile)), getOnHover(MekanismLang.MINER_INVERSE)));
        addButton(radiusField = new GuiTextField(this, 13, 67, 38, 11));
        radiusField.setMaxStringLength(Integer.toString(MekanismConfig.general.minerMaxRadius.get()).length());
        radiusField.setInputValidator(InputValidator.DIGIT);
        radiusField.configureDigitalBorderInput(this::setRadius);
        addButton(minField = new GuiTextField(this, 13, 92, 38, 11));
        minField.setMaxStringLength(3);
        minField.setInputValidator(InputValidator.DIGIT);
        minField.configureDigitalBorderInput(this::setMinY);
        addButton(maxField = new GuiTextField(this, 13, 117, 38, 11));
        maxField.setMaxStringLength(3);
        maxField.setInputValidator(InputValidator.DIGIT);
        maxField.configureDigitalBorderInput(this::setMaxY);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        drawTitleText(matrix, MekanismLang.MINER_CONFIG.translate(), titleY);
        drawTextWithScale(matrix, MekanismLang.FILTERS.translate(), 14, 22, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.FILTER_COUNT.translate(getFilters().size()), 14, 31, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.MINER_IS_INVERSE.translate(OnOff.of(tile.inverse)), 14, 131, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.MINER_RADIUS.translate(tile.getRadius()), 14, 58, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.MIN.translate(tile.getMinY()), 14, 83, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.MAX.translate(tile.getMaxY()), 14, 108, screenTextColor(), 0.8F);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            addWindow(GuiMinerItemStackFilter.edit(this, tile, (MinerItemStackFilter) filter));
        } else if (filter instanceof ITagFilter) {
            addWindow(GuiMinerTagFilter.edit(this, tile, (MinerTagFilter) filter));
        } else if (filter instanceof IMaterialFilter) {
            addWindow(GuiMinerMaterialFilter.edit(this, tile, (MinerMaterialFilter) filter));
        } else if (filter instanceof IModIDFilter) {
            addWindow(GuiMinerModIDFilter.edit(this, tile, (MinerModIDFilter) filter));
        }
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