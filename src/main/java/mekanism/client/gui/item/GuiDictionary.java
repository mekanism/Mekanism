package mekanism.client.gui.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDropdown;
import mekanism.client.gui.element.custom.GuiDictionaryTarget;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.GuiComponents.IDropdownEnum;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.registries.MekanismItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

//TODO: Eventually it would be nice that when a tag is selected in the GUI that it shows everything else that is in that tag
public class GuiDictionary extends GuiMekanism<DictionaryContainer> {

    private GuiTextScrollList scrollList;
    private GuiDictionaryTarget target;
    private DictionaryTagType currentType = DictionaryTagType.ITEM;

    public GuiDictionary(DictionaryContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 5;
        playerInventoryTitleY = ySize - 96;
        titleY = 5;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.NORMAL, this, 5, 5).setRenderHover(true));
        addButton(scrollList = new GuiTextScrollList(this, 7, 29, 162, 42));
        //TODO: Ideally we would eventually replace this with some sort of tab system as it would probably look better
        // and could then be limited to just the tags the target supports
        addButton(new GuiDropdown<>(this, 124, 73, 45, DictionaryTagType.class, () -> currentType, this::setCurrentType));
        addButton(target = new GuiDictionaryTarget(this, 6, 6, this::updateScrollList));
    }

    private void setCurrentType(DictionaryTagType type) {
        currentType = type;
        scrollList.setText(target.getTags(currentType));
    }

    private void updateScrollList(Set<DictionaryTagType> supportedTypes) {
        if (!supportedTypes.contains(currentType) && !supportedTypes.isEmpty()) {
            currentType = supportedTypes.stream().findFirst().orElse(currentType);
        }
        scrollList.setText(target.getTags(currentType));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismItems.DICTIONARY.getTextComponent(), titleY);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        drawTextScaledBound(matrix, MekanismLang.DICTIONARY_TAG_TYPE.translate(), 77, playerInventoryTitleY, titleTextColor(), 45);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hasShiftDown() && !target.hasTarget()) {
            for (int i = 0; i < container.inventorySlots.size(); i++) {
                Slot slot = container.inventorySlots.get(i);
                if (isMouseOverSlot(slot, mouseX, mouseY)) {
                    ItemStack stack = slot.getStack();
                    if (stack.isEmpty()) {
                        break;
                    }
                    target.setTargetSlot(stack, true);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public enum DictionaryTagType implements IDropdownEnum<DictionaryTagType> {
        ITEM(MekanismLang.DICTIONARY_ITEM, MekanismLang.DICTIONARY_ITEM_DESC),
        BLOCK(MekanismLang.DICTIONARY_BLOCK, MekanismLang.DICTIONARY_BLOCK_DESC),
        FLUID(MekanismLang.DICTIONARY_FLUID, MekanismLang.DICTIONARY_FLUID_DESC),
        ENTITY_TYPE(MekanismLang.DICTIONARY_ENTITY_TYPE, MekanismLang.DICTIONARY_ENTITY_TYPE_DESC),
        POTION(MekanismLang.DICTIONARY_POTION, MekanismLang.DICTIONARY_POTION_DESC),
        ENCHANTMENT(MekanismLang.DICTIONARY_ENCHANTMENT, MekanismLang.DICTIONARY_ENCHANTMENT_DESC),
        TILE_ENTITY_TYPE(MekanismLang.DICTIONARY_TILE_ENTITY_TYPE, MekanismLang.DICTIONARY_TILE_ENTITY_TYPE_DESC),
        GAS(MekanismLang.DICTIONARY_GAS, MekanismLang.DICTIONARY_GAS_DESC),
        INFUSE_TYPE(MekanismLang.DICTIONARY_INFUSE_TYPE, MekanismLang.DICTIONARY_INFUSE_TYPE_DESC),
        PIGMENT(MekanismLang.DICTIONARY_PIGMENT, MekanismLang.DICTIONARY_PIGMENT_DESC),
        SLURRY(MekanismLang.DICTIONARY_SLURRY, MekanismLang.DICTIONARY_SLURRY_DESC);

        private final ILangEntry name;
        private final ILangEntry tooltip;

        DictionaryTagType(ILangEntry name, ILangEntry tooltip) {
            this.name = name;
            this.tooltip = tooltip;
        }

        @Override
        public ITextComponent getTooltip() {
            return tooltip.translate();
        }

        @Override
        public ITextComponent getShortName() {
            return name.translate();
        }
    }
}