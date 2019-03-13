package mekanism.client.jei.machine.other;

import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetallurgicInfuserRecipeCategory extends BaseRecipeCategory
{
	private final IDrawable background;

	@Nullable
	private MetallurgicInfuserRecipe tempRecipe;
	
	public MetallurgicInfuserRecipeCategory(IGuiHelper helper)
	{
		super(helper, "mekanism:gui/GuiMetallurgicInfuser.png", "metallurgic_infuser", "tile.MachineBlock.MetallurgicInfuser.name", ProgressBar.MEDIUM);

		xOffset = 5;
		yOffset = 16;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 166, 54);
	}
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(new GuiSlot(SlotType.EXTRA, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 16, 34));
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 50, 42));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 142, 34).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 108, 42));

		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return 1F;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 164, 15));
		guiElements.add(new GuiProgress(new IProgressInfoHandler() {
			@Override
			public double getProgress()
			{
				return (double)timer.getValue() / 20F;
			}
		}, ProgressBar.MEDIUM, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 70, 46));
	}
	
	public static List<ItemStack> getInfuseStacks(InfuseType type)
	{
		List<ItemStack> ret = new ArrayList<>();

		for(Map.Entry<ItemStack, InfuseObject> obj : InfuseRegistry.getObjectMap().entrySet())
		{
			if(obj.getValue().type == type)
			{
				ret.add(obj.getKey());
			}
		}

		return ret;
	}
	
	@Override
	public IDrawable getBackground()
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		if(!(recipeWrapper instanceof MetallurgicInfuserRecipeWrapper))
		{
			return;
		}

		tempRecipe = ((MetallurgicInfuserRecipeWrapper)recipeWrapper).getRecipe();
		
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		
		itemStacks.init(0, true, 45, 26);
		itemStacks.init(1, false, 103, 26);
		itemStacks.init(2, true, 11, 18);

		itemStacks.set(0, tempRecipe.getInput().inputStack);
		itemStacks.set(1, tempRecipe.getOutput().output);
		itemStacks.set(2, getInfuseStacks(tempRecipe.getInput().infuse.type));
	}
}
