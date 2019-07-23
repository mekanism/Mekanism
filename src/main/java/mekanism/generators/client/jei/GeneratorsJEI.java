package mekanism.generators.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class GeneratorsJEI implements IModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistry registry) {
        //registry.registerSubtypeInterpreter(Item.getItemFromBlock(GeneratorsBlocks.Generator), MekanismJEI.NBT_INTERPRETER);
        //TODO: As needed
    }
}