package ic2.api.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import ic2.api.item.IKineticRotor;

/**
 * Interface for {@link TileEntity}s that can have rotors, see also {@link IKineticRotor}<br/>
 * Use the {@link RotorRegistry} to use IC2's default windmill renderer
 */
public interface IRotorProvider {
	/**
	 * @return Radius of current rotor (in blocks), or 0 for no rotor
	 */
	public int getRotorDiameter();

	/**
	 * @return The current direction the rotor is facing
	 */
	public EnumFacing getFacing();

	/**
	 * @return Angle (in degrees) to render the rotor at
	 */
	public float getAngle();

	/**
	 * @return Texture of the current rotor, called every tick so remember to store in a variable
	 */
	public ResourceLocation getRotorRenderTexture();
}