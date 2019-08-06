package mekanism.common.recipe;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import mekanism.common.block.states.MachineType;
import mekanism.common.config.MekanismConfig;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.states.GeneratorType;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;

/**
 * Used as a condition in mekanism _factories.json
 *
 * WARNING: Only one of these values could apply!
 */
public class MekanismRecipeEnabledCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        if (JSONUtils.hasField(json, "machineType")) {
            String machineType = JSONUtils.getString(json, "machineType");
            final MachineType type = MekanismConfig.current().general.machinesManager.typeFromName(machineType);
            //TODO: Check config
            return () -> true;//() -> MekanismConfig.current().general.machinesManager.isEnabled(type);
        }

        if (Loader.isModLoaded(MekanismGenerators.MODID) && JSONUtils.hasField(json, "generatorType")) {
            final String generatorType = JSONUtils.getString(json, "generatorType");
            final GeneratorType type = MekanismConfig.current().generators.generatorsManager.typeFromName(generatorType);
            //noinspection Convert2Lambda - classloading issues if generators not installed
            return new BooleanSupplier() {
                @Override
                public boolean getAsBoolean() {
                    //TODO: Check config
                    return true;//MekanismConfig.current().generators.generatorsManager.isEnabled(type);
                }
            };
        }

        if (JSONUtils.hasField(json, "circuitOredict")) {
            return () -> MekanismConfig.current().general.controlCircuitOreDict.val();
        }

        throw new IllegalStateException("Config defined with recipe_enabled condition without a valid field defined! Valid values: \"machineType\", \"generatorType\" "
                                        + "(when Mekanism Generators installed) and \"circuitOredict\"");
    }
}