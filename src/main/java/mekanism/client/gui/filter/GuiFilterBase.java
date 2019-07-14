package mekanism.client.gui.filter;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.gui.button.GuiButtonImageMek;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.client.sound.SoundHandler;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public abstract class GuiFilterBase<FILTER extends IFilter, TILE extends TileEntityContainerBlock> extends GuiFilter<TILE> {

    protected String status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
    protected FILTER origFilter;
    protected FILTER filter;
    protected boolean isNew;
    protected int ticker;

    protected GuiButton saveButton;
    protected GuiButton deleteButton;
    protected GuiButtonImageMek backButton;
    protected GuiButtonImageMek replaceButton;
    protected GuiButtonImageMek defaultButton;
    protected GuiColorButton colorButton;

    protected GuiFilterBase(TILE tile, Container container) {
        super(tile, container);
    }

    protected GuiFilterBase(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (isNew) {
            deleteButton.enabled = false;
        }
    }

    protected void transporterMouseClicked(int button, TransporterFilter filter) {
        if (button == 1 && colorButton.isMouseOver()) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            filter.color = TransporterUtils.decrement(filter.color);
        }
    }

    protected void drawMinerForegroundLayer(int mouseX, int mouseY, ItemStack stack) {
        if (filter instanceof MinerFilter) {
            MinerFilter mFilter = (MinerFilter) filter;
            renderItem(stack, 12, 19);
            renderItem(mFilter.replaceStack, 149, 19);
            if (replaceButton.isMouseOver()) {
                drawHoveringText(LangUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils.transYesNo(mFilter.requireStack), mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    protected void drawTransporterForegroundLayer(int mouseX, int mouseY, @Nonnull ItemStack stack) {
        if (filter instanceof TransporterFilter) {
            TransporterFilter tFilter = (TransporterFilter) filter;
            fontRenderer.drawString(LangUtils.transOnOff(tFilter.allowDefault), 24, 66, 0x404040);
            renderItem(stack, 12, 19);
            drawColorIcon(12, 44, tFilter.color, 1);
            drawTransporterForegroundText(mouseX - guiLeft, mouseY - guiTop, tFilter);
        }
    }

    protected void drawTransporterForegroundText(int xAxis, int yAxis, TransporterFilter filter) {
        if (defaultButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.allowDefault"), xAxis, yAxis);
        } else if (colorButton.isMouseOver()) {
            if (filter.color != null) {
                drawHoveringText(filter.color.getColoredName(), xAxis, yAxis);
            } else {
                drawHoveringText(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
    }

    protected void actionPerformedMinerCommon(GuiButton guibutton, MinerFilter filter) {
        if (guibutton.id == backButton.id) {
            sendPacketToServer(isNew ? 5 : 0);
        } else if (guibutton.id == replaceButton.id) {
            filter.requireStack = !filter.requireStack;
        }
    }

    protected void actionPerformedTransporter(GuiButton guibutton, TransporterFilter filter) {
        if (guibutton.id == backButton.id) {
            sendPacketToServer(isNew ? 4 : 0);
        } else if (guibutton.id == defaultButton.id) {
            filter.allowDefault = !filter.allowDefault;
        } else if (guibutton.id == colorButton.id) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                filter.color = null;
            } else {
                filter.color = TransporterUtils.increment(filter.color);
            }
        }
    }

    protected boolean overReplaceOutput(int xAxis, int yAxis) {
        return xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35;
    }

    protected void minerFilterClickCommon(int xAxis, int yAxis, MinerFilter filter) {
        if (overReplaceOutput(xAxis, yAxis)) {
            boolean doNull = false;
            ItemStack stack = mc.player.inventory.getItemStack();
            ItemStack toUse = ItemStack.EMPTY;
            if (!stack.isEmpty() && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                if (stack.getItem() instanceof ItemBlock) {
                    if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                        toUse = stack.copy();
                        toUse.setCount(1);
                    }
                }
            } else if (stack.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                doNull = true;
            }
            if (!toUse.isEmpty() || doNull) {
                filter.replaceStack = toUse;
            }
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}