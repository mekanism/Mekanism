package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.inventory.container.tile.PersonalChestTileContainer;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPersonalChestTile extends GuiMekanismTile<TileEntityPersonalChest, PersonalChestTileContainer> {

    public GuiPersonalChestTile(PersonalChestTileContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize += 26;
        ySize += 64;
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, getGuiLocation()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("block.personal_chest"), 8, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiPersonalChest.png");
    }
}