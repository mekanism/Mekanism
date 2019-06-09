package mekanism.client.gui.filter;

import java.io.IOException;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismSounds;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public abstract class GuiTextFilter<FILTER extends IFilter, TILE extends TileEntityContainerBlock> extends GuiTextFilterBase<FILTER, TILE> {

    protected List<ItemStack> iterStacks;
    protected int stackSwitch;
    protected int stackIndex;

    protected GuiTextFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    @Override
    protected boolean wasTextboxKey(char c, int i) {
        return TransporterFilter.SPECIAL_CHARS.contains(c) || super.wasTextboxKey(c, i);
    }

    @Override
    protected GuiTextField createTextField(int guiWidth, int guiHeight) {
        return new GuiTextField(2, fontRenderer, guiWidth + 35, guiHeight + 47, 95, 12);
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
        drawTexturedModalRect(guiLeft + 5, guiTop + 5, 176, xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16, 11);
        drawTexturedModalRect(guiLeft + 131, guiTop + 47, 187, xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59, 12);
        if (tileEntity instanceof TileEntityDigitalMiner) {
            drawTexturedModalRect(guiLeft + 148, guiTop + 45, 199, xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59, 14);
            text.drawTextBox();
            drawRect(xAxis, yAxis, guiLeft, guiTop);
        } else if (tileEntity instanceof TileEntityLogisticalSorter) {
            drawTexturedModalRect(guiLeft + 11, guiTop + 64, 199, xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75, 11);
            text.drawTextBox();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        text.mouseClicked(mouseX, mouseY, button);
        int xAxis = mouseX - (width - xSize) / 2;
        int yAxis = mouseY - (height - ySize) / 2;
        if (xAxis >= 131 && xAxis <= 143 && yAxis >= 47 && yAxis <= 59) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            setText();
        }
        if (tileEntity instanceof TileEntityDigitalMiner && filter instanceof MinerFilter) {
            if (button == 0) {
                minerFilterClickCommon(xAxis, yAxis, (MinerFilter) filter);
            }
        } else if (tileEntity instanceof TileEntityLogisticalSorter && filter instanceof TransporterFilter) {
            TransporterFilter tFilter = (TransporterFilter) filter;
            if (button == 0) {
                if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                    sendPacketToServer(isNew ? 4 : 0);
                }
                if (xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                    tFilter.allowDefault = !tFilter.allowDefault;
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0) {
                button = 2;
            }
            if (xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60) {
                SoundHandler.playSound(MekanismSounds.DING);
                if (button == 0) {
                    tFilter.color = TransporterUtils.increment(tFilter.color);
                } else if (button == 1) {
                    tFilter.color = TransporterUtils.decrement(tFilter.color);
                } else if (button == 2) {
                    tFilter.color = null;
                }
            }
        }
    }
}