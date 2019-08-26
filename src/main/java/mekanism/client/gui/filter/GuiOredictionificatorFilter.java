package mekanism.client.gui.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.filter.OredictionificatorFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.ItemRegistryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiOredictionificatorFilter extends GuiTextFilterBase<OredictionificatorFilter, TileEntityOredictionificator, OredictionificatorFilterContainer> {

    private Button prevButton;
    private Button nextButton;
    private Button checkboxButton;

    public GuiOredictionificatorFilter(OredictionificatorFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getFilter();
        filter = container.getFilter();
        isNew = container.isNew();
        updateRenderStack();
    }

    @Override
    protected void addButtons() {
        addButton(saveButton = new GuiButtonTranslation(guiLeft + 31, guiTop + 62, 54, 20, "gui.save", onPress -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.hasFilter()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.BACK_BUTTON);
            }
        }));
        addButton(deleteButton = new GuiButtonTranslation(guiLeft + 89, guiTop + 62, 54, 20, "gui.delete", onPress -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 212, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(ClickedTileButton.BACK_BUTTON)));
        addButton(prevButton = new GuiButtonDisableableImage(guiLeft + 31, guiTop + 21, 12, 12, 200, 12, -12, getGuiLocation(),
              onPress -> {
                  if (filter.hasFilter()) {
                      List<Item> matchingItems = filter.getMatchingItems();
                      if (filter.index > 0) {
                          filter.index--;
                      } else {
                          filter.index = matchingItems.size() - 1;
                      }
                      updateRenderStack();
                  }
              }));
        addButton(nextButton = new GuiButtonDisableableImage(guiLeft + 63, guiTop + 21, 12, 12, 188, 12, -12, getGuiLocation(),
              onPress -> {
                  if (filter.hasFilter()) {
                      List<Item> matchingItems = filter.getMatchingItems();
                      if (filter.index < matchingItems.size() - 1) {
                          filter.index++;
                      } else {
                          filter.index = 0;
                      }
                      updateRenderStack();
                  }
              }));
        addButton(checkboxButton = new GuiButtonDisableableImage(guiLeft + 130, guiTop + 48, 12, 12, 176, 12, -12, getGuiLocation(),
              onPress -> setText()));
    }

    @Override
    public void setText() {
        String newFilter = text.getText().toLowerCase();
        String modid = "forge";
        if (newFilter.contains(":")) {
            String[] split = newFilter.split(":");
            modid = split[0];
            newFilter = split[1];
        }
        List<String> possibleFilters = TileEntityOredictionificator.possibleFilters.getOrDefault(modid, Collections.emptyList());
        if (possibleFilters.stream().anyMatch(newFilter::startsWith)) {
            filter.setFilter(new ResourceLocation(modid, newFilter));
            filter.index = 0;
            text.setText("");
            updateRenderStack();
        }
        updateButtons();
    }

    public void updateButtons() {
        saveButton.active = filter.hasFilter();
        deleteButton.active = !isNew;
    }

    @Override
    protected TextFieldWidget createTextField() {
        return new TextFieldWidget(font, guiLeft + 33, guiTop + 48, 96, 12, "");
    }

    @Override
    public void init() {
        super.init();
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(TextComponentUtil.build(Translation.of(isNew ? "gui.new" : "gui.edit"), " " + Translation.of("gui.filter")), 0, xSize, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.index"), ": " + filter.index), 79, 23, 0x404040);
        if (filter.hasFilter()) {
            renderScaledText(filter.getFilterText(), 32, 38, 0x404040, 111);
        }
        renderItem(renderStack, 45, 19);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 31 && xAxis <= 43 && yAxis >= 21 && yAxis <= 33) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.lastItem"), xAxis, yAxis);
        } else if (xAxis >= 63 && xAxis <= 75 && yAxis >= 21 && yAxis <= 33) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.nextItem"), xAxis, yAxis);
        } else if (xAxis >= 33 && xAxis <= 129 && yAxis >= 48 && yAxis <= 60) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.oreDictCompat"), xAxis, yAxis);
        } else if (xAxis >= 45 && xAxis <= 61 && yAxis >= 19 && yAxis <= 35) {
            if (!renderStack.isEmpty()) {
                String name = ItemRegistryUtils.getMod(renderStack);
                String extra = name.equals("null") ? "" : " (" + name + ")";
                displayTooltip(TextComponentUtil.build(renderStack.getDisplayName() + extra), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        //TODO: Figure out what the parameters do
        text.renderButton(0, 0, 0);
        MekanismRenderer.resetColor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        text.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "oredictionificator_filter.png");
    }

    private void updateRenderStack() {
        if (!filter.hasFilter()) {
            renderStack = ItemStack.EMPTY;
            return;
        }
        List<Item> matchingItems = filter.getMatchingItems();
        if (matchingItems.isEmpty()) {
            renderStack = ItemStack.EMPTY;
            return;
        }
        if (matchingItems.size() - 1 >= filter.index) {
            renderStack = new ItemStack(matchingItems.get(filter.index));
        } else {
            renderStack = ItemStack.EMPTY;
        }
    }
}