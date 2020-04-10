package mekanism.client.gui.filter;

import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFilterSelect<TILE extends TileEntityMekanism, CONTAINER extends EmptyTileContainer<TILE>> extends GuiFilter<TILE, CONTAINER> {

    protected MekanismButton itemStackButton;
    protected MekanismButton oredictButton;
    protected MekanismButton materialButton;
    protected MekanismButton modIDButton;
    protected MekanismButton backButton;

    protected GuiFilterSelect(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void addButtons() {
        addButton(new GuiElementHolder(this, 23, 31, 130, 82));
        addButton(itemStackButton = new TranslationButton(this, getGuiLeft() + 24, getGuiTop() + 32, 128, 20, MekanismLang.BUTTON_ITEMSTACK_FILTER, onItemStackButton()));
        addButton(oredictButton = new TranslationButton(this, getGuiLeft() + 24, getGuiTop() + 52, 128, 20, MekanismLang.BUTTON_TAG_FILTER, onTagButton()));
        addButton(materialButton = new TranslationButton(this, getGuiLeft() + 24, getGuiTop() + 72, 128, 20, MekanismLang.BUTTON_MATERIAL_FILTER, onMaterialButton()));
        addButton(modIDButton = new TranslationButton(this, getGuiLeft() + 24, getGuiTop() + 92, 128, 20, MekanismLang.BUTTON_MODID_FILTER, onModIDButton()));
        addButton(backButton = new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"), onBackButton()));
    }

    protected abstract Runnable onItemStackButton();

    protected abstract Runnable onTagButton();

    protected abstract Runnable onMaterialButton();

    protected abstract Runnable onModIDButton();

    protected abstract Runnable onBackButton();

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.CREATE_FILTER_TITLE.translate(), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}