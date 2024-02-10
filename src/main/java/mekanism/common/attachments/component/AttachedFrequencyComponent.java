package mekanism.common.attachments.component;

import java.util.Objects;
import java.util.Set;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class AttachedFrequencyComponent implements IAttachedComponent<TileComponentFrequency> {

    private final ItemStack stack;
    //Note: As we don't really need the frequency component object accessible on the item we can just keep track of it as a CompoundTag
    @Nullable
    private CompoundTag frequencyNBT;

    public AttachedFrequencyComponent(IAttachmentHolder attachmentHolder) {
        if (attachmentHolder instanceof ItemStack itemStack) {
            this.stack = itemStack;
        } else {
            this.stack = ItemStack.EMPTY;
        }
        loadLegacyData();
    }

    @Deprecated//TODO - 1.21: Remove this legacy way of loading data
    private void loadLegacyData() {
        if (!stack.isEmpty()) {
            ItemDataUtils.getAndRemoveData(stack, NBTConstants.COMPONENT_FREQUENCY, CompoundTag::getCompound).ifPresent(this::deserializeNBT);
        }
    }

    public boolean isCompatible(AttachedFrequencyComponent other) {
        return other == this || Objects.equals(frequencyNBT, other.frequencyNBT);
    }

    @Override
    public void copyFrom(TileComponentFrequency component) {
        Set<FrequencyType<?>> customFrequencies = component.getCustomFrequencies();
        if (!customFrequencies.isEmpty()) {
            CompoundTag serializedComponent = component.serialize();
            if (serializedComponent.contains(FrequencyType.SECURITY.getName(), Tag.TAG_COMPOUND)) {
                //Don't persist security frequency to items as that is instead stored from the security component to the SecurityObject
                serializedComponent.remove(FrequencyType.SECURITY.getName());
            }
            deserializeNBT(serializedComponent);
            if (stack.getItem() instanceof IFrequencyItem frequencyItem && customFrequencies.contains(frequencyItem.getFrequencyType())) {
                stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE).copyFrom(component);
            }
        }
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        if (frequencyNBT == null || frequencyNBT.isEmpty()) {
            return null;
        }
        return frequencyNBT;
    }

    @Override
    public void deserializeNBT(CompoundTag frequencyNBT) {
        if (frequencyNBT.isEmpty()) {
            this.frequencyNBT = null;
        } else {
            this.frequencyNBT = frequencyNBT;
        }
    }
}