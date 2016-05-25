package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getWailaStack).<br> 
 * Common accessor for both Entity and Block/TileEntity.<br>
 * Available data depends on what it is called upon (ie : getEntity() will return null if looking at a block, etc).<br>
 *
 */
public interface IWailaCommonAccessor {
	World        		 getWorld();
	EntityPlayer 		 getPlayer();
	Block        		 getBlock();
	int          		 getBlockID();
	String               getBlockQualifiedName();
	int          		 getMetadata();
	TileEntity           getTileEntity();
	Entity               getEntity();
	BlockPos 			 getPosition();
	Vec3d                 getRenderingPosition();
	NBTTagCompound       getNBTData();
	int                  getNBTInteger(NBTTagCompound tag, String keyname);
	double               getPartialFrame();
	EnumFacing			 getSide();
	ItemStack            getStack();
}
