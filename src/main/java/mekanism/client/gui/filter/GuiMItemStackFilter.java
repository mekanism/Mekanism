package mekanism.client.gui.filter;

import java.io.IOException;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiMItemStackFilter extends GuiItemStackFilter<MItemStackFilter, TileEntityDigitalMiner> {

    private Button fuzzyButton;

    public GuiMItemStackFilter(PlayerEntity player, TileEntityDigitalMiner tile, int index) {
        super(player, tile);
        origFilter = (MItemStackFilter) tileEntity.filters.get(index);
        filter = ((MItemStackFilter) tileEntity.filters.get(index)).clone();
    }

    public GuiMItemStackFilter(PlayerEntity player, TileEntityDigitalMiner tile) {
        super(player, tile);
        isNew = true;
        filter = new MItemStackFilter();
    }

    @Override
    protected void addButtons() {
        buttonList.add(saveButton = new Button(0, guiLeft + 27, guiTop + 62, 60, 20, LangUtils.localize("gui.save")));
        buttonList.add(deleteButton = new Button(1, guiLeft + 89, guiTop + 62, 60, 20, LangUtils.localize("gui.delete")));
        buttonList.add(backButton = new GuiButtonDisableableImage(2, guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation()));
        buttonList.add(replaceButton = new GuiButtonDisableableImage(3, guiLeft + 148, guiTop + 45, 14, 14, 199, 14, -14, getGuiLocation()));
        buttonList.add(fuzzyButton = new GuiButtonDisableableImage(4, guiLeft + 15, guiTop + 45, 14, 14, 213, 14, -14, getGuiLocation()));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new PacketDigitalMinerGui(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }

    @Override
    protected void actionPerformed(Button guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == saveButton.id) {
            if (!filter.getItemStack().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
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
            displayTooltip(LangUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils.transYesNo(filter.requireStack), xAxis, yAxis);
        } else if (fuzzyButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.digitalMiner.fuzzyMode") + ": " + LangUtils.transYesNo(filter.fuzzy), xAxis, yAxis);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (overTypeInput(xAxis, yAxis)) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && !InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    if (stack.getItem() instanceof BlockItem) {
                        if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                            filter.setItemStack(stack.copy());
                            filter.getItemStack().setCount(1);
                        }
                    }
                } else if (stack.isEmpty() && InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
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