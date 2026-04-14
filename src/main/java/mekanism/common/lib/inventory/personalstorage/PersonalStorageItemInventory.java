package mekanism.common.lib.inventory.personalstorage;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.IContentsListener;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Inventory for Personal Storages when an item. Handled by the Block when placed in world.
 */
@NothingNullByDefault
public class PersonalStorageItemInventory extends AbstractPersonalStorageItemInventory {

    public static final Codec<PersonalStorageItemInventory> CODEC = SlotData.CODEC
          .listOf()
          .promotePartial(err -> Mekanism.logger.error("Failed to parse some Personal Storage slots: {}", err))
          .xmap(PersonalStorageItemInventory::new, PersonalStorageItemInventory::toSlotData);

    @Nullable
    private IContentsListener parent;

    private PersonalStorageItemInventory(List<SlotData> loadedData) {
        this.parent = null;
        for (SlotData slotData : loadedData) {
            var slot = slots.get(slotData.slot);
            if (slot instanceof BasicInventorySlot basicInventorySlot) {
                basicInventorySlot.setStackUnchecked(slotData.stack);
            } else {
                //shouldn't happen, but just in case
                slot.setStack(slotData.stack);
            }
        }
    }

    PersonalStorageItemInventory(IContentsListener parent) {
        this.parent = parent;
    }

    protected void setParent(IContentsListener newParent) {
        this.parent = newParent;
    }

    private List<SlotData> toSlotData() {
        List<SlotData> out = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            IInventorySlot slot = slots.get(i);
            if (!slot.isEmpty()) {
                out.add(new SlotData(i, slot.getStack()));
            }
        }
        return out;
    }

    @Override
    public void onContentsChanged() {
        Objects.requireNonNull(parent, "Incorrect deserialisation, setParent not called").onContentsChanged();
    }

    record SlotData(int slot, ItemStack stack) {

        SlotData(Pair<Integer, ItemStack> pair) {
            this(pair.getFirst(), pair.getSecond());
        }

        public Pair<Integer, ItemStack> asPair() {
            return Pair.of(slot, stack);
        }

        public static Codec<SlotData> CODEC = Codec.pair(Codec.INT, SerializerHelper.OVERSIZED_ITEM_CODEC).xmap(SlotData::new, SlotData::asPair);
    }
}
