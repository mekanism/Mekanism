package mekanism.common.attachments;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FormulaAttachment implements INBTSerializable<CompoundTag> {

    public static FormulaAttachment create(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack stack && stack.getItem() instanceof ItemCraftingFormula) {
            return new FormulaAttachment(false);
        }
        throw new IllegalArgumentException("Attempted to attach a formula to something other than a crafting formula item.");
    }

    public static Optional<FormulaAttachment> formula(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemCraftingFormula) {
            return Optional.of(stack.getData(MekanismAttachmentTypes.FORMULA_HOLDER));
        }
        return Optional.empty();
    }

    public static Optional<FormulaAttachment> existingFormula(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemCraftingFormula) {
            return stack.getExistingData(MekanismAttachmentTypes.FORMULA_HOLDER);
        }
        return Optional.empty();
    }

    private final List<IInventorySlot> inventory = Util.make(() -> {
        ImmutableList.Builder<IInventorySlot> builder = ImmutableList.builder();
        for (int i = 0; i < 9; i++) {
            builder.add(new ShallowInventorySlot());
        }
        return builder.build();
    });
    private boolean invalid;

    private FormulaAttachment(boolean invalid) {
        this.invalid = invalid;
    }

    public void setInvalid() {
        this.invalid = true;
    }

    public List<IInventorySlot> getItems() {
        return inventory;
    }

    public boolean isEmpty() {
        return inventory.stream().allMatch(IInventorySlot::isEmpty);
    }

    public boolean hasItems() {
        return inventory.stream().anyMatch(slot -> !slot.isEmpty());
    }

    public void setItems(NonNullList<ItemStack> items) {
        for (int i = 0, slots = this.inventory.size(); i < slots; i++) {
            this.inventory.get(i).setStack(items.get(i).copyWithCount(1));
        }
        invalid = false;
    }

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isCompatible(FormulaAttachment other) {
        if (other == this) {
            return true;
        } else if (invalid != other.invalid) {
            return false;
        }
        for (int i = 0, slots = inventory.size(); i < slots; i++) {
            if (!inventory.get(i).isCompatible(other.inventory.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (invalid) {
            nbt.putBoolean(NBTConstants.INVALID, true);
        }
        ContainerType.ITEM.saveTo(nbt, this.inventory);
        return nbt.isEmpty() ? null : nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        invalid = nbt.getBoolean(NBTConstants.INVALID);
        ContainerType.ITEM.readFrom(nbt, inventory);
    }

    @Nullable
    public FormulaAttachment copy(IAttachmentHolder holder) {
        if (!invalid && inventory.stream().allMatch(IInventorySlot::isEmpty)) {
            return null;
        }
        FormulaAttachment copy = new FormulaAttachment(invalid);
        ContainerType.ITEM.copy(inventory, copy.inventory);
        return copy;
    }

    /**
     * Implementation of an inventory slot that saves the stack at the top level instead of one level down, and doesn't bother with size overrides
     */
    private static class ShallowInventorySlot extends BasicInventorySlot {

        private ShallowInventorySlot() {
            super(1, alwaysTrueBi, alwaysTrueBi, alwaysTrue, null, 0, 0);
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            //Set the stack in an unchecked way so that if it is no longer valid, we don't end up
            // crashing due to the stack not being valid
            setStackUnchecked(ItemStack.of(nbt));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            if (!isEmpty()) {
                current.save(nbt);
            }
            return nbt;
        }
    }
}