package mekanism.client.gui.filter;

import mekanism.client.gui.button.MekanismButton;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.common.inventory.container.tile.filter.FilterEmptyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFilterSelect<TILE extends TileEntityMekanism, CONTAINER extends FilterEmptyContainer<TILE>> extends GuiFilter<TILE, CONTAINER> {

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
        addButton(itemStackButton = new TranslationButton(this, guiLeft + 24, guiTop + 32, 128, 20, "gui.mekanism.itemstack", onItemStackButton()));
        addButton(oredictButton = new TranslationButton(this, guiLeft + 24, guiTop + 52, 128, 20, "gui.mekanism.oredict", onTagButton()));
        addButton(materialButton = new TranslationButton(this, guiLeft + 24, guiTop + 72, 128, 20, "gui.mekanism.material", onMaterialButton()));
        addButton(modIDButton = new TranslationButton(this, guiLeft + 24, guiTop + 92, 128, 20, "gui.mekanism.modID", onModIDButton()));
        addButton(backButton = new MekanismImageButton(this, guiLeft + 5, guiTop + 5, 11, 14, getButtonLocation("back"), onBackButton()));
    }

    protected abstract Runnable onItemStackButton();

    protected abstract Runnable onTagButton();

    protected abstract Runnable onMaterialButton();

    protected abstract Runnable onModIDButton();

    protected abstract Runnable onBackButton();

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("gui.mekanism.filterSelect.title"), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}