package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.PigmentStack;
import net.minecraft.network.PacketBuffer;

public class PigmentElement extends ChemicalElement {

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