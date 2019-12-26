package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.PersonalChestTileContainer;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiPersonalChestTile extends GuiMekanismTile<TileEntityPersonalChest, PersonalChestTileContainer> {

    public GuiPersonalChestTile(PersonalChestTileContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize += 26;
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, tile, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 8, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "personal_chest.png");
    }
}