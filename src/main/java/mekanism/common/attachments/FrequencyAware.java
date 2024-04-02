package mekanism.common.attachments;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.frequency.TileComponentFrequency;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FrequencyAware<FREQ extends Frequency> implements INBTSerializable<CompoundTag> {

    public static FrequencyAware<?> create(IAttachmentHolder holder) {
        if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.getItem() instanceof IFrequencyItem frequencyItem) {
            return new FrequencyAware<>(stack, frequencyItem.getFrequencyType());
        }
        throw new IllegalArgumentException("Attempted to attach frequency awareness to an object that does not support frequencies.");
    }

    private final FrequencyType<FREQ> frequencyType;
    private final ItemStack attachmentHolder;
    @Nullable
    private FrequencyIdentity identity;
    @Nullable
    private FREQ frequency;

    private FrequencyAware(ItemStack stack, FrequencyType<FREQ> frequencyType) {
        this(stack, frequencyType, null, null);
    }

    private FrequencyAware(ItemStack attachmentHolder, FrequencyType<FREQ> frequencyType, @Nullable FrequencyIdentity identity, @Nullable FREQ frequency) {
        this.attachmentHolder = attachmentHolder;
        this.frequencyType = frequencyType;
        this.frequency = frequency;
        this.identity = identity;
    }

    @Nullable
    public UUID getOwner() {
        return identity == null ? null : identity.ownerUUID();
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
        if (frequency != null && frequency.getSecurity() == SecurityMode.TRUSTED && EffectiveSide.get().isServer()) {
            //If it is a trusted frequency, and we are on the server, validate whether the owner of the item can actually access the frequency
            UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(attachmentHolder);
            if (ownerUUID != null && !frequency.ownerMatches(ownerUUID)) {
                SecurityFrequency security = FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequency(frequency.getOwner());
                if (security != null && !security.isTrusted(ownerUUID)) {
                    setFrequency(null);
                }
            }
        }
        return frequency;
    }

    public void setFrequency(FrequencyIdentity data, UUID player) {
        //Note: We don't bother validating if the frequency is public or not here, as if it isn't then
        // a new private frequency will just be created for the player who sent a packet they shouldn't
        // have been able to send due to not knowing what private frequencies exist for other players
        FrequencyManager<FREQ> manager;
        FREQ freq = null;
        if (!Objects.equals(data.ownerUUID(), player) && SecurityUtils.get().isTrusted(data.securityMode(), data.ownerUUID(), player)) {
            manager = frequencyType.getManager(data, data.ownerUUID());
            freq = manager.getFrequency(data.key());
            if (freq == null) {
                //Frequency doesn't exist, update the data to having the player as the owner
                data = new FrequencyIdentity(data.key(), data.securityMode(), player);
            }
        }
        if (freq == null) {
            //If the player is the owner, or is trying to create a new trusted frequency, create it for this player instead
            manager = frequencyType.getManager(data, player);
            freq = manager.getOrCreateFrequency(data, player);
        }
        setFrequency(freq);
    }

    public void removeFrequency(FrequencyIdentity data, UUID player) {
        FrequencyManager<FREQ> manager = frequencyType.getManager(data, data.ownerUUID() == null ? player : data.ownerUUID());
        if (manager.remove(data.key(), player)) {
            FrequencyIdentity current = getIdentity();
            if (current != null && current.equals(data)) {
                //If the frequency we are removing matches the stored frequency, remove it
                setFrequency(null);
            }
        }
    }

    public void setFrequency(@Nullable FREQ frequency) {
        this.frequency = frequency;
        this.identity = this.frequency == null ? null : this.frequency.getIdentity();
        if (IColoredItem.supports(attachmentHolder)) {
            if (this.frequency == null) {
                attachmentHolder.removeData(MekanismAttachmentTypes.COLORABLE);
            } else {
                attachmentHolder.setData(MekanismAttachmentTypes.COLORABLE, Optional.of(((IColorableFrequency) this.frequency).getColor()));
            }
        }
    }

    public void copyFrom(TileComponentFrequency component) {
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
        return Objects.equals(identity, other.identity);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        if (frequency == null) {
            //If we have no frequency, try to serialize it based on a stored identity (such as if we are on the client)
            return identity == null ? null : frequencyType.getIdentitySerializer().serialize(identity);
        }
        return frequency.serializeIdentityWithOwner();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        identity = FrequencyIdentity.load(frequencyType, nbt);
        if (identity != null && EffectiveSide.get().isServer()) {
            //Only try to look up the frequency on the server
            frequency = frequencyType.getManager(identity, identity.ownerUUID()).getFrequency(identity.key());
        }
    }

    @Nullable
    public FrequencyAware<FREQ> copy(IAttachmentHolder holder) {
        if (frequency == null && identity == null) {
            return null;
        } else if (holder instanceof ItemStack stack && !stack.isEmpty() && stack.getItem() instanceof IFrequencyItem frequencyItem &&
                   frequencyItem.getFrequencyType() == frequencyType) {
            return new FrequencyAware<>(stack, frequencyType, identity, frequency);
        }
        return null;
    }
}