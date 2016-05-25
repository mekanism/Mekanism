package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getWailaStack).<br>
 * An instance of this interface is passed to most of Waila Block/TileEntity callbacks.
 * @author ProfMobius
 *
 */

public interface IWailaDataAccessor{
	World        		 getWorld();
	EntityPlayer 		 getPlayer();
	Block        		 getBlock();
	//int          		 getBlockID();
	int                  getMetadata();
	IBlockState    		 getBlockState();
	TileEntity           getTileEntity();
	RayTraceResult getMOP();
	BlockPos             getPosition();
	Vec3d                 getRenderingPosition();
	NBTTagCompound       getNBTData();
	int                  getNBTInteger(NBTTagCompound tag, String keyname);
	double               getPartialFrame();
	EnumFacing           getSide();
	ItemStack            getStack();
}
