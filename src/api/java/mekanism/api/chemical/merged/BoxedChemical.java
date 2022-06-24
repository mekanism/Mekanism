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
import net.minecraft.network.chat.Component;
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
        ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
        return new BoxedChemical(chemicalType, switch (chemicalType) {
            case GAS -> buffer.readRegistryIdSafe(Gas.class);
            case INFUSION -> buffer.readRegistryIdSafe(InfuseType.class);
            case PIGMENT -> buffer.readRegistryIdSafe(Pigment.class);
            case SLURRY ->  buffer.readRegistryIdSafe(Slurry.class);
        });
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
            case GAS -> buffer.writeRegistryId(MekanismAPI.gasRegistry(), (Gas) chemical);
            case INFUSION -> buffer.writeRegistryId(MekanismAPI.infuseTypeRegistry(), (InfuseType) chemical);
            case PIGMENT -> buffer.writeRegistryId(MekanismAPI.pigmentRegistry(), (Pigment) chemical);
            case SLURRY -> buffer.writeRegistryId(MekanismAPI.slurryRegistry(), (Slurry) chemical);
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