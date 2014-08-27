package mekanism.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TestSound extends PositionedSound implements ITickableSound
{
	public boolean finished = true;

	public TestSound(ResourceLocation location, TileEntity tile)
	{
		super(location);
		this.volume = 1.0f;
		this.field_147663_c = 1.0f;
		this.xPosF = tile.xCoord;
		this.yPosF = tile.yCoord;
		this.zPosF = tile.zCoord;
		this.repeat = true;
		this.field_147665_h = 0;
		this.field_147666_i = AttenuationType.LINEAR;
	}

	@Override
	public boolean isDonePlaying()
	{
		return finished;
	}

	@Override
	public void update()
	{
	}
}