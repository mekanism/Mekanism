package mekanism.common.recipe;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import mekanism.common.config_old.MekanismConfigOld;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

/**
 * Used as a condition in mekanism _factories.json
 *
 * WARNING: Only one of these values could apply!
 */
public class MekanismRecipeEnabledCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        //TODO
        /*if (JSONUtils.hasField(json, "machineType")) {
            String machineType = JSONUtils.getString(json, "machineType");
            final MachineType type = MekanismConfigOld.current().general.machinesManager.typeFromName(machineType);
            //TODO: Check config
            return () -> true;//() -> MekanismConfig.current().general.machinesManager.isEnabled(type);
        }

        if (ModList.get().isLoaded(MekanismGenerators.MODID) && JSONUtils.hasField(json, "generatorType")) {
            final String generatorType = JSONUtils.getString(json, "generatorType");
            final GeneratorType type = MekanismConfigOld.current().generators.generatorsManager.typeFromName(generatorType);
            //noinspection Convert2Lambda - classloading issues if generators not installed
            return new BooleanSupplier() {
                @Override
                public boolean getAsBoolean() {
                    //TODO: Check config
                    return true;//MekanismConfig.current().generators.generatorsManager.isEnabled(type);
                }
            };
        }*/

        if (JSONUtils.hasField(json, "circuitOredict")) {
            return () -> MekanismConfigOld.current().general.controlCircuitOreDict.get();
        }

        throw new IllegalStateException("Config defined with recipe_enabled condition without a valid field defined! Valid values: \"machineType\", \"generatorType\" "
                                        + "(when Mekanism Generators installed) and \"circuitOredict\"");
    }
}