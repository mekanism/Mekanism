package mekanism.client.gui.filter;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismSounds;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
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
            buttonList.get(1).enabled = false;
        }
    }

    protected void drawRect(int xAxis, int yAxis, int xMin, int xMax, int yMin, int yMax) {
        if (xAxis >= xMin && xAxis <= xMax && yAxis >= yMin && yAxis <= yMax) {
            int x = guiLeft + xMin;
            int y = guiTop + yMin;
            drawRect(x, y, x + 16, y + 16, 0x80FFFFFF);
        }
    }

    protected void transporterMouseClicked(int xAxis, int yAxis, int button, TransporterFilter filter) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0) {
            button = 2;
        }
        if (xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60) {
            SoundHandler.playSound(MekanismSounds.DING);
            if (button == 0) {
                filter.color = TransporterUtils.increment(filter.color);
            } else if (button == 1) {
                filter.color = TransporterUtils.decrement(filter.color);
            } else if (button == 2) {
                filter.color = null;
            }
        }
    }

    protected void drawMinerForegroundLayer(int mouseX, int mouseY, ItemStack stack) {
        if (filter instanceof MinerFilter) {
            MinerFilter mFilter = (MinerFilter) filter;
            renderItem(stack, 12, 19);
            renderItem(mFilter.replaceStack, 149, 19);
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59) {
                drawHoveringText(LangUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils.transYesNo(mFilter.requireStack), xAxis, yAxis);
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
        if (xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75) {
            drawHoveringText(LangUtils.localize("gui.allowDefault"), xAxis, yAxis);
        } else if (xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60) {
            if (filter.color != null) {
                drawHoveringText(filter.color.getColoredName(), xAxis, yAxis);
            } else {
                drawHoveringText(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
    }

    protected void minerFilterClickCommon(int xAxis, int yAxis, MinerFilter filter) {
        if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            sendPacketToServer(isNew ? 5 : 0);
        } else if (xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            filter.requireStack = !filter.requireStack;
        } else if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
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