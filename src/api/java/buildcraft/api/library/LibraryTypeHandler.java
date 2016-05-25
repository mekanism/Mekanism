package buildcraft.api.library;

import net.minecraft.item.ItemStack;

public abstract class LibraryTypeHandler {
    public enum HandlerType {
        LOAD,
        STORE
    }

    private final String extension;

    public LibraryTypeHandler(String extension) {
        this.extension = extension;
    }

    public abstract boolean isHandler(ItemStack stack, HandlerType type);

    public boolean isInputExtension(String ext) {
        return extension.equals(ext);
    }

    public String getOutputExtension() {
        return extension;
    }

    public abstract int getTextColor();

    public abstract String getName(ItemStack stack);
}
