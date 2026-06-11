package mekanism.client.gui;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.module.GuiModuleScreen;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.GuiMekaSuitHelmetOptions;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketUpdateModuleSettings;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class GuiModuleTweaker extends GuiMekanism<ModuleTweakerContainer> {

    private final ArmorPreview armorPreview;
    private final Consumer<ModuleConfig<?>> saveCallback;

    @Nullable
    private GuiModuleScrollList scrollList;
    @Nullable
    private GuiModuleScreen moduleScreen;
    @Nullable
    private TranslationButton optionsButton;

    private int selected = -1;

    public GuiModuleTweaker(ModuleTweakerContainer container, Inventory inv, Component title) {
        super(container, inv, title, DEFAULT_IMAGE_WIDTH + 90, DEFAULT_IMAGE_HEIGHT + 20);
        armorPreview = new ArmorPreview(this, inv.player);
        saveCallback = configItem -> {
            if (moduleScreen != null) {
                IModule<?> module = moduleScreen.getCurrentModule();
                if (module != null && selected != -1) {//Shouldn't be null but validate just in case
                    int slotIndex = menu.slots.get(selected).getSlotIndex();
                    PacketUtils.sendToServer(PacketUpdateModuleSettings.create(slotIndex, module.getDataHolder(), module.getInstalledCount(), configItem));
                }
            }
        };
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        Supplier<ItemResource> itemSupplier = () -> getItemType(selected);
        addRenderableWidget(new GuiElementHolder(this, 30, 136, 120, 18));
        moduleScreen = addRenderableWidget(new GuiModuleScreen(this, 150, 20, itemSupplier, saveCallback, armorPreview));
        scrollList = addRenderableWidget(new GuiModuleScrollList(this, 30, 20, 116, itemSupplier, moduleScreen::setModule));
        optionsButton = addRenderableWidget(new TranslationButton(this, 31, 137, 118, 16, MekanismLang.BUTTON_OPTIONS, (element, _, _) -> {
            ((GuiModuleTweaker) element.gui()).openOptions();
            return true;
        }));
        optionsButton.active = false;
        int size = menu.slots.size();
        for (int i = 0; i < size; i++) {
            Slot slot = menu.slots.get(i);
            final int index = i;
            // initialize selected item
            if (selected == -1 && isValidItem(index)) {
                select(index);
            }
            addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, slot.x - 1, slot.y - 1)
                  .click((_, _, _) -> select(index), 1.0F, MekanismSounds.BEEP_ON)
                  .overlayColor(isValidItem(index) ? null : () -> 0xCC333333)
                  .with(() -> index == selected ? SlotOverlay.SELECT : null));
        }
    }

    private void openOptions() {
        addWindow(new GuiMekaSuitHelmetOptions(this, (imageWidth - 140) / 2, (imageHeight - 140) / 2));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        if (selected != -1 && (isPreviousButton(event) || isNextButton(event))) {
            int curIndex = -1;
            IntList selectable = new IntArrayList();
            for (int index = 0, slots = menu.slots.size(); index < slots; index++) {
                if (isValidItem(index)) {
                    selectable.add(index);
                    if (index == selected) {
                        curIndex = selectable.size() - 1;
                    }
                }
            }
            int targetIndex;
            if (isPreviousButton(event)) {
                targetIndex = curIndex == 0 ? selectable.size() - 1 : curIndex - 1;
            } else {//isNextButton
                targetIndex = curIndex + 1;
            }
            select(selectable.getInt(targetIndex % selectable.size()));
            return true;
        }
        return false;
    }

    private boolean isPreviousButton(InputWithModifiers key) {
        return key.isUp() || key.isLeft();
    }

    private boolean isNextButton(InputWithModifiers key) {
        return key.isDown() || key.isRight();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (moduleScreen != null) {
            // make sure we get the release event
            moduleScreen.onRelease(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, 24);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private boolean select(int index) {
        if (isValidItem(index)) {
            selected = index;
            ItemResource itemType = getItemType(index);
            armorPreview.tryUpdateFull(menu.slots.get(index).getItem());
            if (scrollList != null) {//Should never be null here
                scrollList.updateItemAndList(itemType);
                scrollList.clearSelection();
            }
            if (optionsButton != null) {//Should never be null here
                optionsButton.active = MekanismItems.MEKASUIT_HELMET.is(itemType);
            }
            return true;
        }
        return false;
    }

    private boolean isValidItem(int index) {
        return ModuleTweakerContainer.isTweakableItem(getItemType(index));
    }

    private ItemResource getItemType(int index) {
        if (index == -1) {
            return ItemResource.EMPTY;
        }
        return ItemResource.of(menu.slots.get(index).getItem());
    }

    public static class ArmorPreview implements Supplier<LivingEntity> {

        private final Map<EquipmentSlot, Supplier<ItemStack>> lazyItems = new EnumMap<>(EquipmentSlot.class);
        private final IGuiWrapper gui;
        @Nullable
        private ArmorStand preview;

        protected ArmorPreview(IGuiWrapper gui, Player player) {
            this.gui = gui;
            for (EquipmentSlot armorSlot : EnumUtils.ARMOR_SLOTS) {
                lazyItems.put(armorSlot, () -> {
                    ItemStack stack = player.getItemBySlot(armorSlot);
                    if (stack.isEmpty()) {
                        //Fall back to MekaSuit for rendering purposes of if not wearing a full set of stuff
                        return (switch (armorSlot) {
                            case FEET -> MekanismItems.MEKASUIT_BOOTS;
                            case LEGS -> MekanismItems.MEKASUIT_PANTS;
                            case CHEST -> MekanismItems.MEKASUIT_BODYARMOR;
                            case HEAD -> MekanismItems.MEKASUIT_HELMET;
                            default -> throw new IllegalStateException("Unknown armor slot: " + armorSlot.getName());
                        }).asStack();
                    }
                    return stack;
                });
            }
        }

        public void tryUpdateFull(ItemStack stack) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (StackUtils.isRenderableArmor(equippable)) {
                //If the selected thing is an armor item update the stack for the slot
                // this is of use in case the item may be an armor piece but is in the hotbar
                EquipmentSlot slot = equippable.slot();
                lazyItems.put(slot, () -> stack);
                updatePreview(slot, stack);
            }
        }

        public void updatePreview(EquipmentSlot slot, ItemStack stack) {
            if (preview != null) {
                preview.setItemSlot(slot, stack);
            }
        }

        public void resetToDefault(EquipmentSlot slot) {
            if (preview != null && lazyItems.containsKey(slot)) {
                updatePreview(slot, lazyItems.get(slot).get());
            }
        }

        @Override
        public LivingEntity get() {
            if (preview == null) {
                preview = new ArmorStand(EntityType.ARMOR_STAND, gui.getLevel());
                preview.setNoBasePlate(true);
                //Copy the player's current armor when we first initialize this
                for (Entry<EquipmentSlot, Supplier<ItemStack>> entry : lazyItems.entrySet()) {
                    preview.setItemSlot(entry.getKey(), entry.getValue().get());
                }
            }
            return preview;
        }
    }
}