package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public abstract class GuiMaterialFilter<FILTER extends IMaterialFilter, TILE extends TileEntityMekanism> extends GuiTypeFilter<FILTER, TILE> {

    protected GuiMaterialFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == saveButton.id) {
            if (!filter.getMaterialItem().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(0);
            } else {
                status = EnumColor.DARK_RED + LangUtils.localize("gui.itemFilter.noItem");
                ticker = 20;
            }
        } else if (guibutton.id == deleteButton.id) {
            Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(0);
        } else if (tileEntity instanceof TileEntityDigitalMiner && filter instanceof MinerFilter) {
            actionPerformedMinerCommon(guibutton, (MinerFilter) filter);
        } else if (tileEntity instanceof TileEntityLogisticalSorter && filter instanceof TransporterFilter) {
            actionPerformedTransporter(guibutton, (TransporterFilter) filter);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (ticker > 0) {
            ticker--;
        } else {
            status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString((isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " + LangUtils.localize("gui.materialFilter"), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.status") + ": " + status, 35, 20, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.materialFilter.details") + ":", 35, 32, 0x00CD00);
        drawForegroundLayer(mouseX, mouseY);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    protected void materialMouseClicked() {
        ItemStack stack = mc.player.inventory.getItemStack();
        if (!stack.isEmpty() && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            if (stack.getItem() instanceof ItemBlock) {
                if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                    filter.setMaterialItem(stack.copy());
                    filter.getMaterialItem().setCount(1);
                }
            }
        } else if (stack.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            filter.setMaterialItem(ItemStack.EMPTY);
        }
        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
    }
}