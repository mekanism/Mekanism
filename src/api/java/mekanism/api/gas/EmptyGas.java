package mekanism.api.gas;

import mekanism.api.MekanismAPI;
import net.minecraft.util.ResourceLocation;

//TODO: Override things so people cannot modify the empty gas, or set fluids etc
public final class EmptyGas extends Gas {

    public EmptyGas() {
        super(new ResourceLocation(MekanismAPI.API_VERSION, "empty_gas"), -1);
    }
}