package mekanism.client.gui.filter;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
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
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        buttonList.clear();
        addButtons(guiWidth, guiHeight);
        if (isNew) {
            buttonList.get(1).enabled = false;
        }
    }

    protected void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis) {
        if (!stack.isEmpty()) {
            //TODO: Is this try catch even needed, some places had it
            try {
                MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                renderHelper.cleanup();
            } catch (Exception ignored) {
            }
        }
    }

    protected void drawRect(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
            MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).disableLighting().disableDepth().colorMaskAlpha();
            int x = guiWidth + 149;
            int y = guiHeight + 19;
            drawRect(x, y, x + 16, y + 16, 0x80FFFFFF);
            renderHelper.cleanup();
        }
    }

    protected void minerFilterClickCommon(int xAxis, int yAxis, MinerFilter filter) {
        if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            sendPacketToServer(isNew ? 5 : 0);
        }
        if (xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            filter.requireStack = !filter.requireStack;
        }
        if (xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35) {
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