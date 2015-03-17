package mekanism.common;

import java.util.List;

import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCHandler
{
	@EventHandler
	public void onIMCEvent(List<IMCMessage> messages)
	{
		for(IMCMessage msg : messages)
		{
			if(msg.isNBTMessage())
			{
				try {
					boolean found = false;
					
					for(Recipe type : Recipe.values())
					{
						if(msg.key.equalsIgnoreCase(type.getRecipeName() + "Recipe"))
						{
							MachineInput input = type.createInput(msg.getNBTValue());
							
							if(input != null && input.isValid())
							{
								MachineRecipe recipe = type.createRecipe(input, msg.getNBTValue());
								
								if(recipe != null && recipe.recipeOutput != null)
								{
									RecipeHandler.addRecipe(type, recipe);
									Mekanism.logger.info("[Mekanism] " + msg.getSender() + " added recipe of type " + type.getRecipeName() + " to the recipe list.");
								}
								else {
									Mekanism.logger.error("[Mekanism] " + msg.getSender() + " attempted to add recipe of type " + type.getRecipeName() + " with an invalid output.");
								}
							}
							else {
								Mekanism.logger.error("[Mekanism] " + msg.getSender() + " attempted to add recipe of type " + type.getRecipeName() + " with an invalid input.");
							}
							
							found = true;
							break;
						}
					}
					
					if(!found)
					{
						Mekanism.logger.error("[Mekanism] " + msg.getSender() + " sent unknown IMC message with key '" + msg.key + ".'");
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
