package buildcraft.api.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.Item;

public final class ListRegistry {
    public static final List<Class<? extends Item>> itemClassAsType = new ArrayList<Class<? extends Item>>();
    private static final List<ListMatchHandler> handlers = new ArrayList<ListMatchHandler>();

    private ListRegistry() {

    }

    public static void registerHandler(ListMatchHandler h) {
        if (h != null) {
            handlers.add(h);
        }
    }

    public static List getHandlers() {
        return Collections.unmodifiableList(handlers);
    }
}
