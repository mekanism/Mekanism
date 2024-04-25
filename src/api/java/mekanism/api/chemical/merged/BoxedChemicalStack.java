package mekanism.api.chemical.merged;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class BoxedChemicalStack implements IHasTextComponent {

    /**
     * Empty Boxed Chemical Stack instance.
     */
    public static final BoxedChemicalStack EMPTY = new BoxedChemicalStack(ChemicalType.GAS, GasStack.EMPTY);

    //TODO - 1.20.5: Docs
    //TODO - 1.20.5: Do we want Codec variant of OPTIONAL_CODEC?
    public static final Codec<BoxedChemicalStack> CODEC = ChemicalStack.BOXED_CODEC.xmap(BoxedChemicalStack::box, BoxedChemicalStack::getChemicalStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxedChemicalStack> STREAM_CODEC = ChemicalStack.BOXED_STREAM_CODEC.map(BoxedChemicalStack::box, BoxedChemicalStack::getChemicalStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxedChemicalStack> OPTIONAL_STREAM_CODEC = ChemicalStack.BOXED_OPTIONAL_STREAM_CODEC.map(BoxedChemicalStack::box, BoxedChemicalStack::getChemicalStack);

    /**
     * Boxes a Chemical Stack.
     *
     * @param chemicalStack Chemical Stack to box.
     *
     * @return Boxed Chemical Stack.
     */
    public static BoxedChemicalStack box(ChemicalStack<?> chemicalStack) {
        if (chemicalStack.isEmpty()) {
            //TODO: Do we care that this can lose the type of chemical as it uses a single instance that always has a type of gas?
            return EMPTY;
        }
        return new BoxedChemicalStack(ChemicalType.getTypeFor(chemicalStack), chemicalStack);
    }

    /**
     * Tries to parse a boxed chemical stack. Empty stacks cannot be parsed with this method.
     *
     * @since 10.6.0
     */
    public static Optional<BoxedChemicalStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(error -> MekanismAPI.logger.error("Tried to load invalid boxed chemical: '{}'", error));
    }

    /**
     * Tries to parse a boxed chemical stack, defaulting to {@link #EMPTY} on parsing failure.
     *
     * @since 10.6.0
     */
    public static BoxedChemicalStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    private final ChemicalType chemicalType;
    private final ChemicalStack<?> chemicalStack;

    private BoxedChemicalStack(ChemicalType chemicalType, ChemicalStack<?> chemicalStack) {
        this.chemicalType = chemicalType;
        this.chemicalStack = chemicalStack;
    }

    /**
     * Gets the boxed type of this stack.
     */
    public BoxedChemical getType() {
        if (isEmpty()) {
            return BoxedChemical.EMPTY;
        }
        return new BoxedChemical(chemicalType, chemicalStack.getChemical());
    }

    /**
     * Gets the chemical type of this stack.
     */
    public ChemicalType getChemicalType() {
        return chemicalType;
    }

    /**
     * Gets whether this boxed chemical stack is empty.
     *
     * @return {@code true} if this stack is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this == EMPTY || chemicalStack.isEmpty();
    }

    /**
     * Saves this stack to a tag, directly writing the keys into the passed tag.
     *
     * @throws IllegalStateException if this stack is empty
     * @since 10.6.0
     */
    public Tag save(Provider lookupProvider, Tag prefix) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty BoxedChemicalStack");
        }
        return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
    }

    /**
     * Saves this stack to a new tag.
     *
     * @throws IllegalStateException if this stack is empty
     * @since 10.6.0
     */
    public Tag save(Provider lookupProvider) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot encode empty BoxedChemicalStack");
        }
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    /**
     * Saves this stack to a new tag. Empty stacks are supported and will be saved as an empty tag.
     */
    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return isEmpty() ? new CompoundTag() : save(lookupProvider);
    }

    /**
     * Gets the internal chemical stack that was boxed.
     */
    public ChemicalStack<?> getChemicalStack() {
        return chemicalStack;
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return chemicalStack.getTextComponent();
    }

    /**
     * Copies this boxed chemical stack into a new boxed chemical stack.
     */
    public BoxedChemicalStack copy() {
        return new BoxedChemicalStack(chemicalType, chemicalStack.copy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BoxedChemicalStack other = (BoxedChemicalStack) o;
        return chemicalType == other.chemicalType && chemicalStack.equals(other.chemicalStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chemicalType, chemicalStack);
    }
}