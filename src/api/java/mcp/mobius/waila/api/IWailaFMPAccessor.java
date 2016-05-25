package mcp.mobius.waila.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getWailaStack).<br>
 * An instance of this interface is passed to most of Waila FMP callbacks.
 * @author ProfMobius
 *
 */

public interface IWailaFMPAccessor {
	World        		 getWorld();
	EntityPlayer 		 getPlayer();
	TileEntity           getTileEntity();
	RayTraceResult getPosition();
	NBTTagCompound       getNBTData();
	NBTTagCompound       getFullNBTData();
	int                  getNBTInteger(NBTTagCompound tag, String keyname);
	double               getPartialFrame();
	Vec3d                 getRenderingPosition();
	String               getID();
}
