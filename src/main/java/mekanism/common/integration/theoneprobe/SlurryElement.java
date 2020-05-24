package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.PacketBuffer;

public class SlurryElement extends ChemicalElement<Slurry, SlurryStack> {

    public static int ID;

    public SlurryElement(@Nonnull SlurryStack stored, long capacity) {
        super(stored, capacity);
    }

    public SlurryElement(PacketBuffer buf) {
        this(ChemicalUtils.readSlurryStack(buf), buf.readVarLong());
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