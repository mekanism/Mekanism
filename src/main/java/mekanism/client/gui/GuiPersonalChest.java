package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.inventory.container.ContainerPersonalChest;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPersonalChest extends GuiMekanismTile<TileEntityPersonalChest, ContainerPersonalChest> {

    public GuiPersonalChest(PlayerInventory inventory, TileEntityPersonalChest tile) {
        super(tile, new ContainerPersonalChest(inventory, tile), inventory);
        xSize += 26;
        ySize += 64;
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, getGuiLocation()));
    }

    public GuiPersonalChest(PlayerInventory inventory, InventoryPersonalChest inv) {
        super(null, new ContainerPersonalChest(inventory, inv), inventory);
        xSize += 26;
        ySize += 64;
        addGuiElement(new GuiSecurityTab(this, getGuiLocation(), inv.currentHand));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.build(Translation.of("block.personal_chest")), 8, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("container.inventory")), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiPersonalChest.png");
    }
}