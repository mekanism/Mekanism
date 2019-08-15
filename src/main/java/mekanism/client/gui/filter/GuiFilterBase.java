package mekanism.client.gui.filter;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilterBase<FILTER extends IFilter, TILE extends TileEntityMekanism> extends GuiFilter<TILE> {

    protected ITextComponent status = TextComponentUtil.build(EnumColor.DARK_GREEN, Translation.of("gui.allOK"));
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

    protected void transporterMouseClicked(double mouseX, double mouseY, int button, TransporterFilter filter) {
        if (button == 1 && colorButton.isMouseOver(mouseX, mouseY)) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            filter.color = TransporterUtils.decrement(filter.color);
        }
    }

    protected void drawMinerForegroundLayer(int mouseX, int mouseY, ItemStack stack) {
        if (filter instanceof MinerFilter) {
            MinerFilter mFilter = (MinerFilter) filter;
            renderItem(stack, 12, 19);
            renderItem(mFilter.replaceStack, 149, 19);
            if (replaceButton.isMouseOver(mouseX, mouseY)) {
                displayTooltip(TextComponentUtil.build(Translation.of("mekanism.gui.digitalMiner.requireReplace"), ": ",
                      YesNo.of(mFilter.requireStack)), mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    protected void drawTransporterForegroundLayer(int mouseX, int mouseY, @Nonnull ItemStack stack) {
        if (filter instanceof TransporterFilter) {
            TransporterFilter tFilter = (TransporterFilter) filter;
            drawString(OnOff.of(tFilter.allowDefault).getTextComponent(), 24, 66, 0x404040);
            renderItem(stack, 12, 19);
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (defaultButton.isMouseOver(mouseX, mouseY)) {
                displayTooltip(TextComponentUtil.translate("mekanism.gui.allowDefault"), xAxis, yAxis);
            } else if (colorButton.isMouseOver(mouseX, mouseY)) {
                if (tFilter.color != null) {
                    displayTooltip(tFilter.color.getColoredName(), xAxis, yAxis);
                } else {
                    displayTooltip(TextComponentUtil.translate("mekanism.gui.none"), xAxis, yAxis);
                }
            }
        }
    }

    protected boolean overReplaceOutput(double xAxis, double yAxis) {
        return xAxis >= 149 && xAxis <= 165 && yAxis >= 19 && yAxis <= 35;
    }

    protected void minerFilterClickCommon(double xAxis, double yAxis, MinerFilter filter) {
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