package ic2.api.info;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for things, that have a location.
 * The function names are the way they are, because of obfuscation issues.
 * See https://github.com/md-5/SpecialSource/issues/12
 * @author Aroma1997
 */
public interface ILocatable {

	/**
	 * @return the position of the object.
	 */
	BlockPos getPosition();

	/**
	 * @return the world of this object.
	 */
	World getWorldObj();

}
