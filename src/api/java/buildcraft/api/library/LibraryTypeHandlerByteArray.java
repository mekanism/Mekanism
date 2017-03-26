package buildcraft.api.library;

import net.minecraft.item.ItemStack;

public abstract class LibraryTypeHandlerByteArray extends LibraryTypeHandler {
    public LibraryTypeHandlerByteArray(String extension) {
        super(extension);
    }

    public abstract ItemStack load(ItemStack stack, byte[] data);

    public abstract byte[] store(ItemStack stack);
}
