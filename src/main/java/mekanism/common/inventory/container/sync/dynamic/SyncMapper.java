package mekanism.common.inventory.container.sync.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.network.container.property.PropertyType;
import net.minecraftforge.fluids.FluidStack;

public class SyncMapper {

    public static final String DEFAULT_TAG = "default";

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final List<SpecialPropertyHandler> specialProperties = new ArrayList<>();

    static {
        specialProperties.add(new SpecialPropertyHandler(IExtendedFluidTank.class, new SpecialPropertyData(FluidStack.class, FluidStack.class, "getFluid", "setStack")));
        specialProperties.add(new SpecialPropertyHandler(IGasTank.class, new SpecialPropertyData(GasStack.class, ChemicalStack.class, "getStack", "setStack")));
        specialProperties.add(new SpecialPropertyHandler(IEnergyContainer.class, new SpecialPropertyData(FloatingLong.class, FloatingLong.class, "getEnergy", "setEnergy")));
        specialProperties.add(new SpecialPropertyHandler(BasicHeatCapacitor.class, new SpecialPropertyData(Double.TYPE, Double.TYPE, "getHeatCapacity", "setHeatCapacityFromPacket"),
              new SpecialPropertyData(Double.TYPE, Double.TYPE, "getHeat", "setHeat")));
    }

    private static Map<Class<?>, PropertyDataClassCache> syncablePropertyMap = new Object2ObjectOpenHashMap<>();

