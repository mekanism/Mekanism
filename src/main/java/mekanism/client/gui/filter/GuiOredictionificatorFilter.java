package mekanism.client.gui.filter;

import java.io.IOException;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiOredictionificatorFilter extends GuiMekanismTile<TileEntityOredictionificator> {

    private OredictionificatorFilter origFilter;
    private OredictionificatorFilter filter = new OredictionificatorFilter();
    private GuiTextField filterText;
    private boolean isNew;
    private ItemStack renderStack = ItemStack.EMPTY;

    public GuiOredictionificatorFilter(EntityPlayer player, TileEntityOredictionificator tile, int index) {
        super(tile, new ContainerFilter(player.inventory, tile));
        origFilter = tileEntity.filters.get(index);
        filter = tileEntity.filters.get(index).clone();
        updateRenderStack();
    }

    public GuiOredictionificatorFilter(EntityPlayer player, TileEntityOredictionificator tile) {
        super(tile, new ContainerFilter(player.inventory, tile));
        isNew = true;
    }

    public void setFilter() {
        String newFilter = filterText.getText();
        boolean has = false;
        for (String s : TileEntityOredictionificator.possibleFilters) {
            if (newFilter.startsWith(s)) {
                has = true;
                break;
            }
        }
        if (has) {
            filter.filter = newFilter;
            filter.index = 0;
            filterText.setText("");
            updateRenderStack();
        }
        updateButtons();
    }

    public void updateButtons() {
        buttonList.get(0).enabled = filter.filter != null && !filter.filter.isEmpty();
        buttonList.get(1).enabled = !isNew;
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        buttonList.clear();
        buttonList.add(new GuiButton(0, guiWidth + 31, guiHeight + 62, 54, 20, LangUtils.localize("gui.save")));
        buttonList.add(new GuiButton(1, guiWidth + 89, guiHeight + 62, 54, 20, LangUtils.localize("gui.delete")));
        if (isNew) {
            buttonList.get(1).enabled = false;
        }
        filterText = new GuiTextField(2, fontRenderer, guiWidth + 33, guiHeight + 48, 96, 12);
        filterText.setMaxStringLength(TileEntityOredictionificator.MAX_LENGTH);
        filterText.setFocused(true);
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String text = (isNew ? LangUtils.localize("gui.new") : LangUtils.localize("gui.edit")) + " " + LangUtils
              .localize("gui.filter");
        fontRenderer.drawString(text, (xSize / 2) - (fontRenderer.getStringWidth(text) / 2), 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.index") + ": " + filter.index, 79, 23, 0x404040);
        if (filter.filter != null) {
            renderScaledText(filter.filter, 32, 38, 0x404040, 111);
        }
        if (!renderStack.isEmpty()) {
            try {
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(renderStack, 45, 19);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            } catch (Exception ignored) {
            }
        }
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33) {
            drawHoveringText(LangUtils.localize("gui.lastItem"), xAxis, yAxis);
        }
        if (xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33) {
            drawHoveringText(LangUtils.localize("gui.nextItem"), xAxis, yAxis);
        }
        if (xAxis >= 33 && xAxis <= 129 && yAxis >= 48 && yAxis <= 60) {
            drawHoveringText(LangUtils.localize("gui.oreDictCompat"), xAxis, yAxis);
        }
        if (xAxis >= 45 && xAxis <= 61 && yAxis >= 19 && yAxis <= 35) {
            if (!renderStack.isEmpty()) {
                String name = ItemRegistryUtils.getMod(renderStack);
                String extra = name.equals("null") ? "" : " (" + name + ")";
                drawHoveringText(renderStack.getDisplayName() + extra, xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
            drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176 + 36, 0, 11, 11);
        } else {
            drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176 + 36, 11, 11, 11);
        }
        if (xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33) {
            drawTexturedModalRect(guiWidth + 31, guiHeight + 21, 176 + 24, 0, 12, 12);
        } else {
            drawTexturedModalRect(guiWidth + 31, guiHeight + 21, 176 + 24, 12, 12, 12);
        }
        if (xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33) {
            drawTexturedModalRect(guiWidth + 63, guiHeight + 21, 176 + 12, 0, 12, 12);
        } else {
            drawTexturedModalRect(guiWidth + 63, guiHeight + 21, 176 + 12, 12, 12, 12);
        }
        if (xAxis >= 130 && xAxis <= 142 && yAxis >= 48 && yAxis <= 60) {
            drawTexturedModalRect(guiWidth + 130, guiHeight + 48, 176, 0, 12, 12);
        } else {
            drawTexturedModalRect(guiWidth + 130, guiHeight + 48, 176, 12, 12, 12);
        }
        filterText.drawTextBox();
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!filterText.isFocused() || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (filterText.isFocused() && i == Keyboard.KEY_RETURN) {
            setFilter();
            return;
        }
        if (Character.isLetter(c) || Character.isDigit(c) || isTextboxKey(c, i)) {
            filterText.textboxKeyTyped(c, i);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            if (!filterText.getText().isEmpty()) {
                setFilter();
            }
            if (filter.filter != null && !filter.filter.isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new NewFilterMessage(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler
                          .sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 0, 52));
            }
        } else if (guibutton.id == 1) {
            Mekanism.packetHandler.sendToServer(new EditFilterMessage(Coord4D.get(tileEntity), true, origFilter, null));
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 0, 52));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        filterText.updateCursorCounter();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        filterText.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = (mouseX - (width - xSize) / 2);
            int yAxis = (mouseY - (height - ySize) / 2);
            if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 0, 52));
            }
            if (xAxis >= 130 && xAxis <= 142 && yAxis >= 48 && yAxis <= 60) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                setFilter();
            }
            if (xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                if (filter.filter != null) {
                    List<ItemStack> ores = OreDictionary.getOres(filter.filter);
                    if (filter.index > 0) {
                        filter.index--;
                    } else {
                        filter.index = ores.size() - 1;
                    }
                    updateRenderStack();
                }
            }
            if (xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                if (filter.filter != null) {
                    List<ItemStack> ores = OreDictionary.getOres(filter.filter);
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

    public void updateRenderStack() {
        if (filter.filter == null || filter.filter.isEmpty()) {
            renderStack = ItemStack.EMPTY;
            return;
        }
        List<ItemStack> stacks = OreDictionary.getOres(filter.filter);
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