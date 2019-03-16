package mekanism.common.recipe;

import com.google.gson.JsonObject;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig;
import mekanism.generators.common.block.states.BlockStateGenerator;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

/**
 * Used as a condition in mekanism _factories.json
 *
 * WARNING: Only one of these values could apply!
 */
public class MekanismRecipeEnabledCondition implements IConditionFactory
{
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        if(JsonUtils.hasField(json, "machineType"))
        {
            String machineType = JsonUtils.getString(json, "machineType");
            final BlockStateMachine.MachineType type = MekanismConfig.current().general.machinesManager.typeFromName(machineType);
            return () -> MekanismConfig.current().general.machinesManager.isEnabled(type);
        }

        if(JsonUtils.hasField(json, "generatorType"))
        {
            final String generatorType = JsonUtils.getString(json, "generatorType");
            final BlockStateGenerator.GeneratorType type = MekanismConfig.current().generators.generatorsManager.typeFromName(generatorType);
            return () -> MekanismConfig.current().generators.generatorsManager.isEnabled(type);
        }

        if(JsonUtils.hasField(json, "circuitOredict"))
        {
            return () -> MekanismConfig.current().general.controlCircuitOreDict.val();
        }

        throw new IllegalStateException("Config defined with recipe_enabled condition without a valid field defined! Valid values: \"machineType\", \"generatorType\" and \"circuitOredict\"");
    }
}
