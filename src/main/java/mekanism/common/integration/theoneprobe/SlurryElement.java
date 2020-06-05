package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.network.PacketBuffer;

public class SlurryElement extends ChemicalElement {

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