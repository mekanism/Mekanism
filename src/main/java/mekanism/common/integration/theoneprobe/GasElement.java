package mekanism.common.integration.theoneprobe;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.network.PacketBuffer;

public class GasElement extends ChemicalElement {

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