package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.PacketBuffer;

public class PigmentElement extends ChemicalElement<Pigment, PigmentStack> {

    public static int ID;

    public PigmentElement(@Nonnull PigmentStack stored, long capacity) {
        super(stored, capacity);
    }

    public PigmentElement(PacketBuffer buf) {
        this(ChemicalUtils.readPigmentStack(buf), buf.readVarLong());
    }

    @Override
    protected ILangEntry getStoredFormat() {
        return MekanismLang.GENERIC_STORED;
    }

    @Override
    public int getID() {
        return ID;
    }
}