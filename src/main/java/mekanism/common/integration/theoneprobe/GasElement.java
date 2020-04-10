package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import net.minecraft.network.PacketBuffer;

public class GasElement extends ChemicalElement<Gas, GasStack> {

    public static int ID;

    public GasElement(@Nonnull GasStack stored, long capacity) {
        super(stored, capacity);
    }

    public GasElement(PacketBuffer buf) {
        this(ChemicalUtils.readGasStack(buf), buf.readVarLong());
    }

    @Override
    protected ILangEntry getStoredFormat() {
        return MekanismLang.GENERIC_STORED_MB;
    }

    @Override
    public int getID() {
        return ID;
    }
}