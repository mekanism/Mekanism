package mekanism.client.gui.filter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiTextFilter<FILTER extends IFilter, TILE extends TileEntityMekanism, CONTAINER extends FilterContainer<TILE, FILTER>> extends GuiTextFilterBase<FILTER, TILE, CONTAINER> {

    protected List<ItemStack> iterStacks;
    protected int stackSwitch;
    protected int stackIndex;
    protected Button checkboxButton;

    protected GuiTextFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected boolean wasTextboxKey(char c, int i) {
        return TransporterFilter.SPECIAL_CHARS.contains(c) || super.wasTextboxKey(c, i);
    }

    @Override
    protected TextFieldWidget createTextField() {
        return new TextFieldWidget(font, guiLeft + 35, guiTop + 47, 95, 12, "");
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = TextComponentUtil.build(EnumColor.DARK_GREEN, Translation.of("gui.allOK"));
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
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        //TODO: Draw Text box
        //text.drawTextBox();
        if (tileEntity instanceof TileEntityDigitalMiner) {
            if (overReplaceOutput(xAxis, yAxis)) {
                fill(guiLeft + 149, guiTop + 19, guiLeft + 165, guiTop + 35, 0x80FFFFFF);
            }
        }
        //This is needed here and not just inside the if statements due to the text box drawing
        MekanismRenderer.resetColor();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (tileEntity instanceof TileEntityDigitalMiner) {
            drawMinerForegroundLayer(mouseX, mouseY, renderStack);
        } else if (tileEntity instanceof TileEntityLogisticalSorter) {
            drawTransporterForegroundLayer(mouseX, mouseY, renderStack);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        text.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && tileEntity instanceof TileEntityDigitalMiner && filter instanceof MinerFilter) {
            minerFilterClickCommon(mouseX - guiLeft, mouseY - guiTop, (MinerFilter) filter);
        } else if (tileEntity instanceof TileEntityLogisticalSorter && filter instanceof TransporterFilter) {
            transporterMouseClicked(mouseX, mouseY, button, (TransporterFilter) filter);
        }
    }
}