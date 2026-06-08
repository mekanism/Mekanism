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
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;

/// Creates a large resource stack from a given [`resource`][Resource] and a `long amount`.
///
/// @param resource The resource to wrap the stack around. If this is empty then the amount **must** be `0`
/// @param amount   The amount of the resource the stack is holding. **Must be non-negative**. If this is `0` then the `resource` **must** be empty.
///
/// @see net.neoforged.neoforge.transfer.resource.ResourceStack
/// @since 10.8.0
public record LargeResourceStack<RESOURCE extends @NonNull Resource>(RESOURCE resource, @Range(from = 0, to = Long.MAX_VALUE) long amount) {

    /// Stack helper for interacting with large chemical resource stacks.
    public static final StackHelper<ChemicalResource> CHEMICAL_HELPER = StackHelper.create(ChemicalResource.EMPTY, ChemicalResource.CODEC, ChemicalResource.STREAM_CODEC);
    /// Stack helper for interacting with large fluid resource stacks.
    public static final StackHelper<FluidResource> FLUID_HELPER = StackHelper.create(FluidResource.EMPTY, FluidResource.CODEC, FluidResource.STREAM_CODEC);
    /// Stack helper for interacting with large item resource stacks.
    public static final StackHelper<ItemResource> ITEM_HELPER = StackHelper.create(ItemResource.EMPTY, ItemResource.CODEC, ItemResource.STREAM_CODEC);

    public LargeResourceStack {
        MekanismPreconditions.checkNonNegative(amount);
        Objects.requireNonNull(resource, "Resource cannot be null");
        if (resource.isEmpty() != (amount == 0)) {
            throw new IllegalArgumentException("The resource can only be empty if the amount is zero");
        }
    }

    /// Checks if the resource stack is empty, meaning that the amount is zero.
    ///
    /// @return `true` if empty
    public boolean isEmpty() {
        //Note: We validate in the constructor that resource returns true for empty only when amount is zero
        return amount == 0;
    }

    /// Checks whether a resource is equivalent to the resource type of this stack.
    ///
    /// @param resource Resource to check.
    ///
    /// @return `true` if the resource matches.
    public boolean matches(RESOURCE resource) {
        return this.resource.equals(resource);
    }

    /// Helper to get the amount represented by this resource stack as an `int`
    ///
    /// @return Amount clamped to an `int`.
    public int amountAsInt() {
        return Ints.saturatedCast(amount);
    }

    /// Helper to create a new large resource stack that has been grown by the given amount.
    ///
    /// @param amountToGrow Amount to grow the stack by. **Must be non-negative**
    /// @param clamp        `true` to clamp the stack's size at max long. If `false` it will throw an exception if it overflows.
    ///
    /// @return A large resource stack with an increased amount.
    ///
    /// @throws IllegalStateException if trying to grow the stack by an amount larger than zero and this stack is empty.
    public LargeResourceStack<RESOURCE> grow(@Range(from = 0, to = Long.MAX_VALUE) long amountToGrow, boolean clamp) {
        MekanismPreconditions.checkNonNegative(amountToGrow);
        if (amountToGrow == 0) {
            return this;
        } else if (isEmpty()) {
            throw new IllegalStateException("Cannot grow empty stack");
        } else if (clamp) {
            if (amount < Long.MAX_VALUE - amountToGrow) {
                return new LargeResourceStack<>(resource, amount + amountToGrow);
            }
            return new LargeResourceStack<>(resource, Long.MAX_VALUE);
        }
        return new LargeResourceStack<>(resource, Math.addExact(amount, amountToGrow));
    }

    @NonNull
    @Override
    public String toString() {
        return amount + "x " + resource;
    }

    /// Helper for dealing with large resource stacks of a given type. Create via [#create(Resource, Codec, StreamCodec)]
    ///
    /// @param empty         Empty stack instance.
    /// @param codec         Codec for the resource stack, does **not** accept empty resource stacks.
    /// @param optionalCodec Codec for the resource stack. Same format as [#codec()], and also accepts empty resource stacks.
    /// @param orEmptyCodec  Codec for the resource stack that returns the empty resource stack if deserialization failed. Same format as [#codec()], and also accepts
    /// empty resource stacks.
    /// @param streamCodec   Stream codec for the resource stack. Accepts empty resource stacks.
    /// @param <RESOURCE>    Resource type that this helper helps with.
    ///
    /// @see #CHEMICAL_HELPER Helper for chemical resources
    /// @see #FLUID_HELPER Helper for fluid resources
    /// @see #ITEM_HELPER Helper for item resources
    public record StackHelper<RESOURCE extends @NonNull Resource>(
          LargeResourceStack<RESOURCE> empty,
          Codec<LargeResourceStack<RESOURCE>> codec,
          Codec<LargeResourceStack<RESOURCE>> optionalCodec,
          Codec<LargeResourceStack<RESOURCE>> orEmptyCodec,
          StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<RESOURCE>> streamCodec
    ) {

        /// Helper to create a stack of the given resource and amount. Unlike [#LargeResourceStack] this method does not require the resource and amount to both be
        /// non-empty to successfully create an empty stack, and will return the cached empty instance in the case that either are empty.
        ///
        /// @param resource The resource to wrap the stack around.
        /// @param amount   The amount of the resource the stack is holding. **Must be non-negative**.
        ///
        /// @return Large resource stack, or the empty instance if the resource is empty or the amount is zero.
        public LargeResourceStack<RESOURCE> createStack(RESOURCE resource, @Range(from = 0, to = Long.MAX_VALUE) long amount) {
            MekanismPreconditions.checkNonNegative(amount);
            Objects.requireNonNull(resource, "Resource cannot be null");
            if (resource.isEmpty() || amount == 0) {
                return empty;
            }
            return new LargeResourceStack<>(resource, amount);
        }

        /// Helper to store a large resource stack if it is not empty, via the given key on the given output.
        ///
        /// @param output Output to write to.
        /// @param key    Key to store the resource stack at.
        /// @param stack  The stack to store if it is non-empty.
        public void storeNonEmpty(ValueOutput output, String key, LargeResourceStack<RESOURCE> stack) {
            if (!stack.isEmpty()) {
                output.store(key, codec, stack);
            }
        }

        /// Helper to read a large resource stack from the key on the given input.
        ///
        /// @param input Input to read from.
        /// @param key   Key to read.
        ///
        /// @return Stored resource stack or the empty instance if the key was not present or could not be read.
        public LargeResourceStack<RESOURCE> readOrEmpty(ValueInput input, String key) {
            return input.read(key, codec).orElse(empty);
        }

        /// Creates a stack helper for dealing with large resource stacks of a given type.
        ///
        /// @param emptyResource       Empty instance of a resource.
        /// @param resourceCodec       Codec for the resource, should **not** accept empty resources.
        /// @param resourceStreamCodec Stream codec for the resource, should accept empty resources.
        /// @param <RESOURCE>          Resource type that this helper helps with.
        ///
        /// @return Resource stack helper.
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