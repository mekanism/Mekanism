package mekanism.client.gui;

import java.util.List;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.ColorButton;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.OreDictCache;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiLogisticalSorter extends GuiFilterHolder<TransporterFilter<?>, TileEntityLogisticalSorter, EmptyTileContainer<TileEntityLogisticalSorter>> {

    public GuiLogisticalSorter(EmptyTileContainer<TileEntityLogisticalSorter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));

        addButton(new TranslationButton(this, getGuiLeft() + filterX, getGuiTop() + 136, filterW, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_SELECT_FILTER_TYPE, tile.getPos()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 58, 14, getButtonLocation("single"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(5))),
              getOnHover(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 84, 14, getButtonLocation("round_robin"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(2))),
              getOnHover(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 110, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1))),
              getOnHover(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION)));
        addButton(new ColorButton(this, getGuiLeft() + 13, getGuiTop() + 137, 16, 16, () -> tile.color,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0, InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(),
                    GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0))),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0, 1)))));
    }

    private boolean overUpArrow(double xAxis, double yAxis, int arrowX, int yStart) {
        return xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20;
    }

    private boolean overDownArrow(double xAxis, double yAxis, int arrowX, int yStart) {
        return xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        // Get mouse position relative to gui
        double xAxis = mouseX - getGuiLeft();
        double yAxis = mouseY - getGuiTop();

        if (button == 0) {
            // Check for scrollbar interaction
            if (xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll() + 18 && yAxis <= getScroll() + 18 + 15) {
                if (needsScrollBars()) {
                    dragOffset = (int) (yAxis - (getScroll() + 18));
                    isDragging = true;
                } else {
                    scroll = 0;
                }
            }

            //Check for filter interaction
            HashList<TransporterFilter<?>> filters = tile.getFilters();
            for (int i = 0; i < 4; i++) {
                int index = getFilterIndex() + i;
                TransporterFilter<?> filter = filters.get(index);
                if (filter != null) {
                    int yStart = i * filterH + filterY;
                    if (xAxis >= filterX && xAxis <= filterX + filterW && yAxis >= yStart && yAxis <= yStart + filterH) {
                        //Check for sorting button
                        int arrowX = filterX + filterW - 12;
                        if (index > 0 && overUpArrow(xAxis, yAxis, arrowX, yStart)) {
                            //Process up button click
                            sendDataFromClick(TileNetworkList.withContents(3, index));
                            return true;
                        }
                        if (index < filters.size() - 1 && overDownArrow(xAxis, yAxis, arrowX, yStart)) {
                            //Process down button click
                            sendDataFromClick(TileNetworkList.withContents(4, index));
                            return true;
                        }
                        if (filter instanceof IItemStackFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_ITEMSTACK, tile.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof ITagFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_TAG, tile.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IMaterialFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_MATERIAL, tile.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IModIDFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_MOD_ID, tile.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "logistical_sorter.png");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Get mouse position relative to gui
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();

        HashList<TransporterFilter<?>> filters = tile.getFilters();
        // Write to info display
        drawString(tile.getName(), 43, 6, 0x404040);
        drawString(MekanismLang.FILTERS.translate(), 11, 19, 0x00CD00);
        drawString(MekanismLang.FILTER_COUNT.translate(filters.size()), 11, 28, 0x00CD00);
        drawString(MekanismLang.SORTER_SINGLE_ITEM.translate(), 12, 48, 0x00CD00);
        drawString(OnOff.of(tile.singleItem).getTextComponent(), 27, 60, 0x00CD00);
        drawString(MekanismLang.SORTER_ROUND_ROBIN.translate(), 12, 74, 0x00CD00);
        drawString(OnOff.of(tile.roundRobin).getTextComponent(), 27, 86, 0x00CD00);
        drawString(MekanismLang.SORTER_AUTO_EJECT.translate(), 12, 100, 0x00CD00);
        drawString(OnOff.of(tile.autoEject).getTextComponent(), 27, 112, 0x00CD00);
        drawString(MekanismLang.SORTER_DEFAULT.translate(), 12, 126, 0x00CD00);

        //TODO: Convert filters into "proper" buttons/widgets
        // Draw filters
        for (int i = 0; i < 4; i++) {
            TransporterFilter<?> filter = filters.get(getFilterIndex() + i);
            if (filter != null) {
                int yStart = i * filterH + filterY;
                if (filter instanceof IItemStackFilter) {
                    IItemStackFilter<?> itemFilter = (IItemStackFilter<?>) filter;
                    renderItem(itemFilter.getItemStack(), 59, yStart + 3);
                    drawString(MekanismLang.ITEM_FILTER.translate(), 78, yStart + 2, 0x404040);
                    if (filter.color != null) {
                        drawString(filter.color.getColoredName(), 78, yStart + 11, 0x404040);
                    } else {
                        drawString(MekanismLang.NONE.translate(), 78, yStart + 11, 0x404040);
                    }
                } else if (filter instanceof ITagFilter) {
                    ITagFilter<?> oreFilter = (ITagFilter<?>) filter;
                    if (!oreDictStacks.containsKey(oreFilter)) {
                        updateStackList(oreFilter);
                    }
                    renderItem(oreDictStacks.get(filter).renderStack, 59, yStart + 3);
                    drawString(MekanismLang.TAG_FILTER.translate(), 78, yStart + 2, 0x404040);
                    if (filter.color != null) {
                        drawString(filter.color.getColoredName(), 78, yStart + 11, 0x404040);
                    } else {
                        drawString(MekanismLang.NONE.translate(), 78, yStart + 11, 0x404040);
                    }
                } else if (filter instanceof IMaterialFilter) {
                    IMaterialFilter<?> itemFilter = (IMaterialFilter<?>) filter;
                    renderItem(itemFilter.getMaterialItem(), 59, yStart + 3);
                    drawString(MekanismLang.MATERIAL_FILTER.translate(), 78, yStart + 2, 0x404040);
                    if (filter.color != null) {
                        drawString(filter.color.getColoredName(), 78, yStart + 11, 0x404040);
                    } else {
                        drawString(MekanismLang.NONE.translate(), 78, yStart + 11, 0x404040);
                    }
                } else if (filter instanceof IModIDFilter) {
                    IModIDFilter<?> modFilter = (IModIDFilter<?>) filter;
                    if (!modIDStacks.containsKey(modFilter)) {
                        updateStackList(modFilter);
                    }
                    renderItem(modIDStacks.get(filter).renderStack, 59, yStart + 3);
                    drawString(MekanismLang.MODID_FILTER.translate(), 78, yStart + 2, 0x404040);
                    if (filter.color != null) {
                        drawString(filter.color.getColoredName(), 78, yStart + 11, 0x404040);
                    } else {
                        drawString(MekanismLang.NONE.translate(), 78, yStart + 11, 0x404040);
                    }
                }

                // Draw hover text for sorting buttons
                int arrowX = filterX + filterW - 12;

                if (getFilterIndex() + i > 0 && overUpArrow(xAxis, yAxis, arrowX, yStart)) {
                    displayTooltip(MekanismLang.MOVE_UP.translate(), xAxis, yAxis);
                }
                if (getFilterIndex() + i < filters.size() - 1 && overDownArrow(xAxis, yAxis, arrowX, yStart)) {
                    displayTooltip(MekanismLang.MOVE_DOWN.translate(), xAxis, yAxis);
                }
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected List<ItemStack> getTagStacks(String tagName) {
        return OreDictCache.getItemTagStacks(tagName);
    }
}