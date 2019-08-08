package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.widget.button.Button;
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
        buttonList.add(itemStackButton = new Button(0, guiLeft + 24, guiTop + 32, 128, 20, LangUtils.localize("gui.itemstack")));
        buttonList.add(oredictButton = new Button(1, guiLeft + 24, guiTop + 52, 128, 20, LangUtils.localize("gui.oredict")));
        buttonList.add(materialButton = new Button(2, guiLeft + 24, guiTop + 72, 128, 20, LangUtils.localize("gui.material")));
        buttonList.add(modIDButton = new Button(3, guiLeft + 24, guiTop + 92, 128, 20, LangUtils.localize("gui.modID")));
        buttonList.add(backButton = new GuiButtonDisableableImage(4, guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(LangUtils.localize("gui.filterSelect.title"), 43, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(Button guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == itemStackButton.id) {
            sendPacketToServer(1);
        } else if (guibutton.id == oredictButton.id) {
            sendPacketToServer(2);
        } else if (guibutton.id == materialButton.id) {
            sendPacketToServer(3);
        } else if (guibutton.id == backButton.id) {
            sendPacketToServer(0);
        }
    }
}