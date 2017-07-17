package mekanism.common.integration;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.recipe.ShapelessMekanismRecipe;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.util.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class IMCHandler
{
	public void onIMCEvent(List<IMCMessage> messages)
	{
		for(IMCMessage msg : messages)
		{
			if(msg.isNBTMessage())
			{
				try {
					boolean found = false;
					boolean delete = false;
					
					String message = msg.key;
					
					if(message.equals("ShapedMekanismRecipe"))
					{
						ShapedMekanismRecipe recipe = ShapedMekanismRecipe.create(msg.getNBTValue());
						
						if(recipe != null)
						{
							GameRegistry.addRecipe(recipe);
							Mekanism.logger.info("[Mekanism] " + msg.getSender() + " added a shaped recipe to the recipe list.");
						}
						else {
							Mekanism.logger.error("[Mekanism] " + msg.getSender() + " attempted to add an invalid shaped recipe.");
						}
						
						found = true;
					}
					else if(message.equals("ShapelessMekanismRecipe"))
					{
						ShapelessMekanismRecipe recipe = ShapelessMekanismRecipe.create(msg.getNBTValue());
						
						if(recipe != null)
						{
							GameRegistry.addRecipe(recipe);
							Mekanism.logger.info("[Mekanism] " + msg.getSender() + " added a shapeless recipe to the recipe list.");
						}
						else {
							Mekanism.logger.error("[Mekanism] " + msg.getSender() + " attempted to add an invalid shapeless recipe.");
						}
						
						found = true;
					}
					else if(message.equals("DeleteMekanismRecipes") || message.equals("RemoveMekanismRecipes"))
					{
						ItemStack stack = RecipeUtils.loadRecipeItemStack(msg.getNBTValue());
						
						if(!stack.isEmpty())
						{
							RecipeUtils.removeRecipes(stack);
							Mekanism.logger.info("[Mekanism] " + msg.getSender() + " removed a Mekanism recipe from the recipe list.");
						}
						else {
							Mekanism.logger.error("[Mekanism] " + msg.getSender() + " attempted to remove a Mekanism recipe with an invalid output.");
						}
						
						found = true;
					}

					if(message.startsWith("Delete") || message.startsWith("Remove"))
					{
						message = message.replace("Delete", "").replace("Remove", "");
						delete = true;
					}

					for(Recipe type : Recipe.values())
					{
						if(message.equalsIgnoreCase(type.getRecipeName() + "Recipe"))
						{
							MachineInput input = type.createInput(msg.getNBTValue());
							
							if(input != null && input.isValid())
							{
								MachineRecipe recipe = type.createRecipe(input, msg.getNBTValue());
								
								if(recipe != null && recipe.recipeOutput != null)
								{
									if(delete)
									{
										RecipeHandler.removeRecipe(type, recipe);
										Mekanism.logger.info("[Mekanism] " + msg.getSender() + " removed recipe of type " + type.getRecipeName() + " from the recipe list.");
									}
									else {
										RecipeHandler.addRecipe(type, recipe);
										Mekanism.logger.info("[Mekanism] " + msg.getSender() + " added recipe of type " + type.getRecipeName() + " to the recipe list.");
									}
								}
								else {
									Mekanism.logger.error("[Mekanism] " + msg.getSender() + " attempted to " + (delete ? "remove" : "add") + " recipe of type " + type.getRecipeName() + " with an invalid output.");
								}
							}
							else {
								Mekanism.logger.error("[Mekanism] " + msg.getSender() + " attempted to " + (delete ? "remove" : "add") + " recipe of type " + type.getRecipeName() + " with an invalid input.");
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
