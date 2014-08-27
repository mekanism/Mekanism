package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.client.sound.TestSound;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;

import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;

public class TileEntityCrusher extends TileEntityElectricMachine
{
	public TestSound sfx;

	public TileEntityCrusher()
	{
		super("Crusher.ogg", "Crusher", usage.crusherUsage, 200, MachineType.CRUSHER.baseEnergy);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(worldObj.isRemote)
		{
			if(isActive && sfx.isDonePlaying())
			{
				Mekanism.logger.info("Playing Crusher noise");
				sfx.finished = false;
				FMLClientHandler.instance().getClient().getSoundHandler().playSound(sfx);
			}
			else if(!(isActive || sfx.isDonePlaying()))
			{
				Mekanism.logger.info("Stopping Crusher noise");
				sfx.finished = true;
			}
		}
	}


	@Override
	public Map getRecipes()
	{
		return Recipe.CRUSHER.get();
	}

	@Override
	public float getVolumeMultiplier()
	{
		return 0.5F;
	}

	@Override
	public void validate()
	{
		super.validate();
		sfx = new TestSound(new ResourceLocation("mekanism", "tile.machine.crusher"), this);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		sfx.finished = true;
	}
}
