package mekanism.client.gui.qio;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostItemConsumer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteractionItem;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.StackUtils;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiQIORedstoneAdapter extends GuiMekanismTile<TileEntityQIORedstoneAdapter, MekanismTileContainer<TileEntityQIORedstoneAdapter>> {

    private GuiTextField text;

    public GuiQIORedstoneAdapter(MekanismTileContainer<TileEntityQIORedstoneAdapter> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 16;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiQIOFrequencyTab(this, tile));
        addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 7, 30).setRenderHover(true)).setGhostHandler((IGhostItemConsumer) ingredient -> {
            Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, StackUtils.size((ItemStack) ingredient, 1)));
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(MekanismSounds.BEEP.get(), 1.0F));
        });
        addRenderableWidget(new GuiInnerScreen(this, 7, 16, imageWidth - 15, 12, () -> {
            List<Component> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq == null) {
                list.add(MekanismLang.NO_FREQUENCY.translate());
            } else {
                list.add(MekanismLang.FREQUENCY.translate(freq.getKey()));
            }
            return list;
        }).tooltip(() -> {
            List<Component> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq != null) {
                list.add(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemCount()), TextUtils.format(freq.getTotalItemCountCapacity())));
                list.add(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemTypes(true)), TextUtils.format(freq.getTotalItemTypeCapacity())));
            }
            return list;
        }));
        addRenderableWidget(new GuiInnerScreen(this, 27, 30, imageWidth - 27 - 8, 54, () -> {
            List<Component> list = new ArrayList<>();
            list.add(tile.getItemType().isEmpty() ? MekanismLang.QIO_ITEM_TYPE_UNDEFINED.translate() : tile.getItemType().getHoverName());
            list.add(MekanismLang.QIO_TRIGGER_COUNT.translate(TextUtils.format(tile.getCount())));
            if (!tile.getItemType().isEmpty() && tile.getQIOFrequency() != null) {
                list.add(MekanismLang.QIO_STORED_COUNT.translate(TextUtils.format(tile.getStoredCount())));
            }
            return list;
        }).clearFormat());
        text = addRenderableWidget(new GuiTextField(this, 29, 70, imageWidth - 39, 12));
        text.setMaxLength(10);
        text.setInputValidator(InputValidator.DIGIT);
        text.setFocused(true);
        text.configureDigitalInput(this::setCount);
    }

    private void setCount() {
        if (!text.getText().isEmpty()) {
            long count = Long.parseLong(text.getText());
            Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.QIO_REDSTONE_ADAPTER_COUNT, tile, (int) Math.min(count, Integer.MAX_VALUE)));
            text.setText("");
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        if (tile.getItemType() != null) {
            renderItem(matrix, tile.getItemType(), 8, 31);
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - leftPos;
            double yAxis = mouseY - topPos;
            if (xAxis >= 8 && xAxis < 24 && yAxis >= 31 && yAxis < 47) {
                ItemStack stack = getMinecraft().player.containerMenu.getCarried();
                if (!stack.isEmpty() && !hasShiftDown()) {
                    Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, StackUtils.size(stack, 1)));
                } else if (stack.isEmpty() && hasShiftDown()) {
                    Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteractionItem.QIO_REDSTONE_ADAPTER_STACK, tile, ItemStack.EMPTY));
                }
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(MekanismSounds.BEEP.get(), 1.0F));
            }
        }
        return true;
    }
}