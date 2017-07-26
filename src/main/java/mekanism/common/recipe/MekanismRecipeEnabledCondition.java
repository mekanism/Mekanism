package mekanism.common.recipe;

import com.google.gson.JsonObject;
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
        if(JsonUtils.hasField(json, "machineType"))
        {
            return () -> MekanismConfig.general.machinesManager.isEnabled(JsonUtils.getString(json, "machineType"));
        }

        if(JsonUtils.hasField(json, "generatorType"))
        {
            return () -> MekanismConfig.generators.generatorsManager.isEnabled(JsonUtils.getString(json, "generatorType"));
        }

        throw new IllegalStateException("Config defined with machine_enabled condition without a machine/generator type!");
    }
}
