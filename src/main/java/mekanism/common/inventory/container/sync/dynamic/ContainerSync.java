package mekanism.common.inventory.container.sync.dynamic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ContainerSync {

    String getter() default "";

    String setter() default "";

    String[] tags() default SyncMapper.DEFAULT_TAG;
}
