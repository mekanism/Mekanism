package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record FrequencyAware<FREQ extends Frequency>(Optional<FrequencyIdentity> identity, Optional<FREQ> frequency) {

    public static final FrequencyAware<?> NONE = new FrequencyAware<>(Optional.empty(), Optional.empty());

    @SuppressWarnings("unchecked")
    public static <FREQ extends Frequency> FrequencyAware<FREQ> none() {
        return (FrequencyAware<FREQ>) NONE;
    }

    public static <FREQ extends Frequency> Codec<FrequencyAware<FREQ>> codec(FrequencyType<FREQ> frequencyType) {
        return RecordCodecBuilder.create(instance -> instance.group(
              frequencyType.getIdentitySerializer().codec().optionalFieldOf(SerializationConstants.IDENTITY).forGetter(FrequencyAware::identity)
        ).apply(instance, identity -> {
            FREQ frequency = null;
            if (identity.isPresent() && EffectiveSide.get().isServer()) {
                //Only try to look up the frequency on the server
                frequency = frequencyType.getManager(identity.get(), identity.get().ownerUUID()).getFrequency(identity.get().key());
            }
            return new FrequencyAware<>(identity, Optional.ofNullable(frequency));
        }));
    }

    public static <FREQ extends Frequency> StreamCodec<ByteBuf, FrequencyAware<FREQ>> streamCodec(FrequencyType<FREQ> frequencyType) {
        return ByteBufCodecs.optional(frequencyType.getIdentitySerializer().streamCodec()).map(identity -> {
            FREQ frequency = null;
            if (identity.isPresent() && EffectiveSide.get().isServer()) {
                //Only try to look up the frequency on the server
                //Note: This will almost always be false
                //TODO - 1.21: Do we want to remove this branch and have the optional always be empty when transmitting via stream?
                frequency = frequencyType.getManager(identity.get(), identity.get().ownerUUID()).getFrequency(identity.get().key());
            }
            return new FrequencyAware<>(identity, Optional.ofNullable(frequency));
        }, FrequencyAware::identity);
    }

    public static final StreamCodec<ByteBuf, DriveMetadata> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_LONG, DriveMetadata::count,
          ByteBufCodecs.VAR_INT, DriveMetadata::types,
          DriveMetadata::new
    );

    public FrequencyAware(@NotNull FREQ freq) {
        this(Optional.of(freq.getIdentity()), Optional.of(freq));
    }

    @Nullable
    public UUID getOwner() {
        return identity.map(FrequencyIdentity::ownerUUID).orElse(null);
    }

    @Nullable
    public FREQ getFrequency(ItemStack stack, DataComponentType<FrequencyAware<FREQ>> type) {
        FREQ frequency = frequency().orElse(null);
        if (frequency != null && frequency.getSecurity() == SecurityMode.TRUSTED && EffectiveSide.get().isServer()) {
            //If it is a trusted frequency, and we are on the server, validate whether the owner of the item can actually access the frequency
            UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
            if (ownerUUID != null && !frequency.ownerMatches(ownerUUID)) {
                SecurityFrequency security = FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequency(frequency.getOwner());
                if (security != null && !security.isTrusted(ownerUUID)) {
                    //TODO - 1.21: Re-evaluate this
                    stack.remove(type);
                    if (stack.getItem() instanceof IColoredItem) {
                        stack.remove(MekanismDataComponents.COLOR);
                    }
                }
            }
        }
        return frequency;
    }

    public static <FREQ extends Frequency> FrequencyAware<FREQ> create(FrequencyType<FREQ> frequencyType, FrequencyIdentity data, UUID player) {
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
        return new FrequencyAware<>(Optional.of(freq.getIdentity()), Optional.of(freq));
    }
}