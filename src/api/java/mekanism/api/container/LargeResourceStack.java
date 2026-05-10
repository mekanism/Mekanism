package mekanism.api.container;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.NonNull;

//TODO - 26.1: Docs and reference ResourceStack
//TODO - 26.1: Should we sanitize the input amount and clamp or throw it so it isn't negative?(if we do add a Range annotation)
public record LargeResourceStack<RESOURCE extends @NonNull Resource>(RESOURCE resource, long amount) {

    public boolean isEmpty() {
        return amount <= 0 || resource.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return amount + "x " + resource;
    }

    public static <RESOURCE extends @NonNull Resource> Codec<LargeResourceStack<RESOURCE>> codec(Codec<RESOURCE> resourceCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
              resourceCodec.fieldOf(SerializationConstants.TYPE).forGetter(LargeResourceStack::resource),
              SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.AMOUNT).forGetter(LargeResourceStack::amount)
        ).apply(instance, LargeResourceStack::new));
    }
}