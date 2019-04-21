package mekanism.common.base;

import java.util.Map.Entry;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidHandlerTarget extends Target<IFluidHandler, Integer> {

    private FluidStack type;

    public FluidHandlerTarget(FluidStack type) {
        this.type = type;
    }

    @Override
    public Integer sendGivenWithDefault(Integer amountPer) {
        int sent = 0;
        for (Entry<EnumFacing, Integer> giveInfo : given.entrySet()) {
            sent += acceptAmount(giveInfo.getKey(), giveInfo.getValue());
        }
        //If needed is not empty then we default it to the given calculated fair split amount of remaining energy
        for (EnumFacing side : needed.keySet()) {
            sent += acceptAmount(side, amountPer);
        }
        return sent;
    }

    private int acceptAmount(EnumFacing side, int amount) {
        //Give it fluid and add how much actually got accepted instead of how much
        // we attempted to give it
        return wrappers.get(side).fill(PipeUtils.copy(type, amount), true);
    }
}