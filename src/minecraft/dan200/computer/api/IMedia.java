/**
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2013. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computer.api;

/**
 * TODO: Document me
 */
public interface IMedia
{
	public String getLabel( net.minecraft.item.ItemStack stack );
	public boolean setLabel( net.minecraft.item.ItemStack stack, String label );
	
	public String getAudioTitle( net.minecraft.item.ItemStack stack );
	public String getAudioRecordName( net.minecraft.item.ItemStack stack );	
    
    public String mountData( net.minecraft.item.ItemStack stack, IComputerAccess computer );
}
