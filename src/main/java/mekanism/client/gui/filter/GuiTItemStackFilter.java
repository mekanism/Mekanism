package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.ColorButton;
import mekanism.client.gui.button.DisableableImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.inventory.container.tile.filter.LSItemStackFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTItemStackFilter extends GuiItemStackFilter<TItemStackFilter, TileEntityLogisticalSorter, LSItemStackFilterContainer> {

    private TextFieldWidget minField;
    private TextFieldWidget maxField;

    public GuiTItemStackFilter(LSItemStackFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(saveButton = new TranslationButton(guiLeft + 47, guiTop + 62, 60, 20, "gui.mekanism.save", onPress -> {
            if (!filter.getItemStack().isEmpty() && !minField.getText().isEmpty() && !maxField.getText().isEmpty()) {
                int min = Integer.parseInt(minField.getText());
                int max = Integer.parseInt(maxField.getText());
                if (max >= min && max <= 64) {
                    filter.min = Integer.parseInt(minField.getText());
                    filter.max = Integer.parseInt(maxField.getText());
                    if (isNew) {
                        Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                    } else {
                        Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                    }
                    sendPacketToServer(ClickedTileButton.BACK_BUTTON);
                } else if (min > max) {
                    //TODO: Lang Keys
                    status = TextComponentUtil.build(EnumColor.DARK_RED, "Max<min");
                    ticker = 20;
                } else { //if(max > 64 || min > 64)
                    status = TextComponentUtil.build(EnumColor.DARK_RED, "Max>64");
                    ticker = 20;
                }
            } else if (filter.getItemStack().isEmpty()) {
                status = TextComponentUtil.build(EnumColor.DARK_RED, "No item");
                ticker = 20;
            } else if (minField.getText().isEmpty() || maxField.getText().isEmpty()) {
                status = TextComponentUtil.build(EnumColor.DARK_RED, "Max/min");
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(guiLeft + 109, guiTop + 62, 60, 20, "gui.mekanism.delete", onPress -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(new DisableableImageButton(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(isNew ? ClickedTileButton.LS_SELECT_FILTER_TYPE : ClickedTileButton.BACK_BUTTON)));
        addButton(new DisableableImageButton(guiLeft + 11, guiTop + 64, 11, 11, 198, 11, -11, getGuiLocation(),
              onPress -> filter.allowDefault = !filter.allowDefault, getOnHover("gui.mekanism.allowDefault")));
        addButton(new ColorButton(guiLeft + 12, guiTop + 44, 16, 16, this, () -> filter.color,
              onPress -> filter.color = InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? null : TransporterUtils.increment(filter.color),
              onRightClick -> filter.color = TransporterUtils.decrement(filter.color)));
        addButton(new DisableableImageButton(guiLeft + 128, guiTop + 44, 11, 11, 187, 11, -11, getGuiLocation(),
              onPress -> filter.sizeMode = !filter.sizeMode,
              (onHover, xAxis, yAxis) -> {
                  if (tileEntity.singleItem && filter.sizeMode) {
                      displayTooltip(TextComponentUtil.build(Translation.of("gui.mekanism.sizeMode"), " - ", Translation.of("gui.mekanism.sizeModeConflict")), xAxis, yAxis);
                  } else {
                      displayTooltip(TextComponentUtil.translate("gui.mekanism.sizeMode"), xAxis, yAxis);
                  }
              }));
    }

    @Override
    public void init() {
        super.init();
        addButton(minField = new TextFieldWidget(font, guiLeft + 149, guiTop + 19, 20, 11, ""));
        minField.setMaxStringLength(2);
        minField.setText("" + filter.min);
        addButton(maxField = new TextFieldWidget(font, guiLeft + 149, guiTop + 31, 20, 11, ""));
        maxField.setMaxStringLength(2);
        maxField.setText("" + filter.max);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if ((!minField.isFocused() && !maxField.isFocused()) || i == GLFW.GLFW_KEY_ESCAPE) {
            return super.charTyped(c, i);
        }
        if (Character.isDigit(c) || isTextboxKey(c, i)) {
            return minField.charTyped(c, i) || maxField.charTyped(c, i);
        }
        return false;
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.itemFilter.min"), ":"), 128, 20, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.itemFilter.max"), ":"), 128, 32, 0x404040);
        if (tileEntity.singleItem && filter.sizeMode) {
            drawString(TextComponentUtil.build(EnumColor.RED, OnOff.of(filter.sizeMode), "!"), 141, 46, 0x404040);
        } else {
            drawString(OnOff.of(filter.sizeMode).getTextComponent(), 141, 46, 0x404040);
        }
        drawTransporterForegroundLayer(filter.getItemStack());
        if (!filter.getItemStack().isEmpty()) {
            renderScaledText(filter.getItemStack().getDisplayName(), 35, 41, 0x00CD00, 89);
        }
    }

    @Override
    public void tick() {
        super.tick();
        minField.tick();
        maxField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && overTypeInput(mouseX - guiLeft, mouseY - guiTop)) {
            ItemStack stack = minecraft.player.inventory.getItemStack();
            if (!stack.isEmpty() && !InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                filter.setItemStack(stack.copy());
                filter.getItemStack().setCount(1);
            } else if (stack.isEmpty() && InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                filter.setItemStack(ItemStack.EMPTY);
            }
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "sorter_itemstack_filter.png");
    }
}