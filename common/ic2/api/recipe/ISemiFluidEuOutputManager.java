package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;


public interface ISemiFluidEuOutputManager {

    /*
    fluid =  register in FluidRegistry
    amount = Value of burn pro Tick (20Tick = 1sek)
    eu = eu output pro Tick if burn (20Tick = 1sek)
     */
    void addFluid(Fluid fluid, int amount , int eu);

    int[] getconsumption(Fluid fluid);

    Fluid[] getacceptsFluids();

    Map<Fluid, int[]> getFluidConsumptionMap();

}
