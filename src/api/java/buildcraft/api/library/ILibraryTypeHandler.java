package buildcraft.api.library;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface ILibraryTypeHandler {
    boolean isHandler(ItemStack stack, boolean store);

    String getFileExtension();

    int getTextColor();

    String getName(ItemStack stack);

    ItemStack load(ItemStack stack, NBTTagCompound compound);

    boolean store(ItemStack stack, NBTTagCompound compound);
}
