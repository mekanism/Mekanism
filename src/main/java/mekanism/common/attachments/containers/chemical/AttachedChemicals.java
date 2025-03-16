package mekanism.common.attachments.containers.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public record AttachedChemicals(List<ChemicalStack> containers) implements IAttachedContainers<ChemicalStack, AttachedChemicals> {

    public static final AttachedChemicals EMPTY = new AttachedChemicals(Collections.emptyList());

    public static final Codec<AttachedChemicals> ACTUAL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ChemicalStack.LENIENT_OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.CHEMICAL_TANKS).forGetter(AttachedChemicals::containers)
    ).apply(instance, AttachedChemicals::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachedChemicals> STREAM_CODEC =
          ChemicalStack.OPTIONAL_STREAM_CODEC.<List<ChemicalStack>>apply(ByteBufCodecs.collection(NonNullList::createWithCapacity))
                      .map(AttachedChemicals::new, AttachedChemicals::containers);

    //TODO - 1.22: delete backcompat, rename ACTUAL_CODEC to CODEC
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final Codec<AttachedChemicals> GAS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ChemicalStack.LENIENT_OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.GAS_TANKS).forGetter(AttachedChemicals::containers)
    ).apply(instance, AttachedChemicals::new));
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final Codec<AttachedChemicals> INFUSE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ChemicalStack.LENIENT_OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.INFUSION_TANKS).forGetter(AttachedChemicals::containers)
    ).apply(instance, AttachedChemicals::new));
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final Codec<AttachedChemicals> PIGMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ChemicalStack.LENIENT_OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.PIGMENT_TANKS).forGetter(AttachedChemicals::containers)
    ).apply(instance, AttachedChemicals::new));
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final Codec<AttachedChemicals> SLURRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ChemicalStack.LENIENT_OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.SLURRY_TANKS).forGetter(AttachedChemicals::containers)
    ).apply(instance, AttachedChemicals::new));
    public static final Codec<AttachedChemicals> CODEC = Codec.withAlternative(ACTUAL_CODEC, Codec.withAlternative(GAS_CODEC, Codec.withAlternative(INFUSE_CODEC, Codec.withAlternative(PIGMENT_CODEC, SLURRY_CODEC))));

    public static AttachedChemicals create(int containers) {
        return new AttachedChemicals(NonNullList.withSize(containers, ChemicalStack.EMPTY));
    }

    public AttachedChemicals {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public ChemicalStack getEmptyStack() {
        return ChemicalStack.EMPTY;
    }

    @Override
    public AttachedChemicals create(List<ChemicalStack> containers) {
        return new AttachedChemicals(containers);
    }
}