package mekanism.common.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import javax.annotation.Nullable;

/**
 * Utility class to help with interacting with {@link LambdaMetafactory} without primitives causing issues in Java versions greater than Java 8
 */
public class LambdaMetaFactoryUtil {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <O, V> Function<O, V> createGetter(Field field, Class<?> objType, String getterName) throws Throwable {
        if (getterName.isEmpty()) {
            MethodHandle getter = LOOKUP.unreflectGetter(field);
            MethodType type = getter.type();
            if (field.getType().isPrimitive()) {
                type = type.wrap().dropParameterTypes(0, 0);
            }
            CallSite site = LambdaMetafactory.metafactory(LOOKUP, "apply",
                  MethodType.methodType(Function.class, MethodHandle.class),
                  type.erase(),
                  MethodHandles.exactInvoker(getter.type()),
                  type
            );
            return (Function<O, V>) site.getTarget().invokeExact(getter);
        }
        CallSite site = LambdaMetafactory.metafactory(LOOKUP, "apply",
              MethodType.methodType(Function.class),
              MethodType.methodType(Object.class, Object.class),
              LOOKUP.findVirtual(objType, getterName, MethodType.methodType(field.getType())),
              MethodType.methodType(field.getType(), objType)
        );
        return (Function<O, V>) site.getTarget().invokeExact();
    }

    public static <O, V> BiConsumer<O, V> createSetter(Field field, Class<?> objType, String setterName) throws Throwable {
        Class<?> fieldType = field.getType();
        if (setterName.isEmpty()) {
            MethodHandle setterMH = LOOKUP.unreflectSetter(field);
            MethodType type = setterMH.type();
            if (fieldType.isPrimitive()) {
                type = type.wrap().changeReturnType(void.class);
            }
            CallSite site = LambdaMetafactory.metafactory(LOOKUP, "accept",
                  MethodType.methodType(BiConsumer.class, MethodHandle.class),
                  type.erase(),
                  MethodHandles.exactInvoker(setterMH.type()),
                  type
            );
            return (BiConsumer<O, V>) site.getTarget().invokeExact(setterMH);
        }
        CallSiteInfo siteInfo;
        Class<?> setterInterface;
        Class<?> setterFieldType = fieldType;
        if (fieldType == boolean.class) {
            setterInterface = BooleanSetter.class;
        } else if (fieldType == byte.class) {
            setterInterface = ByteSetter.class;
        } else if (fieldType == char.class) {
            setterInterface = CharSetter.class;
        } else if (fieldType == double.class) {
            setterInterface = ObjDoubleConsumer.class;
        } else if (fieldType == float.class) {
            setterInterface = FloatSetter.class;
        } else if (fieldType == int.class) {
            setterInterface = ObjIntConsumer.class;
        } else if (fieldType == long.class) {
            setterInterface = ObjLongConsumer.class;
        } else if (fieldType == short.class) {
            setterInterface = ShortSetter.class;
        } else {
            setterInterface = BiConsumer.class;
            setterFieldType = Object.class;
        }
        CallSite site = LambdaMetafactory.metafactory(LOOKUP, "accept",
              MethodType.methodType(setterInterface),
              MethodType.methodType(void.class, Object.class, setterFieldType),
              LOOKUP.findVirtual(objType, setterName, MethodType.methodType(void.class, fieldType)),
              MethodType.methodType(void.class, objType, fieldType)
        );
        siteInfo = new CallSiteInfo(site, null);
        if (fieldType == boolean.class) {
            BooleanSetter<O> setter = siteInfo.handle == null ? (BooleanSetter<O>) siteInfo.site.getTarget().invokeExact()
                                                              : (BooleanSetter<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Boolean) value);
        } else if (fieldType == byte.class) {
            ByteSetter<O> setter = siteInfo.handle == null ? (ByteSetter<O>) siteInfo.site.getTarget().invokeExact()
                                                           : (ByteSetter<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Byte) value);
        } else if (fieldType == char.class) {
            CharSetter<O> setter = siteInfo.handle == null ? (CharSetter<O>) siteInfo.site.getTarget().invokeExact()
                                                           : (CharSetter<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Character) value);
        } else if (fieldType == double.class) {
            ObjDoubleConsumer<O> setter = siteInfo.handle == null ? (ObjDoubleConsumer<O>) siteInfo.site.getTarget().invokeExact()
                                                                  : (ObjDoubleConsumer<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Double) value);
        } else if (fieldType == float.class) {
            FloatSetter<O> setter = siteInfo.handle == null ? (FloatSetter<O>) siteInfo.site.getTarget().invokeExact()
                                                            : (FloatSetter<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Float) value);
        } else if (fieldType == int.class) {
            ObjIntConsumer<O> setter = siteInfo.handle == null ? (ObjIntConsumer<O>) siteInfo.site.getTarget().invokeExact()
                                                               : (ObjIntConsumer<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Integer) value);
        } else if (fieldType == long.class) {
            ObjLongConsumer<O> setter = siteInfo.handle == null ? (ObjLongConsumer<O>) siteInfo.site.getTarget().invokeExact()
                                                                : (ObjLongConsumer<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Long) value);
        } else if (fieldType == short.class) {
            ShortSetter<O> setter = siteInfo.handle == null ? (ShortSetter<O>) siteInfo.site.getTarget().invokeExact()
                                                            : (ShortSetter<O>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
            return (instance, value) -> setter.accept(instance, (Short) value);
        }
        return siteInfo.handle == null ? (BiConsumer<O, V>) siteInfo.site.getTarget().invokeExact()
                                       : (BiConsumer<O, V>) siteInfo.site.getTarget().invokeExact(siteInfo.handle);
    }

    private static class CallSiteInfo {

        private final CallSite site;
        @Nullable
        private final MethodHandle handle;

        public CallSiteInfo(CallSite site, @Nullable MethodHandle handle) {
            this.site = site;
            this.handle = handle;
        }
    }

    @FunctionalInterface
    private interface BooleanSetter<O> {

        void accept(O instance, boolean value);
    }

    @FunctionalInterface
    private interface ByteSetter<O> {

        void accept(O instance, byte value);
    }

    @FunctionalInterface
    private interface CharSetter<O> {

        void accept(O instance, char value);
    }

    @FunctionalInterface
    private interface FloatSetter<O> {

        void accept(O instance, float value);
    }

    @FunctionalInterface
    private interface ShortSetter<O> {

        void accept(O instance, short value);
    }
}