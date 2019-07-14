package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.button.GuiButtonImageMek;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiMItemStackFilter extends GuiItemStackFilter<MItemStackFilter, TileEntityDigitalMiner> {

    private GuiButtonImageMek fuzzyButton;

    public GuiMItemStackFilter(EntityPlayer player, TileEntityDigitalMiner tile, int index) {
        super(player, tile);
        origFilter = (MItemStackFilter) tileEntity.filters.get(index);
        filter = ((MItemStackFilter) tileEntity.filters.get(index)).clone();
    }

    public GuiMItemStackFilter(EntityPlayer player, TileEntityDigitalMiner tile) {
        super(player, tile);
        isNew = true;
        filter = new MItemStackFilter();
    }

    @Override
    protected void addButtons() {
        buttonList.add(saveButton = new GuiButton(0, guiLeft + 27, guiTop + 62, 60, 20, LangUtils.localize("gui.save")));
        buttonList.add(deleteButton = new GuiButton(1, guiLeft + 89, guiTop + 62, 60, 20, LangUtils.localize("gui.delete")));
        buttonList.add(backButton = new GuiButtonImageMek(2, guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation()));
        buttonList.add(replaceButton = new GuiButtonImageMek(3, guiLeft + 148, guiTop + 45, 14, 14, 199, 14, -14, getGuiLocation()));
        buttonList.add(fuzzyButton = new GuiButtonImageMek(4, guiLeft + 15, guiTop + 45, 14, 14, 213, 14, -14, getGuiLocation()));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == saveButton.id) {
            if (!filter.getItemStack().isEmpty()) {
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
        } else if (guibutton.id == fuzzyButton.id) {
            filter.fuzzy = !filter.fuzzy;
        } else {
            actionPerformedMinerCommon(guibutton, filter);
        }
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getItemStack().isEmpty()) {
            renderScaledText(filter.getItemStack().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        renderItem(filter.getItemStack(), 12, 19);
        renderItem(filter.replaceStack, 149, 19);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (replaceButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils.transYesNo(filter.requireStack), xAxis, yAxis);
        } else if (fuzzyButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.fuzzyMode") + ": " + LangUtils.transYesNo(filter.fuzzy), xAxis, yAxis);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (overTypeInput(xAxis, yAxis)) {
                ItemStack stack = mc.player.inventory.getItemStack();
                if (!stack.isEmpty() && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    if (stack.getItem() instanceof ItemBlock) {
                        if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                            filter.setItemStack(stack.copy());
                            filter.getItemStack().setCount(1);
                        }
                    }
                } else if (stack.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    filter.setItemStack(ItemStack.EMPTY);
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            } else {
                minerFilterClickCommon(xAxis, yAxis, filter);
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiMItemStackFilter.png");
    }
}