package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.inventory.container.tile.filter.DMItemStackFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public class GuiMItemStackFilter extends GuiItemStackFilter<MItemStackFilter, TileEntityDigitalMiner, DMItemStackFilterContainer> {

    public GuiMItemStackFilter(DMItemStackFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 111, 43));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 18).setRenderHover(true));
        addButton(new GuiSlot(SlotType.NORMAL, this, 148, 18).setRenderHover(true));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!filter.getItemStack().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tile), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
            } else {
                status = MekanismLang.ITEM_FILTER_NO_ITEM.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tile), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(isNew ? ClickedTileButton.DM_SELECT_FILTER_TYPE : ClickedTileButton.DIGITAL_MINER_CONFIG)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 148, getGuiTop() + 45, 14, 16, getButtonLocation("exclamation"),
              () -> filter.requireStack = !filter.requireStack, getOnHoverReplace(filter)));
        //TODO: Move fuzzy mode to the logistical sorter
        /*addButton(new MekanismImageButton(this, getGuiLeft() + 15, getGuiTop() + 45, 14, getButtonLocation("fuzzy"),
              () -> filter.fuzzy = !filter.fuzzy, getOnHover(() -> MekanismLang.MINER_FUZZY_MODE.translate(YesNo.of(filter.fuzzy)))));*/
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getItemStack().isEmpty()) {
            renderScaledText(filter.getItemStack().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        renderItem(filter.getItemStack(), 12, 19);
        renderItem(filter.replaceStack, 149, 19);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - getGuiLeft();
            double yAxis = mouseY - getGuiTop();
            if (overTypeInput(xAxis, yAxis)) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && !hasShiftDown()) {
                    if (stack.getItem() instanceof BlockItem) {
                        if (Block.getBlockFromItem(stack.getItem()) != Blocks.BEDROCK) {
                            filter.setItemStack(stack.copy());
                            filter.getItemStack().setCount(1);
                        }
                    }
                } else if (stack.isEmpty() && hasShiftDown()) {
                    filter.setItemStack(ItemStack.EMPTY);
                }
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            } else {
                minerFilterClickCommon(xAxis, yAxis, filter);
            }
        }
        return true;
    }
}