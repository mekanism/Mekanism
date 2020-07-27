package mekanism.common.integration.lookingat.theoneprobe;

import javax.annotation.Nonnull;
import mcjty.theoneprobe.api.IElement;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.lookingat.ChemicalElement;
import net.minecraft.network.PacketBuffer;

public abstract class TOPChemicalElement extends ChemicalElement implements IElement {

    protected TOPChemicalElement(@Nonnull ChemicalStack<?> stored, long capacity) {
        super(stored, capacity);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        ChemicalUtils.writeChemicalStack(buf, stored);
        buf.writeVarLong(capacity);
    }

    public static class GasElement extends TOPChemicalElement {

        public GasElement(@Nonnull GasStack stored, long capacity) {
            super(stored, capacity);
        }

        public GasElement(PacketBuffer buf) {
            this(ChemicalUtils.readGasStack(buf), buf.readVarLong());
        }

        @Override
        public int getID() {
            return TOPProvider.GAS_ELEMENT_ID;
        }
    }

    public static class InfuseTypeElement extends TOPChemicalElement {

        public InfuseTypeElement(@Nonnull InfusionStack stored, long capacity) {
            super(stored, capacity);
        }

        public InfuseTypeElement(PacketBuffer buf) {
            this(ChemicalUtils.readInfusionStack(buf), buf.readVarLong());
        }

        @Override
        public int getID() {
            return TOPProvider.INFUSION_ELEMENT_ID;
        }
    }

    public static class PigmentElement extends TOPChemicalElement {

        public PigmentElement(@Nonnull PigmentStack stored, long capacity) {
            super(stored, capacity);
        }

        public PigmentElement(PacketBuffer buf) {
            this(ChemicalUtils.readPigmentStack(buf), buf.readVarLong());
        }

        @Override
        public int getID() {
            return TOPProvider.PIGMENT_ELEMENT_ID;
        }
    }

    public static class SlurryElement extends TOPChemicalElement {

        public SlurryElement(@Nonnull SlurryStack stored, long capacity) {
            super(stored, capacity);
        }

        public SlurryElement(PacketBuffer buf) {
            this(ChemicalUtils.readSlurryStack(buf), buf.readVarLong());
        }

        @Override
        public int getID() {
            return TOPProvider.SLURRY_ELEMENT_ID;
        }
    }
}