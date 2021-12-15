package mekanism.api.chemical.merged;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

/**
 *
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoxedChemical implements IHasTextComponent {

    /**
     * Empty Boxed Chemical instance.
     */
    public static final BoxedChemical EMPTY = new BoxedChemical(ChemicalType.GAS, MekanismAPI.EMPTY_GAS);

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
    @SuppressWarnings("RedundantCast")
    public static BoxedChemical read(PacketBuffer buffer) {
        //Note: Casts are needed for compiling, so it knows how to read it properly
        ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
        if (chemicalType == ChemicalType.GAS) {
            return new BoxedChemical(chemicalType, (Gas) buffer.readRegistryId());
        } else if (chemicalType == ChemicalType.INFUSION) {
            return new BoxedChemical(chemicalType, (InfuseType) buffer.readRegistryId());
        } else if (chemicalType == ChemicalType.PIGMENT) {
            return new BoxedChemical(chemicalType, (Pigment) buffer.readRegistryId());
        } else if (chemicalType == ChemicalType.SLURRY) {
            return new BoxedChemical(chemicalType, (Slurry) buffer.readRegistryId());
        } else {
            throw new IllegalStateException("Unknown chemical type");
        }
    }

    /**
     * Reads a Boxed Chemical from a CompoundNBT.
     *
     * @param nbt NBT.
     *
     * @return Boxed Chemical.
     */
    public static BoxedChemical read(@Nullable CompoundNBT nbt) {
        ChemicalType chemicalType = ChemicalType.fromNBT(nbt);
        Chemical<?> chemical = null;
        if (chemicalType == ChemicalType.GAS) {
            chemical = Gas.readFromNBT(nbt);
        } else if (chemicalType == ChemicalType.INFUSION) {
            chemical = InfuseType.readFromNBT(nbt);
        } else if (chemicalType == ChemicalType.PIGMENT) {
            chemical = Pigment.readFromNBT(nbt);
        } else if (chemicalType == ChemicalType.SLURRY) {
            chemical = Slurry.readFromNBT(nbt);
        }
        return chemicalType == null || chemical == null ? EMPTY : new BoxedChemical(chemicalType, chemical);
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
    public CompoundNBT write(CompoundNBT nbt) {
        chemicalType.write(nbt);
        chemical.write(nbt);
        return nbt;
    }

    /**
     * Writes this BoxedChemical to a Packet Buffer.
     *
     * @param buffer - Buffer to write to.
     */
    public void write(PacketBuffer buffer) {
        buffer.writeEnum(chemicalType);
        if (chemicalType == ChemicalType.GAS) {
            buffer.writeRegistryId((Gas) chemical);
        } else if (chemicalType == ChemicalType.INFUSION) {
            buffer.writeRegistryId((InfuseType) chemical);
        } else if (chemicalType == ChemicalType.PIGMENT) {
            buffer.writeRegistryId((Pigment) chemical);
        } else if (chemicalType == ChemicalType.SLURRY) {
            buffer.writeRegistryId((Slurry) chemical);
        } else {
            throw new IllegalStateException("Unknown chemical type");
        }
    }

    /**
     * Gets the internal chemical that was boxed.
     */
    public Chemical<?> getChemical() {
        return chemical;
    }

    @Override
    public ITextComponent getTextComponent() {
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