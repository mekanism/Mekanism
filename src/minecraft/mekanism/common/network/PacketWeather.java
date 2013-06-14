package mekanism.common.network;

import java.io.DataOutputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketWeather implements IMekanismPacket
{
	public WeatherType activeType;
	
	@Override
	public String getName() 
	{
		return "Weather";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		activeType = (WeatherType)data[0];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
	  	player.getCurrentEquippedItem().damageItem(4999, player);
    	int weatherType = dataStream.readInt();
    	
    	switch(weatherType)
    	{
	    	case 0:
	    		world.getWorldInfo().setRaining(false);
		        world.getWorldInfo().setThundering(false);
	    		break;
	    	case 1:
	    		world.getWorldInfo().setThundering(true);
	    		break;
	    	case 2:
	    		world.getWorldInfo().setRaining(true);
		        world.getWorldInfo().setThundering(true);
		        break;
	    	case 3:
	    		world.getWorldInfo().setRaining(true);
	    		break;
    	}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception 
	{
		dataStream.writeInt(activeType.ordinal());
	}
	
	public static enum WeatherType
	{
		/** Clears the world of all weather effects, including rain, lightning, and clouds. */
		CLEAR,
		
		/** Sets the world's weather to thunder. This may or may not include rain. */
		STORM,
		
		/** Sets the world's weather to both thunder AND rain. */
		HAZE,
		
		/** Sets the world's weather to rain. */
		RAIN
	}
}
