package buildcraft.api.gates;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public interface ITriggerParameter {

	public abstract ItemStack getItemStack();

	public abstract void set(ItemStack stack);

	public abstract void writeToNBT(NBTTagCompound compound);

	public abstract void readFromNBT(NBTTagCompound compound);

	public abstract ItemStack getItem();

}