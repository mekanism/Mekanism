package mekanism.client;

import mekanism.common.EntityRobit;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;

public class RenderRobit extends RenderLiving
{
	public RenderRobit() 
	{
		super(new ModelRobit(), 0.5F);
	}
}