    public static void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier) {
        setup(container, holderClass, holderSupplier, DEFAULT_TAG);
    }

    public static void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier, String tag) {
        PropertyDataClassCache cache = syncablePropertyMap.computeIfAbsent(holderClass, c -> buildSyncMap(c));

        for (PropertyField field : cache.propertyFieldMap.get(tag)) {
            for (TrackedFieldData data : field.trackedData) {
                container.track(data.createProxiedSyncableData(holderSupplier));
            }
        }
    }

    private static PropertyDataClassCache buildSyncMap(Class<?> clazz) {
        PropertyDataClassCache cache = new PropertyDataClassCache();
        try {
            buildSyncMap(clazz, cache);
        } catch (Throwable e) {
            Mekanism.logger.error("Failed to create sync map for " + clazz.getName());
            e.printStackTrace();
        }
        return cache;
    }

    private static void buildSyncMap(Class<?> clazz, PropertyDataClassCache cache) throws Throwable {
        if (clazz.getSuperclass() != null) {
            PropertyDataClassCache superCache = syncablePropertyMap.get(clazz.getSuperclass());
            if (superCache != null) {
                cache.propertyFieldMap.putAll(superCache.propertyFieldMap);
                return;
            } else {
                // recurse to root superclass
                buildSyncMap(clazz.getSuperclass(), cache);
            }
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ContainerSync.class)) {
                field.setAccessible(true);
                ContainerSync syncData = field.getAnnotation(ContainerSync.class);
                PropertyType type = PropertyType.getFromType(field.getType());
                SpecialPropertyHandler handler = specialProperties.stream().filter(h -> h.fieldType.isAssignableFrom(field.getType())).findFirst().orElse(null);
                PropertyField newField = null;
                if (handler != null) {
                    newField = createSpecialProperty(handler, field, clazz, syncData);
                } else if (type != null) {
                    newField = new PropertyField(new TrackedFieldData(createGetter(field, clazz, syncData), createSetter(field, clazz, syncData), type));
                } else if (field.getType().isEnum()) {
                    newField = new PropertyField(new EnumFieldData(createGetter(field, clazz, syncData), createSetter(field, clazz, syncData), field.getType()));
                } else {
                    Mekanism.logger.error("Attempted to sync an invalid field '" + field.getName() + "'");
                    continue;
                }

                cache.propertyFieldMap.put(syncData.tag(), newField);
            }
        }
    }

    private static PropertyField createSpecialProperty(SpecialPropertyHandler handler, Field field, Class<?> objType, ContainerSync syncData) throws Throwable {
        PropertyField ret = new PropertyField();
        for (SpecialPropertyData data : handler.specialData) {
            PropertyType type = PropertyType.getFromType(data.propertyType);
            if (type == null) {
                Mekanism.logger.error("Tried to create special property data from invalid type '" + data.valueType + "'.");
                return null;
            }
            Function<Object, Object> fieldGetter = createGetter(field, objType, syncData);
            CallSite site = LambdaMetafactory.metafactory(LOOKUP, "apply", MethodType.methodType(Function.class), MethodType.methodType(Object.class, Object.class),
                  LOOKUP.findVirtual(handler.fieldType, data.getterMethodName, MethodType.methodType(data.valueType)), MethodType.methodType(data.valueType, handler.fieldType));
            Function<Object, Object> getter = (Function<Object, Object>) site.getTarget().invokeExact();

            site = LambdaMetafactory.metafactory(LOOKUP, "accept", MethodType.methodType(BiConsumer.class), MethodType.methodType(void.class, Object.class, Object.class),
                  LOOKUP.findVirtual(handler.fieldType, data.setterMethodName, MethodType.methodType(void.class, data.valueType)), MethodType.methodType(void.class, handler.fieldType, data.valueType));
            BiConsumer<Object, Object> setter = (BiConsumer<Object, Object>) site.getTarget().invokeExact();
            ret.addTrackedData(new TrackedFieldData(obj -> getter.apply(fieldGetter.apply(obj)), (obj, val) -> setter.accept(fieldGetter.apply(obj), val), type));
        }
        return ret;
    }

    private static <O, V> Function<O, V> createGetter(Field field, Class<?> objType, ContainerSync syncData) throws Throwable {
        if (!syncData.getter().isEmpty()) {
            CallSite site = LambdaMetafactory.metafactory(LOOKUP, "apply", MethodType.methodType(Function.class), MethodType.methodType(Object.class, Object.class),
                  LOOKUP.findVirtual(objType, syncData.getter(), MethodType.methodType(field.getType())), MethodType.methodType(field.getType(), objType));
            return (Function<O, V>) site.getTarget().invokeExact();
        } else {
            MethodHandle getter = LOOKUP.unreflectGetter(field);
            MethodType type = getter.type();
            if (field.getType().isPrimitive()) {
                type = type.wrap().dropParameterTypes(0, 0);
            }
            CallSite site = LambdaMetafactory.metafactory(LOOKUP, "apply", MethodType.methodType(Function.class, MethodHandle.class), type.erase(), MethodHandles.exactInvoker(getter.type()), type);
            return (Function<O, V>) site.getTarget().invokeExact(getter);
        }
    }

    private static <O, V> BiConsumer<O, V> createSetter(Field field, Class<?> objType, ContainerSync syncData) throws Throwable {
        if (!syncData.setter().isEmpty()) {
            CallSite site = LambdaMetafactory.metafactory(LOOKUP, "accept", MethodType.methodType(BiConsumer.class), MethodType.methodType(void.class, Object.class, Object.class),
                  LOOKUP.findVirtual(objType, syncData.setter(), MethodType.methodType(void.class, field.getType())), MethodType.methodType(void.class, objType, field.getType()));
            return (BiConsumer<O, V>) site.getTarget().invokeExact();
        } else {
            MethodHandle setter = LOOKUP.unreflectSetter(field);
            MethodType type = setter.type();
            if (field.getType().isPrimitive()) {
                type = type.wrap().changeReturnType(void.class);
            }
            CallSite site = LambdaMetafactory.metafactory(LOOKUP, "accept", MethodType.methodType(BiConsumer.class, MethodHandle.class), type.erase(), MethodHandles.exactInvoker(setter.type()), type);
            return (BiConsumer<O, V>) site.getTarget().invokeExact(setter);
        }
    }

    protected static class PropertyDataClassCache {

        private Multimap<String, PropertyField> propertyFieldMap = HashMultimap.create();
    }

    protected static class PropertyField {

        private List<TrackedFieldData> trackedData = new ArrayList<>();

        private PropertyField(TrackedFieldData... data) {
            trackedData.addAll(Arrays.asList(data));
        }

        private void addTrackedData(TrackedFieldData data) {
            trackedData.add(data);
        }
    }

    protected static class TrackedFieldData {

        private PropertyType propertyType;
        private Function<Object, Object> getter;
        private BiConsumer<Object, Object> setter;

        protected TrackedFieldData(Function<Object, Object> getter, BiConsumer<Object, Object> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private TrackedFieldData(Function<Object, Object> getter, BiConsumer<Object, Object> setter, PropertyType propertyType) {
            this(getter, setter);
            this.propertyType = propertyType;
        }

        protected Object get(Object dataObj) {
            return getter.apply(dataObj);
        }

        protected void set(Object dataObj, Object value) {
            setter.accept(dataObj, value);
        }

        protected ISyncableData createProxiedSyncableData(Supplier<Object> obj) {
            return create(() -> {
                Object dataObj = obj.get();
                return dataObj == null ? getDefault() : get(dataObj);
            }, (val) -> {
                Object dataObj = obj.get();
                if (dataObj != null) {
                    set(dataObj, val);
                }
            });
        }

        protected ISyncableData create(Supplier<Object> getter, Consumer<Object> setter) {
            return propertyType.create(getter, setter);
        }

        protected Object getDefault() {
            return propertyType.getDefault();
        }
    }

    protected static class EnumFieldData extends TrackedFieldData {

        private Object[] constants;

        private EnumFieldData(Function<Object, Object> getter, BiConsumer<Object, Object> setter, Class<?> enumClass) {
            super(getter, setter);
            constants = enumClass.getEnumConstants();
        }

        @Override
        protected ISyncableData create(Supplier<Object> getter, Consumer<Object> setter) {
            return createData((Enum[]) constants, getter, setter);
        }

        protected <ENUM extends Enum<ENUM>> ISyncableData createData(ENUM[] constants, Supplier<Object> getter, Consumer<Object> setter) {
            return SyncableEnum.create((val) -> constants[val], constants[0], () -> (ENUM) getter.get(), (val) -> setter.accept(val));
        }

        @Override
        protected Object getDefault() {
            return constants[0];
        }
    }

    protected static class SpecialPropertyHandler {

        private Class<?> fieldType;
        private List<SpecialPropertyData> specialData = new ArrayList<>();

        private SpecialPropertyHandler(Class<?> fieldType, SpecialPropertyData... data) {
            this.fieldType = fieldType;
            specialData.addAll(Arrays.asList(data));
        }
    }

    protected static class SpecialPropertyData {

        private Class<?> propertyType;
        private Class<?> valueType;
        private String getterMethodName;
        private String setterMethodName;

        private SpecialPropertyData(Class<?> propertyType, Class<?> valueType, String getterMethodName, String setterMethodName) {
            this.propertyType = propertyType;
            this.valueType = valueType;
            this.getterMethodName = getterMethodName;
            this.setterMethodName = setterMethodName;
        }
    }
}
