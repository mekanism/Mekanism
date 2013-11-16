package calclavia.lib.render;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Applied to TileEntities to render a tag above them.
 * 
 * @author Calclavia
 * 
 */
public interface ITagRender
{
	/**
	 * Gets the list of strings to render above the object.
	 * 
	 * @param player The player this list will display for
	 * @param map HashMap of strings followed by there color Example {"Hello World",0x88FF88}
	 * @return The height in which the render should happen.
	 */
	public float addInformation(HashMap<String, Integer> map, EntityPlayer player);
}
