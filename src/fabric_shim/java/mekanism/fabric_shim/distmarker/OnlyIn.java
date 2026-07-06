package mekanism.fabric_shim.distmarker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stand-in for net.neoforged.api.distmarker.OnlyIn.
 *
 * <p>Unlike NeoForge (RuntimeDistCleaner) and Fabric's own @Environment, nothing strips members
 * carrying this annotation at load time on this port — it is documentation only. Code must not
 * rely on stripping for dedicated-server safety; client-only classes still need to be reached
 * only from client entry points.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PACKAGE})
public @interface OnlyIn {

    Dist value();

    Class<?> _interface() default Object.class;
}
