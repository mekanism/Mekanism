package mekanism.common.base;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.common.tier.FluidTankTier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CreativeFluidTank extends FluidTank {

    public CreativeFluidTank() {
        super(FluidTankTier.CREATIVE.getStorage());
    }

    public CreativeFluidTank(Predicate<FluidStack> validator) {
        super(FluidTankTier.CREATIVE.getStorage(), validator);
    }

    @Override
    public FluidTank setCapacity(int capacity) {
        //NO-OP this
        return this;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (action.execute()) {
            if (resource.isEmpty() || !isFluidValid(resource)) {
                return 0;
            }
            if (isEmpty()) {
                //If we are actually empty and are calling fill set the tank to being full of that type
                setFluid(new FluidStack(resource, getCapacity()));
                return resource.getAmount();
            }
        }
        //Simulate the filling as we are creative and don't actually want the contents to change
        return super.fill(resource, FluidAction.SIMULATE);
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        //Simulate the draining as we are creative and don't actually want the contents to change
        return super.drain(resource, FluidAction.SIMULATE);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        //Simulate the draining as we are creative and don't actually want the contents to change
        return super.drain(maxDrain, FluidAction.SIMULATE);
    }
}