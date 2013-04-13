package buildcraft.api.transport;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;

public interface IPipedItem {

	public abstract void remove();

	/* GETTING & SETTING */
	public abstract void setWorld(World world);

	public abstract Position getPosition();

	public abstract void setPosition(double x, double y, double z);

	/**
	 * @return the speed
	 */
	public abstract float getSpeed();

	/**
	 * @param speed
	 *            the speed to set
	 */
	public abstract void setSpeed(float speed);

	/**
	 * @return the item
	 */
	public abstract ItemStack getItemStack();

	/**
	 * @param item
	 *            the item to set
	 */
	public abstract void setItemStack(ItemStack item);

	/**
	 * @return the container
	 */
	public abstract TileEntity getContainer();

	/**
	 * @param container
	 *            the container to set
	 */
	public abstract void setContainer(TileEntity container);

	/**
	 * @return the entityId
	 */
	public abstract int getEntityId();

	/**
	 * @param entityId
	 *            the entityId to set
	 */
	public abstract void setEntityId(int entityId);

	/* SAVING & LOADING */
	public abstract void readFromNBT(NBTTagCompound nbttagcompound);

	public abstract void writeToNBT(NBTTagCompound nbttagcompound);

	public abstract EntityItem toEntityItem(ForgeDirection dir);

	public abstract float getEntityBrightness(float f);

	public abstract boolean isCorrupted();

	public abstract void addContribution(String key, IPassiveItemContribution contribution);

	public abstract IPassiveItemContribution getContribution(String key);

	public abstract boolean hasContributions();

}
