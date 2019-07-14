package mekanism.client.gui.filter;

import java.io.IOException;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.client.gui.button.GuiButtonImageMek;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiTextFilter<FILTER extends IFilter, TILE extends TileEntityContainerBlock> extends GuiTextFilterBase<FILTER, TILE> {

    protected List<ItemStack> iterStacks;
    protected int stackSwitch;
    protected int stackIndex;
    protected GuiButtonImageMek checkboxButton;

    protected GuiTextFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    @Override
    protected boolean wasTextboxKey(char c, int i) {
        return TransporterFilter.SPECIAL_CHARS.contains(c) || super.wasTextboxKey(c, i);
    }

    @Override
    protected GuiTextField createTextField() {
        return new GuiTextField(2, fontRenderer, guiLeft + 35, guiTop + 47, 95, 12);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (ticker > 0) {
            ticker--;
        } else {
            status = EnumColor.DARK_GREEN + LangUtils.localize("gui.allOK");
        }
        if (stackSwitch > 0) {
            stackSwitch--;
        }
        if (stackSwitch == 0 && iterStacks != null && iterStacks.size() > 0) {
            stackSwitch = 20;
            if (stackIndex == -1 || stackIndex == iterStacks.size() - 1) {
                stackIndex = 0;
            } else if (stackIndex < iterStacks.size() - 1) {
                stackIndex++;
            }
            renderStack = iterStacks.get(stackIndex);
        } else if (iterStacks != null && iterStacks.size() == 0) {
            renderStack = ItemStack.EMPTY;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        text.drawTextBox();
        if (tileEntity instanceof TileEntityDigitalMiner) {
            if (overReplaceOutput(xAxis, yAxis)) {
                drawRect(guiLeft + 149, guiTop + 19, guiLeft + 165, guiTop + 35, 0x80FFFFFF);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == checkboxButton.id) {
            setText();
        } else if (tileEntity instanceof TileEntityDigitalMiner && filter instanceof MinerFilter) {
            actionPerformedMinerCommon(guibutton, (MinerFilter) filter);
        } else if (tileEntity instanceof TileEntityLogisticalSorter && filter instanceof TransporterFilter) {
            actionPerformedTransporter(guibutton, (TransporterFilter) filter);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        text.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && tileEntity instanceof TileEntityDigitalMiner && filter instanceof MinerFilter) {
            minerFilterClickCommon(mouseX - guiLeft, mouseY - guiTop, (MinerFilter) filter);
        } else if (tileEntity instanceof TileEntityLogisticalSorter && filter instanceof TransporterFilter) {
            transporterMouseClicked(button, (TransporterFilter) filter);
        }
    }
}