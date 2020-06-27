package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.PacketGuiInteract.GuiInteractionItem;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiQIORedstoneAdapter extends GuiMekanismTile<TileEntityQIORedstoneAdapter, MekanismTileContainer<TileEntityQIORedstoneAdapter>> {

    private GuiTextField text;

    public GuiQIORedstoneAdapter(MekanismTileContainer<TileEntityQIORedstoneAdapter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 16;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiQIOFrequencyTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiSlot(SlotType.NORMAL, this, 7, 30).setRenderHover(true));
        func_230480_a_(new GuiInnerScreen(this, 7, 16, xSize - 15, 12, () -> {
            List<ITextComponent> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq != null) {
                list.add(MekanismLang.FREQUENCY.translate(freq.getKey()));
            } else {
                list.add(MekanismLang.NO_FREQUENCY.translate());
            }
            return list;
        }).tooltip(() -> {
            List<ITextComponent> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq != null) {
                list.add(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      QIOFrequency.formatItemCount(freq.getTotalItemCount()), QIOFrequency.formatItemCount(freq.getTotalItemCountCapacity())));
                list.add(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      QIOFrequency.formatItemCount(freq.getTotalItemTypes(true)), QIOFrequency.formatItemCount(freq.getTotalItemTypeCapacity())));
            }
            return list;
        }));
        func_230480_a_(new GuiInnerScreen(this, 27, 30, xSize - 27 - 8, 54, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(!tile.getItemType().isEmpty() ? tile.getItemType().getStack().getDisplayName() : MekanismLang.QIO_ITEM_TYPE_UNDEFINED.translate());
            list.add(MekanismLang.QIO_TRIGGER_COUNT.translate(QIOFrequency.formatItemCount(tile.getCount())));
            if (!tile.getItemType().isEmpty() && tile.getQIOFrequency() != null) {
                list.add(MekanismLang.QIO_STORED_COUNT.translate(QIOFrequency.formatItemCount(tile.getStoredCount())));
            }
            return list;
        }).clearFormat());
        func_230480_a_(text = new GuiTextField(this, 29, 70, xSize - 39, 12));
        text.setMaxStringLength(10);
        text.setInputValidator(InputValidator.DIGIT);
        text.func_230996_d_(true);
        text.configureDigitalInput(this::setCount);
    }

    private void setCount() {
        if (!text.getText().isEmpty()) {
            long count = Long.parseLong(text.getText());
            Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_REDSTONE_ADAPTER_COUNT, tile, (int) Math.min(count, Integer.MAX_VALUE)));
            text.setText("");
        }
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        if (tile.getItemType() != null) {
            renderItem(matrix, tile.getItemType(), 8, 31);
        }
        super.func_230451_b_(matrix, mouseX, mouseY);
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        super.func_231044_a_(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - getGuiLeft();
            double yAxis = mouseY - getGuiTop();
            if (xAxis >= 8 && xAxis < 24 && yAxis >= 31 && yAxis < 47) {
                ItemStack stack = getMinecraft().player.inventory.getItemStack();
                if (!stack.isEmpty() && !func_231173_s_()) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, StackUtils.size(stack, 1)));
                } else if (stack.isEmpty() && func_231173_s_()) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, ItemStack.EMPTY));
                }
                SoundHandler.playSound(MekanismSounds.BEEP.get());
            }
        }
        return true;
    }
}