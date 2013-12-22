package mekanism.client.sound;

import mekanism.api.Pos3D;
import mekanism.common.IActiveState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Sound -- an object that is created in SoundHandler. A 'Sound' object runs off of
 * PaulsCode's SoundSystem. It has several methods; play(), for looping the clip,
 * stop(), for stopping the loop, remove(), for removing the sound from PaulsCode,
 * and updateVolume() for updating the volume based on where the player is.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class TileSound extends Sound
{
	/** The TileEntity this sound is associated with. */
	public TileEntity tileEntity;
	
	/**
	 * A sound that runs off of the PaulsCode sound system.
	 * @param id - unique identifier
	 * @param sound - bundled path to the sound
	 * @param tileentity - the tile this sound is playing from.
	 */
	public TileSound(String id, String sound, TileEntity tileentity)
	{
		super(id, sound, tileentity, new Pos3D(tileentity));
		
		tileEntity = tileentity;
	}
	
	@Override
	public float getMultiplier()
	{
		return ((IHasSound)tileEntity).getVolumeMultiplier();
	}
	
	@Override
	public Pos3D getLocation()
	{
		return new Pos3D(tileEntity);
	}
    
	@Override
	public boolean update(World world)
	{
		if(!(tileEntity instanceof IHasSound))
		{
			return false;
		}
		else if(world.getBlockTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord) != tileEntity)
		{
			return false;
		}
		else if(!((IHasSound)tileEntity).getSoundPath().equals(soundPath))
		{
			return false;
		}
		else if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() != isPlaying)
			{
				if(((IActiveState)tileEntity).getActive())
				{
					play();
				}
				else {
					stopLoop();
				}
			}
		}
		
		return true;
	}
}
