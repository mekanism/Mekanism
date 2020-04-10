/*package mekanism.common.integration.crafttweaker.commands;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.impl.commands.CTCommands.CommandImpl;
import java.util.Collection;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.text.EnumColor;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.text.ITextComponent;

public class GasesCommand extends CommandImpl {

    public GasesCommand() {
        super("gases", "Outputs a list of all registered gases to the crafttweaker.log", context -> {
            CraftTweakerAPI.logInfo("Gases:");
            Collection<Gas> gases = MekanismAPI.GAS_REGISTRY.getValues();
            //TODO: Should this be something other than translation key
            gases.forEach(gas -> CraftTweakerAPI.logInfo(String.format("<gas:%s>, %s", gas.getName(), gas.getTranslationKey())));

            ITextComponent message = TextComponentUtil.build(EnumColor.BRIGHT_GREEN, "List of " + gases.size() + " gases generated! Check the crafttweaker.log file!");
            context.getSource().sendFeedback(message, true);
            CraftTweakerAPI.logInfo(message.getFormattedText());
            return 0;
        });
    }
}*/