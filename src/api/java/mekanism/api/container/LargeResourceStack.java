package mekanism.api.container;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalResource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.NonNull;

//TODO - 26.1: Docs and reference ResourceStack
//TODO - 26.1: Should we sanitize the input amount and clamp or throw it so it isn't negative?(if we do add a Range annotation)
public record LargeResourceStack<RESOURCE extends @NonNull Resource>(RESOURCE resource, long amount) {

    //TODO - 26.1: Re-evaluate these constants
    public static final LargeResourceStack<ItemResource> EMPTY_ITEM_STACK = new LargeResourceStack<>(ItemResource.EMPTY, 0);
    public static final LargeResourceStack<FluidResource> EMPTY_FLUID_STACK = new LargeResourceStack<>(FluidResource.EMPTY, 0);
    public static final LargeResourceStack<ChemicalResource> EMPTY_CHEMICAL_STACK = new LargeResourceStack<>(ChemicalResource.EMPTY, 0);

    public boolean isEmpty() {
        return amount <= 0 || resource.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return amount + "x " + resource;
    }

    public int amountAsInt() {
        return Ints.saturatedCast(amount);
    }

    public static <RESOURCE extends @NonNull Resource> Codec<LargeResourceStack<RESOURCE>> codec(Codec<RESOURCE> resourceCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
              resourceCodec.fieldOf(SerializationConstants.TYPE).forGetter(LargeResourceStack::resource),
              SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.AMOUNT).forGetter(LargeResourceStack::amount)
        ).apply(instance, LargeResourceStack::new));
    }

    public static <RESOURCE extends @NonNull Resource> StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>> streamCodec(StreamCodec<RegistryFriendlyByteBuf, RESOURCE> resourceCodec,
          LargeResourceStack<RESOURCE> emptyStack) {
        return new StreamCodec<>() {
            @NonNull
            @Override
            public LargeResourceStack<RESOURCE> decode(@NonNull RegistryFriendlyByteBuf buf) {
                long amount = buf.readVarLong();
                if (amount <= 0) {
                    return emptyStack;
                }
                return new LargeResourceStack<>(resourceCodec.decode(buf), amount);
            }

            @Override
            public void encode(@NonNull RegistryFriendlyByteBuf buf, @NonNull LargeResourceStack<RESOURCE> stack) {
                if (stack.isEmpty()) {
                    buf.writeVarInt(0);
                } else {
                    buf.writeVarLong(stack.amount());
                    resourceCodec.encode(buf, stack.resource());
                }
            }
        };
    }
}