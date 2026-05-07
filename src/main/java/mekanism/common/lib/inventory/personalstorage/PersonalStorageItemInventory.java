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
import net.neoforged.neoforge.transfer.item.ItemResource;
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
            IInventorySlot slot = slots.get(slotData.slot);
            if (slot instanceof BasicInventorySlot basicInventorySlot) {
                basicInventorySlot.setContentsUnchecked(slotData.resource(), slotData.amount());
            } else {
                //shouldn't happen, but just in case
                slot.setContents(slotData.resource(), slotData.amount());
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
                out.add(new SlotData(i, slot.getResource(), slot.amount()));
            }
        }
        return out;
    }

    @Override
    public void onContentsChanged() {
        Objects.requireNonNull(parent, "Incorrect deserialisation, setParent not called").onContentsChanged();
    }

    record SlotData(int slot, ItemResource resource, int amount) {

        SlotData(Pair<Integer, ItemStack> pair) {
            this(pair.getFirst(), ItemResource.of(pair.getSecond()), pair.getSecond().count());
        }

        public Pair<Integer, ItemStack> asPair() {
            return Pair.of(slot, resource.toStack(amount));
        }

        //TODO - 26.1: Rewrite this codec to probably not be a pair codec, but at the very least not bother using the oversized item codec
        public static Codec<SlotData> CODEC = Codec.pair(Codec.INT, SerializerHelper.OVERSIZED_ITEM_CODEC).xmap(SlotData::new, SlotData::asPair);
    }
}
