package mekanism.fabric_shim.fluids;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.fabric_shim.common.MutableDataComponentHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * ItemStack equivalent for fluids: a fluid, a millibucket amount, and a data component patch.
 *
 * <p>Mekanism-owned stand-in for NeoForge's FluidStack (same surface and serialized formats, fresh
 * implementation), so recipe JSON, NBT and network encodings stay byte-compatible with upstream.
 * Amounts remain in millibuckets everywhere inside the mod; conversion to Fabric droplets
 * (x81) happens only in the transfer-API boundary adapters (Phase 2).
 *
 * <p>Differences from NeoForge: FluidType-based methods are not provided (Fabric has no FluidType);
 * name lookups go through Fabric's fluid variant attributes instead.
 */
public final class FluidStack implements MutableDataComponentHolder {

    public static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
          .validate(holder -> holder.is(Fluids.EMPTY.builtInRegistryHolder())
                              ? DataResult.error(() -> "Fluid must not be minecraft:empty")
                              : DataResult.success(holder));

    /**
     * Codec matching NeoForge's fluid stack format ({@code id}, {@code amount}, optional {@code components});
     * does not accept empty stacks.
     */
    public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(
          () -> RecordCodecBuilder.create(
                instance -> instance.group(
                      FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
                      ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount),
                      DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(stack -> stack.components.asPatch()))
                      .apply(instance, FluidStack::new)));

    /**
     * Codec that always deserializes with a fixed amount; does not accept empty stacks.
     */
    public static Codec<FluidStack> fixedAmountCodec(int amount) {
        return Codec.lazyInitialized(
              () -> RecordCodecBuilder.create(
                    instance -> instance.group(
                          FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
                          DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                                .forGetter(stack -> stack.components.asPatch()))
                          .apply(instance, (holder, patch) -> new FluidStack(holder, amount, patch))));
    }

    /**
     * Codec that accepts empty stacks, serializing them as {@code {}}.
     */
    public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
          .xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));

    /**
     * Stream codec that accepts empty stacks.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);

        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            int amount = buf.readVarInt();
            if (amount <= 0) {
                return FluidStack.EMPTY;
            }
            Holder<Fluid> holder = FLUID_STREAM_CODEC.decode(buf);
            DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
            return new FluidStack(holder, amount, patch);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                buf.writeVarInt(0);
            } else {
                buf.writeVarInt(stack.getAmount());
                FLUID_STREAM_CODEC.encode(buf, stack.getFluidHolder());
                DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
            }
        }
    };

    /**
     * Stream codec that does not accept empty stacks.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            FluidStack stack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
            if (stack.isEmpty()) {
                throw new DecoderException("Empty FluidStack not allowed");
            }
            return stack;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            if (stack.isEmpty()) {
                throw new EncoderException("Empty FluidStack not allowed");
            }
            FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
    };

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final FluidStack EMPTY = new FluidStack((Void) null);

    private int amount;
    private final Fluid fluid;
    private final PatchedDataComponentMap components;

    public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
        this(fluid.value(), amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
    }

    public FluidStack(Holder<Fluid> fluid, int amount) {
        this(fluid.value(), amount);
    }

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    private FluidStack(Fluid fluid, int amount, PatchedDataComponentMap components) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components;
    }

    private FluidStack(@Nullable Void unused) {
        this.fluid = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    @Override
    public PatchedDataComponentMap getComponents() {
        return components;
    }

    public DataComponentPatch getComponentsPatch() {
        return this.isEmpty() ? DataComponentPatch.EMPTY : this.components.asPatch();
    }

    public boolean isComponentsPatchEmpty() {
        //Note: vanilla PatchedDataComponentMap has no isPatchEmpty (NeoForge patch); asPatch().isEmpty() is equivalent
        return this.isEmpty() || this.components.asPatch().isEmpty();
    }

    /**
     * Tries to parse a fluid stack. Empty stacks cannot be parsed with this method.
     */
    public static Optional<FluidStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> LOGGER.error("Tried to load invalid fluid: '{}'", error));
    }

    /**
     * Tries to parse a fluid stack, defaulting to {@link #EMPTY} on parsing failure.
     */
    public static FluidStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    public boolean isEmpty() {
        return this == EMPTY || this.fluid == Fluids.EMPTY || this.amount <= 0;
    }

    /**
     * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
     */
    public FluidStack split(int amount) {
        int i = Math.min(amount, this.amount);
        FluidStack fluidStack = this.copyWithAmount(i);
        this.shrink(i);
        return fluidStack;
    }

    /**
     * Creates a copy of this stack, then sets this stack's amount to {@code 0}.
     */
    public FluidStack copyAndClear() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        FluidStack fluidStack = this.copy();
        this.setAmount(0);
        return fluidStack;
    }

    public Fluid getFluid() {
        return this.isEmpty() ? Fluids.EMPTY : this.fluid;
    }

    public Holder<Fluid> getFluidHolder() {
        return this.getFluid().builtInRegistryHolder();
    }

    public boolean is(TagKey<Fluid> tag) {
        return this.getFluid().builtInRegistryHolder().is(tag);
    }

    public boolean is(Fluid fluid) {
        return this.getFluid() == fluid;
    }

    public boolean is(Predicate<Holder<Fluid>> holderPredicate) {
        return holderPredicate.test(this.getFluidHolder());
    }

    public boolean is(Holder<Fluid> holder) {
        return is(holder.value());
    }

    public boolean is(HolderSet<Fluid> holderSet) {
        return holderSet.contains(this.getFluidHolder());
    }

    public Stream<TagKey<Fluid>> getTags() {
        return this.getFluid().builtInRegistryHolder().tags();
    }

    /**
     * Saves this stack to a tag, directly writing the keys into the passed tag.
     *
     * @throws IllegalStateException if this stack is empty
     */
    public Tag save(HolderLookup.Provider lookupProvider, Tag prefix) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix)
              .getOrThrow(error -> new IllegalStateException("Failed to encode FluidStack " + this + ": " + error));
    }

    /**
     * Saves this stack to a new tag.
     *
     * @throws IllegalStateException if this stack is empty
     */
    public Tag save(HolderLookup.Provider lookupProvider) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this)
              .getOrThrow(error -> new IllegalStateException("Failed to encode FluidStack " + this + ": " + error));
    }

    /**
     * Saves this stack to a new tag. Empty stacks are saved as an empty tag.
     */
    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return this.isEmpty() ? new CompoundTag() : this.save(lookupProvider, new CompoundTag());
    }

    public FluidStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        return new FluidStack(this.fluid, this.amount, this.components.copy());
    }

    public FluidStack copyWithAmount(int amount) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        FluidStack fluidStack = this.copy();
        fluidStack.setAmount(amount);
        return fluidStack;
    }

    /**
     * Checks fluid, amount, and components.
     */
    public static boolean matches(FluidStack first, FluidStack second) {
        if (first == second) {
            return true;
        }
        return first.getAmount() == second.getAmount() && isSameFluidSameComponents(first, second);
    }

    /**
     * Checks the fluid only, ignoring amount and components.
     */
    public static boolean isSameFluid(FluidStack first, FluidStack second) {
        return first.is(second.getFluid());
    }

    /**
     * Checks fluid and components, ignoring amount.
     */
    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        if (!first.is(second.getFluid())) {
            return false;
        }
        return (first.isEmpty() && second.isEmpty()) || Objects.equals(first.components, second.components);
    }

    //Note: typo preserved from NeoForge for source compatibility
    public static MapCodec<FluidStack> lenientOtionalFieldOf(String fieldName) {
        return CODEC.lenientOptionalFieldOf(fieldName)
              .xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    /**
     * Hashes the fluid and components of this stack, ignoring the amount.
     */
    public static int hashFluidAndComponents(@Nullable FluidStack stack) {
        if (stack == null) {
            return 0;
        }
        int i = 31 + stack.getFluid().hashCode();
        return 31 * i + stack.getComponents().hashCode();
    }

    @Override
    public String toString() {
        return this.getAmount() + " " + this.getFluid();
    }

    @Nullable
    @Override
    public <T> T set(DataComponentType<? super T> type, @Nullable T component) {
        return this.components.set(type, component);
    }

    @Nullable
    @Override
    public <T> T remove(DataComponentType<? extends T> type) {
        return this.components.remove(type);
    }

    @Override
    public void applyComponents(DataComponentPatch patch) {
        this.components.applyPatch(patch);
    }

    @Override
    public void applyComponents(DataComponentMap components) {
        this.components.setAll(components);
    }

    /**
     * Returns the hover name of this stack, via Fabric's fluid variant attributes
     * (NeoForge routes this through FluidType).
     */
    public Component getHoverName() {
        return FluidVariantAttributes.getName(FluidVariant.of(getFluid(), getComponentsPatch()));
    }

    /**
     * @deprecated Use {@link #getHoverName} instead.
     */
    @Deprecated(forRemoval = true)
    public Component getDisplayName() {
        return getHoverName();
    }

    public int getAmount() {
        return this.isEmpty() ? 0 : this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Limits the amount of this stack to at most the given amount.
     */
    public void limitSize(int amount) {
        if (!this.isEmpty() && this.getAmount() > amount) {
            this.setAmount(amount);
        }
    }

    public void grow(int addedAmount) {
        this.setAmount(this.getAmount() + addedAmount);
    }

    public void shrink(int removedAmount) {
        this.grow(-removedAmount);
    }

    /**
     * Determines if the fluid and the components are equal. This does not check amounts.
     *
     * @deprecated Use {@link #isSameFluidSameComponents} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean isFluidEqual(FluidStack other) {
        return isSameFluidSameComponents(this, other);
    }

    /**
     * @deprecated Use {@link #matches} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean isFluidStackIdentical(FluidStack other) {
        return matches(this, other);
    }

    /**
     * @return true if this FluidStack contains the other FluidStack (same fluid, same components, >= amount)
     */
    @Deprecated(forRemoval = true)
    public boolean containsFluid(FluidStack other) {
        return isFluidEqual(other) && amount >= other.amount;
    }
}
