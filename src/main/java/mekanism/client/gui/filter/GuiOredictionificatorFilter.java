package mekanism.client.gui.filter;

import java.io.IOException;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.ItemRegistryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@SideOnly(Side.CLIENT)
public class GuiOredictionificatorFilter extends GuiTextFilterBase<OredictionificatorFilter, TileEntityOredictionificator> {

    public GuiOredictionificatorFilter(EntityPlayer player, TileEntityOredictionificator tile, int index) {
        super(tile, new ContainerFilter(player.inventory, tile));
        origFilter = tileEntity.filters.get(index);
        filter = tileEntity.filters.get(index).clone();
        updateRenderStack();
    }

    public GuiOredictionificatorFilter(EntityPlayer player, TileEntityOredictionificator tile) {
        super(tile, new ContainerFilter(player.inventory, tile));
        filter = new OredictionificatorFilter();
        isNew = true;
    }

    @Override
    protected void addButtons() {
        buttonList.add(new GuiButton(0, guiLeft + 31, guiTop + 62, 54, 20, LangUtils.localize("gui.save")));
        buttonList.add(new GuiButton(1, guiLeft + 89, guiTop + 62, 54, 20, LangUtils.localize("gui.delete")));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 0, guiID));
    }

    @Override
    public void setText() {
        String newFilter = text.getText();
        if (TileEntityOredictionificator.possibleFilters.stream().anyMatch(newFilter::startsWith)) {
            filter.filter = newFilter;
            filter.index = 0;
            text.setText("");
            updateRenderStack();
        }
        updateButtons();
    }

    public void updateButtons() {
        buttonList.get(0).enabled = filter.filter != null && !filter.filter.isEmpty();
        buttonList.get(1).enabled = !isNew;
    }

    @Override
    protected GuiTextField createTextField() {
        return new GuiTextField(2, fontRenderer, guiLeft + 33, guiTop + 48, 96, 12);
    }

    @Override
    public void initGui() {
        super.initGui();
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String text = (isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " + LangUtils.localize("gui.filter");
        fontRenderer.drawString(text, (xSize / 2) - (fontRenderer.getStringWidth(text) / 2), 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.index") + ": " + filter.index, 79, 23, 0x404040);
        if (filter.filter != null) {
            renderScaledText(filter.filter, 32, 38, 0x404040, 111);
        }
        renderItem(renderStack, 45, 19);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33) {
            drawHoveringText(LangUtils.localize("gui.lastItem"), xAxis, yAxis);
        } else if (xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33) {
            drawHoveringText(LangUtils.localize("gui.nextItem"), xAxis, yAxis);
        } else if (xAxis >= 33 && xAxis <= 129 && yAxis >= 48 && yAxis <= 60) {
            drawHoveringText(LangUtils.localize("gui.oreDictCompat"), xAxis, yAxis);
        } else if (xAxis >= 45 && xAxis <= 61 && yAxis >= 19 && yAxis <= 35) {
            if (!renderStack.isEmpty()) {
                String name = ItemRegistryUtils.getMod(renderStack);
                String extra = name.equals("null") ? "" : " (" + name + ")";
                drawHoveringText(renderStack.getDisplayName() + extra, xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        drawTexturedModalRect(guiLeft + 5, guiTop + 5, 212, xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16, 11);
        drawTexturedModalRect(guiLeft + 31, guiTop + 21, 200, xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33, 12);
        drawTexturedModalRect(guiLeft + 63, guiTop + 21, 188, xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33, 12);
        drawTexturedModalRect(guiLeft + 130, guiTop + 48, 176, xAxis >= 130 && xAxis <= 142 && yAxis >= 48 && yAxis <= 60, 12);
        text.drawTextBox();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.filter != null && !filter.filter.isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(52);
            }
        } else if (guibutton.id == 1) {
            Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(52);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        text.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                sendPacketToServer(52);
            } else if (xAxis >= 130 && xAxis <= 142 && yAxis >= 48 && yAxis <= 60) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                setText();
            } else if (xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                if (filter.filter != null) {
                    List<ItemStack> ores = OreDictionary.getOres(filter.filter, false);
                    if (filter.index > 0) {
                        filter.index--;
                    } else {
                        filter.index = ores.size() - 1;
                    }
                    updateRenderStack();
                }
            } else if (xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                if (filter.filter != null) {
                    List<ItemStack> ores = OreDictionary.getOres(filter.filter, false);
                    if (filter.index < ores.size() - 1) {
                        filter.index++;
                    } else {
                        filter.index = 0;
                    }
                    updateRenderStack();
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiOredictionificatorFilter.png");
    }

    private void updateRenderStack() {
        if (filter.filter == null || filter.filter.isEmpty()) {
            renderStack = ItemStack.EMPTY;
            return;
        }
        List<ItemStack> stacks = OreDictionary.getOres(filter.filter, false);
        if (stacks.isEmpty()) {
            renderStack = ItemStack.EMPTY;
            return;
        }
        if (stacks.size() - 1 >= filter.index) {
            renderStack = stacks.get(filter.index).copy();
        } else {
            renderStack = ItemStack.EMPTY;
        }
    }
}