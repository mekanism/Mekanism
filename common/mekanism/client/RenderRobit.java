package mekanism.client;

import net.minecraft.client.renderer.entity.RenderLiving;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRobit extends RenderLiving
{
	public RenderRobit() 
	{
		super(new ModelRobit(), 0.5F);
	}
}
