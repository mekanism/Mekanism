package mekanism.api.chemical.merged;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
@NothingNullByDefault
public class BoxedChemical implements IHasTextComponent {

    /**
     * Empty Boxed Chemical instance.
     */
    public static final BoxedChemical EMPTY = new BoxedChemical(ChemicalType.GAS, MekanismAPI.EMPTY_GAS);
    //TODO - 1.20.5: Docs
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxedChemical> STREAM_CODEC = StreamCodec.ofMember(BoxedChemical::write, BoxedChemical::read);

    /**
     * Boxes a Chemical.
     *
     * @param chemical Chemical to box.
     *
     * @return Boxed Chemical.
     */
    public static BoxedChemical box(Chemical<?> chemical) {
        if (chemical.isEmptyType()) {
            return EMPTY;
        }
        return new BoxedChemical(ChemicalType.getTypeFor(chemical), chemical);
    }

    /**
     * Reads a Boxed Chemical from a Packet Buffer.
     *
     * @param buffer Buffer.
     *
     * @return Boxed Chemical.
     */
    public static BoxedChemical read(FriendlyByteBuf buffer) {
        //TODO - 1.20.5: Deprecate this in favor of stream codecs?
        ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
        Chemical<?> c = switch (chemicalType) {
            case GAS -> buffer.readById(MekanismAPI.GAS_REGISTRY::byId);
            case INFUSION -> buffer.readById(MekanismAPI.INFUSE_TYPE_REGISTRY::byId);
            case PIGMENT -> buffer.readById(MekanismAPI.PIGMENT_REGISTRY::byId);
            case SLURRY -> buffer.readById(MekanismAPI.SLURRY_REGISTRY::byId);
        };
        if (c == null || c.isEmptyType()) {
            return EMPTY;
        }
        return new BoxedChemical(chemicalType, c);
    }

    /**
     * Reads a Boxed Chemical from a CompoundNBT.
     *
     * @param nbt NBT.
     *
     * @return Boxed Chemical.
     */
    public static BoxedChemical read(@Nullable CompoundTag nbt) {
        ChemicalType chemicalType = ChemicalType.fromNBT(nbt);
        if (chemicalType == null) {
            return EMPTY;
        }
        return new BoxedChemical(chemicalType, switch (chemicalType) {
            case GAS -> Gas.readFromNBT(nbt);
            case INFUSION -> InfuseType.readFromNBT(nbt);
            case PIGMENT -> Pigment.readFromNBT(nbt);
            case SLURRY -> Slurry.readFromNBT(nbt);
        });
    }

    private final ChemicalType chemicalType;
    private final Chemical<?> chemical;

    protected BoxedChemical(ChemicalType chemicalType, Chemical<?> chemical) {
        this.chemicalType = chemicalType;
        this.chemical = chemical;
    }

    /**
     * Gets whether this boxed chemical is empty.
     *
     * @return {@code true} if this boxed chemical is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this == EMPTY || chemical.isEmptyType();
    }

    /**
     * Gets the chemical type.
     */
    public ChemicalType getChemicalType() {
        return chemicalType;
    }

    /**
     * Writes this BoxedChemical to a defined tag compound.
     *
     * @param nbt - tag compound to write to
     *
     * @return tag compound with this BoxedChemical's data
     */
    public CompoundTag write(CompoundTag nbt) {
        chemicalType.write(nbt);
        chemical.write(nbt);
        return nbt;
    }

    /**
     * Writes this BoxedChemical to a Packet Buffer.
     *
     * @param buffer - Buffer to write to.
     */
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(chemicalType);
        switch (chemicalType) {
            case GAS -> buffer.writeById(MekanismAPI.GAS_REGISTRY::getId, (Gas) chemical);
            case INFUSION -> buffer.writeById(MekanismAPI.INFUSE_TYPE_REGISTRY::getId, (InfuseType) chemical);
            case PIGMENT -> buffer.writeById(MekanismAPI.PIGMENT_REGISTRY::getId, (Pigment) chemical);
            case SLURRY -> buffer.writeById(MekanismAPI.SLURRY_REGISTRY::getId, (Slurry) chemical);
        }
    }

    /**
     * Gets the internal chemical that was boxed.
     */
    public Chemical<?> getChemical() {
        return chemical;
    }

    @Override
    public Component getTextComponent() {
        return chemical.getTextComponent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BoxedChemical other = (BoxedChemical) o;
        return chemicalType == other.chemicalType && chemical == other.chemical;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chemicalType, chemical);
    }
}