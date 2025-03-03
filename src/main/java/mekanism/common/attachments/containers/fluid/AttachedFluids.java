package mekanism.common.attachments.containers.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public record AttachedFluids(List<FluidStack> containers) implements IAttachedContainers<FluidStack, AttachedFluids> {

    public static final AttachedFluids EMPTY = new AttachedFluids(Collections.emptyList());

    public static final Codec<AttachedFluids> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          SerializerHelper.LENIENT_OPTIONAL_FLUID_CODEC.listOf().fieldOf(SerializationConstants.FLUID_TANKS).forGetter(AttachedFluids::containers)
    ).apply(instance, AttachedFluids::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachedFluids> STREAM_CODEC =
          FluidStack.OPTIONAL_STREAM_CODEC.<List<FluidStack>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                .map(AttachedFluids::new, AttachedFluids::containers);

    public static AttachedFluids create(int containers) {
        return new AttachedFluids(NonNullList.withSize(containers, FluidStack.EMPTY));
    }

    public AttachedFluids {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public FluidStack getEmptyStack() {
        return FluidStack.EMPTY;
    }

    @Override
    public AttachedFluids create(List<FluidStack> containers) {
        return new AttachedFluids(containers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        List<FluidStack> otherContainers = ((AttachedFluids) o).containers;
        if (containers.size() != otherContainers.size()) {
            return false;
        }
        for (int i = 0; i < containers.size(); i++) {
            if (!FluidStack.matches(containers.get(i), otherContainers.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (FluidStack stack : containers) {
            hash = hash * 31 + FluidStack.hashFluidAndComponents(stack);
        }
        return hash;
    }
}