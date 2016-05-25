package buildcraft.api.library;

import java.util.HashSet;
import java.util.Set;

public final class LibraryAPI {
    private static final Set<LibraryTypeHandler> handlers = new HashSet<LibraryTypeHandler>();

    private LibraryAPI() {

    }

    public static Set<LibraryTypeHandler> getHandlerSet() {
        return handlers;
    }

    public static void registerHandler(LibraryTypeHandler handler) {
        handlers.add(handler);
    }

    public static LibraryTypeHandler getHandlerFor(String extension) {
        for (LibraryTypeHandler h : handlers) {
            if (h.isInputExtension(extension)) {
                return h;
            }
        }
        return null;
    }
}
