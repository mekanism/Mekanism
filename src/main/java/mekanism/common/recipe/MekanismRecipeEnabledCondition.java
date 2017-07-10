package mekanism.common.recipe;

import com.google.gson.JsonObject;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class MekanismRecipeEnabledCondition implements IConditionFactory
{
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        String machineType = JsonUtils.getString(json, "machineType");
        if(!machineType.isEmpty())
        {
            return () -> MekanismConfig.general.machinesManager.isEnabled(machineType);
        }

        String generatorType = JsonUtils.getString(json, "generatorType");
        if(!generatorType.isEmpty())
        {
            return () -> MekanismConfig.generators.generatorsManager.isEnabled(generatorType);
        }

        throw new IllegalStateException("Config defined with machine_enabled condition without a machine/generator type!");
    }
}
