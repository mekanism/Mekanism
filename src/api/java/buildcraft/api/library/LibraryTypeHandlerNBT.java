package buildcraft.api.library;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class LibraryTypeHandlerNBT extends LibraryTypeHandler {
    public LibraryTypeHandlerNBT(String extension) {
        super(extension);
    }

    public abstract ItemStack load(ItemStack stack, NBTTagCompound nbt);

    public abstract boolean store(ItemStack stack, NBTTagCompound nbt);
}
