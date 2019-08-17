package mekanism.client.gui.filter;

import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
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
        buttons.add(itemStackButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 32, 128, 20, "gui.itemstack", onItemStackButton()));
        buttons.add(oredictButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 52, 128, 20, "gui.oredict", onTagButton()));
        buttons.add(materialButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 72, 128, 20, "gui.material", onMaterialButton()));
        buttons.add(modIDButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 92, 128, 20, "gui.modID", onModIDButton()));
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(), onBackButton()));
    }

    protected abstract IPressable onItemStackButton();

    protected abstract IPressable onTagButton();

    protected abstract IPressable onMaterialButton();

    protected abstract IPressable onModIDButton();

    protected abstract IPressable onBackButton();

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("mekanism.gui.filterSelect.title"), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}