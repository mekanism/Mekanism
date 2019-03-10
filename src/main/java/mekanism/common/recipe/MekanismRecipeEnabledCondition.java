package mekanism.common.recipe;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.JsonUtils;
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
        if (JsonUtils.hasField(json, "machineType")) {
            return () -> MekanismConfig.general.machinesManager.isEnabled(JsonUtils.getString(json, "machineType"));
        }

        if (JsonUtils.hasField(json, "generatorType")) {
            return () -> MekanismConfig.generators.generatorsManager
                  .isEnabled(JsonUtils.getString(json, "generatorType"));
        }

        if (JsonUtils.hasField(json, "circuitOredict")) {
            return () -> MekanismConfig.general.controlCircuitOreDict;
        }

        throw new IllegalStateException(
              "Config defined with recipe_enabled condition without a valid field defined! Valid values: \"machineType\", \"generatorType\" and \"circuitOredict\"");
    }
}
