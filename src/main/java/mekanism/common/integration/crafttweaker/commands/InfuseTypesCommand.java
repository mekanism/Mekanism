package mekanism.common.integration.crafttweaker.commands;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.impl.commands.CTCommands.CommandImpl;
import java.util.Set;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.text.EnumColor;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.text.ITextComponent;

public class InfuseTypesCommand extends CommandImpl {

    public InfuseTypesCommand() {
        super("infuseTypes", "Outputs a list of all registered metallurgic infuser infusion types to the crafttweaker.log", context -> {
            CraftTweakerAPI.logInfo("Infuse Types:");
            Set<String> names = InfuseRegistry.getInfuseMap().keySet();
            //TODO: When Infuse Types are proper objects make this return the bracket handlers
            names.forEach(CraftTweakerAPI::logInfo);
            ITextComponent message = TextComponentUtil.build(EnumColor.BRIGHT_GREEN,
                  "List of " + names.size() + " metallurgic infuser infusion types generated! Check the crafttweaker.log file!");
            context.getSource().sendFeedback(message, true);
            CraftTweakerAPI.logInfo(message.getFormattedText());
            return 0;
        });
    }
}