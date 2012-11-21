package railcraft.common.api.fuel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraftforge.liquids.LiquidStack;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class FuelManager
{

    public static final Map<LiquidStack, Integer> boilerFuel = new HashMap<LiquidStack, Integer>();

    /**
     * Register the amount of heat in a bucket of liquid fuel.
     *
     * @param liquid
     * @param heatValuePerBucket
     */
    public static void addBoilerFuel(LiquidStack liquid, int heatValuePerBucket) {
        boilerFuel.put(liquid, heatValuePerBucket);
    }

    public static int getBoilerFuelValue(LiquidStack liquid) {
        for(Entry<LiquidStack, Integer> entry : boilerFuel.entrySet()) {
            if(entry.getKey().isLiquidEqual(liquid)) {
                return entry.getValue();
            }
        }
        return 0;
    }
}
