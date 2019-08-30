package mekanism.client.gui.filter;

import mekanism.client.gui.button.DisableableImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.common.inventory.container.tile.filter.FilterEmptyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilterSelect<TILE extends TileEntityMekanism, CONTAINER extends FilterEmptyContainer<TILE>> extends GuiFilter<TILE, CONTAINER> {

    protected Button itemStackButton;
    protected Button oredictButton;
    protected Button materialButton;
    protected Button modIDButton;
    protected Button backButton;

    protected GuiFilterSelect(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void addButtons() {
        addButton(itemStackButton = new TranslationButton(guiLeft + 24, guiTop + 32, 128, 20, "gui.itemstack", onItemStackButton()));
        addButton(oredictButton = new TranslationButton(guiLeft + 24, guiTop + 52, 128, 20, "gui.oredict", onTagButton()));
        addButton(materialButton = new TranslationButton(guiLeft + 24, guiTop + 72, 128, 20, "gui.material", onMaterialButton()));
        addButton(modIDButton = new TranslationButton(guiLeft + 24, guiTop + 92, 128, 20, "gui.modID", onModIDButton()));
        addButton(backButton = new DisableableImageButton(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(), onBackButton()));
    }

    protected abstract IPressable onItemStackButton();

    protected abstract IPressable onTagButton();

    protected abstract IPressable onMaterialButton();

    protected abstract IPressable onModIDButton();

    protected abstract IPressable onBackButton();

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("gui.mekanism.filterSelect.title"), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}