package mekanism.client.gui.filter;

import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilterSelect<TILE extends TileEntityMekanism> extends GuiFilter<TILE> {

    protected Button itemStackButton;
    protected Button oredictButton;
    protected Button materialButton;
    protected Button modIDButton;
    protected Button backButton;

    protected GuiFilterSelect(PlayerEntity player, TILE tile) {
        super(tile, new ContainerNull(player, tile));
    }

    @Override
    protected void addButtons() {
        buttons.add(itemStackButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 32, 128, 20, "gui.itemstack", onPress -> sendPacketToServer(1)));
        buttons.add(oredictButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 52, 128, 20, "gui.oredict", onPress -> sendPacketToServer(2)));
        buttons.add(materialButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 72, 128, 20, "gui.material", onPress -> sendPacketToServer(3)));
        buttons.add(modIDButton = new GuiButtonTranslation(guiLeft + 24, guiTop + 92, 128, 20, "gui.modID", onPress -> onModIDButton()));
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(0)));
    }

    protected abstract IPressable onModIDButton();

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.filterSelect.title")), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}