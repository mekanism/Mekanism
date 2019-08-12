package mekanism.common.integration.wrenches;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.IMekWrench;
import net.minecraft.item.ItemStack;

/**
 * Generic handler for wrenches, exposed as {@link IMekWrench}. Generally you don't want to use the enum directly, instead use {@link Wrenches#getHandler(ItemStack)}, and
 * check for nullability.
 */
public enum Wrenches {
    MEKANISM_PASSTHROUGH(IMekWrench.class.getName(), MekPassthrough::new),
    //BUILDCRAFT(BuildCraftProxy.BUILDCRAFT_WRENCH_CLASS, BuildCraftProxy::new),
    //COFH(CofhProxy.COFH_HAMMER_CLASS, CofhProxy::new),
    IC2(IC2Proxy.IC2_WRENCH_CLASS, IC2Proxy::new);

    private final String classname;
    private final Supplier<MekWrenchProxy> mekProxy;
    private Class<?> itemClass;
    private boolean enabled;
    private MekWrenchProxy provider;

    Wrenches(String clazz, Supplier<MekWrenchProxy> mekProxy) {
        this.classname = clazz;
        this.mekProxy = mekProxy;
    }

    public static void initialise() {
        for (Wrenches w : values()) {
            try {
                w.itemClass = Class.forName(w.classname);
            } catch (ClassNotFoundException e) {
                w.enabled = false;
                w.provider = null;
                w.itemClass = null;
                continue;
            }
            try {
                w.provider = w.mekProxy.get();
            } catch (Exception e) {
                w.enabled = false;
                w.provider = null;
            }
            w.enabled = w.provider != null;
        }
    }

    public static @Nullable
    IMekWrench getHandler(ItemStack it) {
        for (Wrenches w : values()) {
            if (w.canHandle(it)) {
                return w.provider.get(it);
            }
        }
        return null;
    }

    private boolean canHandle(ItemStack it) {
        return enabled && provider != null && itemClass != null && itemClass.isInstance(it.getItem());
    }
}