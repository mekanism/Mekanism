package mekanism.common.attachments;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class OverflowAware implements INBTSerializable<ListTag> {

    //Note: Linked map to ensure each call to save is in the same order so that there is more uniformity
    private final Object2IntMap<HashedItem> overflow = new Object2IntLinkedOpenHashMap<>();

    public OverflowAware(IAttachmentHolder attachmentHolder) {
        loadLegacyData(attachmentHolder);
    }

    @Deprecated//TODO - 1.21: Remove this legacy way of loading data
    private void loadLegacyData(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty() && ItemDataUtils.hasData(stack, NBTConstants.OVERFLOW, Tag.TAG_LIST)) {
            deserializeNBT(ItemDataUtils.getList(stack, NBTConstants.OVERFLOW));
            //Remove the legacy data now that it has been parsed and loaded
            ItemDataUtils.removeData(stack, NBTConstants.OVERFLOW);
        }
    }

    public Object2IntMap<HashedItem> getOverflow() {
        return overflow;
    }

    public void setOverflow(Object2IntMap<HashedItem> overflow) {
        this.overflow.clear();
        this.overflow.putAll(overflow);
    }

    public boolean isCompatible(OverflowAware other) {
        return other == this || overflow.equals(other.overflow);
    }

    @Nullable
    @Override
    public ListTag serializeNBT() {
        return this.overflow.isEmpty() ? null : writeOverflow(overflow);
    }

    @Override
    public void deserializeNBT(ListTag overflowTag) {
        overflow.clear();
        readOverflow(overflow, overflowTag);
    }

    public static ListTag writeOverflow(Object2IntMap<HashedItem> overflow) {
        ListTag overflowTag = new ListTag();
        for (Object2IntMap.Entry<HashedItem> entry : overflow.object2IntEntrySet()) {
            CompoundTag overflowComponent = new CompoundTag();
            overflowComponent.put(NBTConstants.TYPE, entry.getKey().internalToNBT());
            overflowComponent.putInt(NBTConstants.COUNT, entry.getIntValue());
            overflowTag.add(overflowComponent);
        }
        return overflowTag;
    }

    public static void readOverflow(Object2IntMap<HashedItem> overflow, ListTag overflowTag) {
        for (int i = 0, size = overflowTag.size(); i < size; i++) {
            CompoundTag overflowComponent = overflowTag.getCompound(i);
            int count = overflowComponent.getInt(NBTConstants.COUNT);
            if (count > 0) {
                //The count should always be greater than zero, but validate it just in case before trying to read the item
                ItemStack s = ItemStack.of(overflowComponent.getCompound(NBTConstants.TYPE));
                //Only add the item if the item could be read. If it can't that means the mod adding the item was probably removed
                if (!s.isEmpty()) {
                    //Note: We can use a raw stack as we just created a new stack from NBT
                    overflow.put(HashedItem.raw(s), count);
                }
            }
        }
    }
}