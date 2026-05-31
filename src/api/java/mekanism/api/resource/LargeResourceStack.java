package mekanism.api.resource;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismPreconditions;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.NonNull;

//TODO - 26.1: Docs and reference ResourceStack
//TODO - 26.1: Should we sanitize the input amount and clamp or throw it so it isn't negative?(if we do add a Range annotation)
//TODO - 26.1: Would it be of more use for some of the usages of this to just add codecs for ResourceStack, rather than using LargeResourceStack
public record LargeResourceStack<RESOURCE extends @NonNull Resource>(RESOURCE resource, long amount) {

    public static final StackHelper<ChemicalResource> CHEMICAL_HELPER = StackHelper.create(ChemicalResource.EMPTY, ChemicalResource.CODEC, ChemicalResource.STREAM_CODEC);
    public static final StackHelper<FluidResource> FLUID_HELPER = StackHelper.create(FluidResource.EMPTY, FluidResource.CODEC, FluidResource.STREAM_CODEC);
    public static final StackHelper<ItemResource> ITEM_HELPER = StackHelper.create(ItemResource.EMPTY, ItemResource.CODEC, ItemResource.STREAM_CODEC);

    public LargeResourceStack {
        MekanismPreconditions.checkNonNegative(amount);
        Objects.requireNonNull(resource, "Resource cannot be null");
        if (resource.isEmpty() != (amount == 0)) {
            throw new IllegalArgumentException("The resource can only be empty if the amount is zero");
        }
    }

    public boolean isEmpty() {
        //Note: We validate in the constructor that resource returns true for empty only when amount is zero
        return amount == 0;
    }

    public boolean matches(RESOURCE resource) {
        return this.resource.equals(resource);
    }

    public int amountAsInt() {
        return Ints.saturatedCast(amount);
    }

    @NonNull
    @Override
    public String toString() {
        return amount + "x " + resource;
    }

    public record StackHelper<RESOURCE extends @NonNull Resource>(
          LargeResourceStack<RESOURCE> empty,
          Codec<LargeResourceStack<RESOURCE>> codec,
          Codec<LargeResourceStack<RESOURCE>> optionalCodec,
          Codec<LargeResourceStack<RESOURCE>> orEmptyCodec,
          StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>> streamCodec
    ) {

        public LargeResourceStack<RESOURCE> createStack(RESOURCE resource, long amount) {
            MekanismPreconditions.checkNonNegative(amount);
            Objects.requireNonNull(resource, "Resource cannot be null");
            if (resource.isEmpty() || amount == 0) {
                return empty;
            }
            return new LargeResourceStack<>(resource, amount);
        }

        public void storeNonEmpty(ValueOutput output, String key, LargeResourceStack<RESOURCE> stack) {
            if (!stack.isEmpty()) {
                output.store(key, codec, stack);
            }
            //TODO - 26.1: Buffered transmitters used to discard the key from output if it was empty... Is that something we want to be doing?
        }

        public LargeResourceStack<RESOURCE> readOrEmpty(ValueInput input, String key) {
            return input.read(key, codec).orElse(empty);
        }

        public static <RESOURCE extends @NonNull Resource> StackHelper<RESOURCE> create(RESOURCE emptyResource, Codec<RESOURCE> resourceCodec,
              StreamCodec<RegistryFriendlyByteBuf, RESOURCE> resourceStreamCodec) {
            LargeResourceStack<RESOURCE> emptyStack = new LargeResourceStack<>(emptyResource, 0);
            Codec<LargeResourceStack<RESOURCE>> codec = RecordCodecBuilder.create(instance -> instance.group(
                  resourceCodec.fieldOf(SerializationConstants.TYPE).forGetter(LargeResourceStack::resource),
                  ExtraCodecs.NON_NEGATIVE_LONG.fieldOf(SerializationConstants.AMOUNT).forGetter(LargeResourceStack::amount)
            ).apply(instance, (resource, amount) -> {
                if (resource.isEmpty() || amount == 0) {
                    return emptyStack;
                }
                return new LargeResourceStack<>(resource, amount);
            }));
            Codec<LargeResourceStack<RESOURCE>> optionalCodec = ExtraCodecs.optionalEmptyMap(codec)
                  .xmap(optional -> optional.orElse(emptyStack), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
            Codec<LargeResourceStack<RESOURCE>> orEmptyCodec = optionalCodec.orElse(
                  (Consumer<String>) error -> MekanismAPI.logger.error("Tried to load invalid resource: '{}'", error),
                  emptyStack
            );
            return new StackHelper<>(emptyStack, codec, optionalCodec, orEmptyCodec, new StreamCodec<>() {
                @NonNull
                @Override
                public LargeResourceStack<RESOURCE> decode(@NonNull RegistryFriendlyByteBuf buf) {
                    long amount = buf.readVarLong();
                    if (amount <= 0) {
                        return emptyStack;
                    }
                    RESOURCE resource = resourceStreamCodec.decode(buf);
                    if (resource.isEmpty()) {
                        //Note: This should never be the empty resource, as otherwise we would have been handled by the above path
                        // We just check it in case someone sent an invalid packet to handle it more gracefully
                        return emptyStack;
                    }
                    return new LargeResourceStack<>(resource, amount);
                }

                @Override
                public void encode(@NonNull RegistryFriendlyByteBuf buf, @NonNull LargeResourceStack<RESOURCE> stack) {
                    if (stack.isEmpty()) {
                        buf.writeVarLong(0);
                    } else {
                        buf.writeVarLong(stack.amount());
                        resourceStreamCodec.encode(buf, stack.resource());
                    }
                }
            });
        }
    }
}