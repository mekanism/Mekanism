package mekanism.api.chemical.merged;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.nbt.CompoundNBT;

//TODO: Make the chemicals know their own chemical type
public enum ChemicalType {
    GAS(c -> c instanceof Gas),
    INFUSION(c -> c instanceof InfuseType),
    PIGMENT(c -> c instanceof Pigment),
    SLURRY(c -> c instanceof Slurry);

    private final Predicate<Chemical<?>> instanceCheck;

    ChemicalType(Predicate<Chemical<?>> instanceCheck) {
        this.instanceCheck = instanceCheck;
    }

    public boolean isInstance(Chemical<?> chemical) {
        return instanceCheck.test(chemical);
   }

   public void write(CompoundNBT nbt) {
        //TODO: IMPLEMENT
   }

    @Nullable
    public static ChemicalType fromNBT(CompoundNBT nbt) {
        //TODO: IMPLEMENT
        return null;
    }

    public static ChemicalType getTypeFor(Chemical<?> chemical) {
        if (chemical instanceof Gas) {
            return GAS;
        } else if (chemical instanceof InfuseType) {
            return INFUSION;
        } else if (chemical instanceof Pigment) {
            return PIGMENT;
        } else if (chemical instanceof Slurry) {
            return SLURRY;
        }
        throw new IllegalStateException("Unknown chemical type");
    }

    public static ChemicalType getTypeFor(ChemicalStack<?> stack) {
        return getTypeFor(stack.getType());
    }
}