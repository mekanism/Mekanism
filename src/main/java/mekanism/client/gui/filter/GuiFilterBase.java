package mekanism.client.gui.filter;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilterBase<FILTER extends IFilter, TILE extends TileEntityMekanism> extends GuiFilter<TILE> {

    protected String status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
    protected FILTER origFilter;
    protected FILTER filter;
    protected boolean isNew;
    protected int ticker;

    protected Button saveButton;
    protected Button deleteButton;
    protected Button backButton;
    protected Button replaceButton;
    protected Button defaultButton;
    protected Button colorButton;

    protected GuiFilterBase(TILE tile, Container container) {
        super(tile, container);
    }

    protected GuiFilterBase(PlayerEntity player, TILE tile) {
        super(player, tile);
    }

    @Override
    public void init() {
        super.init();
        if (isNew) {
            deleteButton.active = false;
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
                displayTooltip(LangUtils.localize("gui.digitalMiner.requireReplace") + ": " + LangUtils.transYesNo(mFilter.requireStack), mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    protected void drawTransporterForegroundLayer(int mouseX, int mouseY, @Nonnull ItemStack stack) {
        if (filter instanceof TransporterFilter) {
            TransporterFilter tFilter = (TransporterFilter) filter;
            font.drawString(LangUtils.transOnOff(tFilter.allowDefault), 24, 66, 0x404040);
            renderItem(stack, 12, 19);
            drawTransporterForegroundText(mouseX - guiLeft, mouseY - guiTop, tFilter);
        }
    }

    protected void drawTransporterForegroundText(int xAxis, int yAxis, TransporterFilter filter) {
        if (defaultButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.allowDefault"), xAxis, yAxis);
        } else if (colorButton.isMouseOver()) {
            if (filter.color != null) {
                displayTooltip(filter.color.getColoredName(), xAxis, yAxis);
            } else {
                displayTooltip(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
    }

    protected void actionPerformedMinerCommon(Button guibutton, MinerFilter filter) {
        if (guibutton.id == backButton.id) {
            sendPacketToServer(isNew ? 5 : 0);
        } else if (guibutton.id == replaceButton.id) {
            filter.requireStack = !filter.requireStack;
        }
    }

    protected void actionPerformedTransporter(Button guibutton, TransporterFilter filter) {
        if (guibutton.id == backButton.id) {
            sendPacketToServer(isNew ? 4 : 0);
        } else if (guibutton.id == defaultButton.id) {
            filter.allowDefault = !filter.allowDefault;
        } else if (guibutton.id == colorButton.id) {
            if (InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
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
            ItemStack stack = minecraft.player.inventory.getItemStack();
            ItemStack toUse = ItemStack.EMPTY;
            if (!stack.isEmpty() && !InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                if (stack.getItem() instanceof BlockItem) {
                    if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                        toUse = stack.copy();
                        toUse.setCount(1);
                    }
                }
            } else if (stack.isEmpty() && InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                doNull = true;
            }
            if (!toUse.isEmpty() || doNull) {
                filter.replaceStack = toUse;
            }
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}