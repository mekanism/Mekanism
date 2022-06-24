package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

//TODO: Eventually it would be nice that when a tag is selected in the GUI that it shows everything else that is in that tag
public class GuiDictionary extends GuiMekanism<DictionaryContainer> {

    private GuiTextScrollList scrollList;
    private GuiDictionaryTarget target;
    private DictionaryTagType currentType = DictionaryTagType.ITEM;

    public GuiDictionary(DictionaryContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 5;
        inventoryLabelY = imageHeight - 96;
        titleLabelY = 5;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 5, 5).setRenderHover(true));
        scrollList = addRenderableWidget(new GuiTextScrollList(this, 7, 29, 162, 42));
        //TODO: Ideally we would eventually replace this with some sort of tab system as it would probably look better
        // and could then be limited to just the tags the target supports
        addRenderableWidget(new GuiDropdown<>(this, 124, 73, 45, DictionaryTagType.class, () -> currentType, this::setCurrentType));
        target = addRenderableWidget(new GuiDictionaryTarget(this, 6, 6, this::updateScrollList));
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
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        drawTextScaledBound(matrix, MekanismLang.DICTIONARY_TAG_TYPE.translate(), 77, inventoryLabelY, titleTextColor(), 45);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hasShiftDown() && !target.hasTarget()) {
            for (int i = 0; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (isMouseOverSlot(slot, mouseX, mouseY)) {
                    ItemStack stack = slot.getItem();
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
        BLOCK_ENTITY_TYPE(MekanismLang.DICTIONARY_BLOCK_ENTITY_TYPE, MekanismLang.DICTIONARY_BLOCK_ENTITY_TYPE_DESC),
        FLUID(MekanismLang.DICTIONARY_FLUID, MekanismLang.DICTIONARY_FLUID_DESC),
        ENTITY_TYPE(MekanismLang.DICTIONARY_ENTITY_TYPE, MekanismLang.DICTIONARY_ENTITY_TYPE_DESC),
        ATTRIBUTE(MekanismLang.DICTIONARY_ATTRIBUTE, MekanismLang.DICTIONARY_ATTRIBUTE_DESC),
        POTION(MekanismLang.DICTIONARY_POTION, MekanismLang.DICTIONARY_POTION_DESC),
        MOB_EFFECT(MekanismLang.DICTIONARY_MOB_EFFECT, MekanismLang.DICTIONARY_MOB_EFFECT_DESC),
        ENCHANTMENT(MekanismLang.DICTIONARY_ENCHANTMENT, MekanismLang.DICTIONARY_ENCHANTMENT_DESC),
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
        public Component getTooltip() {
            return tooltip.translate();
        }

        @Override
        public Component getShortName() {
            return name.translate();
        }
    }
}