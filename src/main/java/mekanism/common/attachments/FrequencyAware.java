package mekanism.common.attachments;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FrequencyAware<FREQ extends Frequency> implements INBTSerializable<CompoundTag> {

    public static FrequencyAware<?> create(IAttachmentHolder holder) {
        if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.getItem() instanceof IFrequencyItem frequencyItem) {
            return new FrequencyAware<>(frequencyItem.getFrequencyType(), stack);
        }
        throw new IllegalArgumentException("Attempted to attach frequency awareness to an object that does not support frequencies.");
    }

    private final FrequencyType<FREQ> frequencyType;
    private final ItemStack stack;
    @Nullable
    private FrequencyIdentity identity;
    @Nullable
    private FREQ frequency;
    @Nullable
    private UUID owner;

    private FrequencyAware(FrequencyType<FREQ> frequencyType, ItemStack stack) {
        this.frequencyType = frequencyType;
        this.stack = stack;
        loadLegacyData();
    }

    @Deprecated//TODO - 1.21?: Remove this way of loading legacy data
    protected void loadLegacyData() {
        if (ItemDataUtils.hasData(stack, NBTConstants.FREQUENCY, Tag.TAG_COMPOUND)) {
            deserializeNBT(ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY));
            //Remove the legacy data now that it has been parsed and loaded
            ItemDataUtils.removeData(stack, NBTConstants.FREQUENCY);
        } else if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_FREQUENCY, Tag.TAG_COMPOUND)) {
            CompoundTag frequencyComponent = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_FREQUENCY);
            if (frequencyComponent.contains(frequencyType.getName(), Tag.TAG_COMPOUND)) {
                deserializeNBT(frequencyComponent.getCompound(frequencyType.getName()));
            }
            //Note: We don't remove legacy data here as it is still necessary/used, and we are just reading the identity
        }
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @Nullable
    public FrequencyIdentity getIdentity() {
        return identity;
    }

    public FrequencyType<FREQ> getFrequencyType() {
        return frequencyType;
    }

    @Nullable
    public FREQ getFrequency() {
        return frequency;
    }

    public void setFrequency(@Nullable FREQ frequency) {
        this.frequency = frequency;
        if (this.frequency == null) {
            this.identity = null;
            this.owner = null;
        } else {
            this.identity = this.frequency.getIdentity();
            this.owner = this.frequency.getOwner();
        }
        if (stack.getItem() instanceof IColoredItem) {
            EnumColor color = this.frequency == null ? null : ((IColorableFrequency) this.frequency).getColor();
            stack.getData(MekanismAttachmentTypes.COLORABLE).setColor(color);
        }
    }

    public void copyFromComponent(TileComponentFrequency component) {
        setFrequency(component.getFrequency(frequencyType));
    }

    public boolean isCompatible(FrequencyAware<?> other) {
        if (other == this) {
            return true;
        }
        if (frequency == null) {
            return other.frequency == null;
        } else if (other.frequency == null) {
            return false;
        }
        return Objects.equals(identity, other.identity) && Objects.equals(owner, other.owner);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        return frequency == null ? null : frequency.serializeIdentityWithOwner();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        identity = FrequencyIdentity.load(frequencyType, nbt);
        if (identity != null && nbt.hasUUID(NBTConstants.OWNER_UUID)) {
            owner = nbt.getUUID(NBTConstants.OWNER_UUID);
            frequency = frequencyType.getManager(identity, owner).getFrequency(identity.key());
        }
    }
}