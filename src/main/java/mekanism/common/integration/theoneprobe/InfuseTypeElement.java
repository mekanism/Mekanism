/*package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.infuse.InfusionStack;
import net.minecraft.network.PacketBuffer;

public class InfuseTypeElement extends ChemicalElement {

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
}*/