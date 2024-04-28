package mekanism.common.attachments.containers.chemical.slurry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedSlurries(List<SlurryStack> containers) implements IAttachedContainers<SlurryStack, AttachedSlurries> {

    public static final Codec<AttachedSlurries> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          SlurryStack.OPTIONAL_CODEC.listOf().fieldOf(NBTConstants.SLURRY_TANKS).forGetter(AttachedSlurries::containers)
    ).apply(instance, AttachedSlurries::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachedSlurries> STREAM_CODEC =
          SlurryStack.OPTIONAL_STREAM_CODEC.<List<SlurryStack>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                .map(AttachedSlurries::new, AttachedSlurries::containers);

    public AttachedSlurries {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    public AttachedSlurries(int containers) {
        this(NonNullList.withSize(containers, SlurryStack.EMPTY));
    }

    @Override
    public AttachedSlurries create(List<SlurryStack> containers) {
        return new AttachedSlurries(containers);
    }
}