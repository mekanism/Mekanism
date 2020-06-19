package mekanism.common.inventory.container.sync.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.annotation.ElementType;
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
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.network.container.property.PropertyType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

public class SyncMapper {

    public static final String DEFAULT_TAG = "default";
    private static final Type CONTAINER_SYNC_TYPE = Type.getType(ContainerSync.class);
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final List<SpecialPropertyHandler<?>> specialProperties = new ArrayList<>();

    static {
        specialProperties.add(new SpecialPropertyHandler<>(IExtendedFluidTank.class,
              SpecialPropertyData.create(FluidStack.class, IFluidTank::getFluid, IExtendedFluidTank::setStack)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(IGasTank.class,
              SpecialPropertyData.create(GasStack.class, IChemicalTank::getStack, IChemicalTank::setStack)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(IInfusionTank.class,
              SpecialPropertyData.create(InfusionStack.class, IChemicalTank::getStack, IChemicalTank::setStack)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(IPigmentTank.class,
              SpecialPropertyData.create(PigmentStack.class, IChemicalTank::getStack, IChemicalTank::setStack)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(ISlurryTank.class,
              SpecialPropertyData.create(SlurryStack.class, IChemicalTank::getStack, IChemicalTank::setStack)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(IEnergyContainer.class,
              SpecialPropertyData.create(FloatingLong.class, IEnergyContainer::getEnergy, IEnergyContainer::setEnergy)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(BasicHeatCapacitor.class,
              SpecialPropertyData.create(double.class, BasicHeatCapacitor::getHeatCapacity, BasicHeatCapacitor::setHeatCapacityFromPacket),
              SpecialPropertyData.create(double.class, IHeatCapacitor::getHeat, IHeatCapacitor::setHeat)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(MergedTank.class,
              SpecialPropertyData.create(FluidStack.class, obj -> obj.getFluidTank().getFluid(), (obj, val) -> obj.getFluidTank().setStack(val)),
              SpecialPropertyData.create(GasStack.class, obj -> obj.getGasTank().getStack(), (obj, val) -> obj.getGasTank().setStack(val)),
              SpecialPropertyData.create(InfusionStack.class, obj -> obj.getInfusionTank().getStack(), (obj, val) -> obj.getInfusionTank().setStack(val)),
              SpecialPropertyData.create(PigmentStack.class, obj -> obj.getPigmentTank().getStack(), (obj, val) -> obj.getPigmentTank().setStack(val)),
              SpecialPropertyData.create(SlurryStack.class, obj -> obj.getSlurryTank().getStack(), (obj, val) -> obj.getSlurryTank().setStack(val))
        ));
        specialProperties.add(new SpecialPropertyHandler<>(MergedChemicalTank.class,
              SpecialPropertyData.create(GasStack.class, obj -> obj.getGasTank().getStack(), (obj, val) -> obj.getGasTank().setStack(val)),
              SpecialPropertyData.create(InfusionStack.class, obj -> obj.getInfusionTank().getStack(), (obj, val) -> obj.getInfusionTank().setStack(val)),
              SpecialPropertyData.create(PigmentStack.class, obj -> obj.getPigmentTank().getStack(), (obj, val) -> obj.getPigmentTank().setStack(val)),
              SpecialPropertyData.create(SlurryStack.class, obj -> obj.getSlurryTank().getStack(), (obj, val) -> obj.getSlurryTank().setStack(val))
        ));
    }

    private static final Map<Class<?>, PropertyDataClassCache> syncablePropertyMap = new Object2ObjectOpenHashMap<>();

    public static void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier) {
        setup(container, holderClass, holderSupplier, DEFAULT_TAG);
    }

    public static void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier, String tag) {
        PropertyDataClassCache cache = syncablePropertyMap.computeIfAbsent(holderClass, SyncMapper::buildSyncMap);

        for (PropertyField field : cache.propertyFieldMap.get(tag)) {
            for (TrackedFieldData data : field.trackedData) {
                container.track(data.createSyncableData(holderSupplier));
            }
        }
    }

    private static PropertyDataClassCache buildSyncMap(Class<?> clazz) {
        PropertyDataClassCache cache = new PropertyDataClassCache();
        try {
            buildSyncMap(clazz, cache);
        } catch (Throwable e) {
            Mekanism.logger.error("Failed to create sync map for {}", clazz.getName());
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

        //TODO - V10: Work on improving this, due to the fact that the above uses recursion
        //TODO - V10: Validate this, I believe that the server and client may have the mod file scan data in a different order??
        // Or at least the evap tower was doing "weird" things such as filling the water tank with brine when on the server
        ModList modList = ModList.get();
        String clazzPath = clazz.getName();
        for (ModFileScanData scanData : modList.getAllScanData()) {
            for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
                //If the annotation is on a field, and is the sync type, and the class matches the class we are checking
                if (data.getTargetType() == ElementType.FIELD && CONTAINER_SYNC_TYPE.equals(data.getAnnotationType()) &&
                    clazzPath.equals(data.getClassType().getClassName())) {
                    String fieldName = data.getMemberName();
                    try {
                        Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        //TODO: Change syncData to instead use the data from the annotationData
                        ContainerSync syncData = field.getAnnotation(ContainerSync.class);
                        PropertyType type = PropertyType.getFromType(field.getType());
                        SpecialPropertyHandler<?> handler = specialProperties.stream().filter(h -> h.fieldType.isAssignableFrom(field.getType())).findFirst().orElse(null);
                        PropertyField newField;
                        if (handler != null) {
                            newField = createSpecialProperty(handler, field, clazz, syncData);
                        } else if (type != null) {
                            newField = new PropertyField(new TrackedFieldData(createGetter(field, clazz, syncData), createSetter(field, clazz, syncData), type));
                        } else if (field.getType().isEnum()) {
                            newField = new PropertyField(new EnumFieldData(createGetter(field, clazz, syncData), createSetter(field, clazz, syncData), field.getType()));
                        } else {
                            Mekanism.logger.error("Attempted to sync an invalid field '{}'", fieldName);
                            continue;
                        }

                        for (String tag : syncData.tags()) {
                            cache.propertyFieldMap.put(tag, newField);
                        }
                    } catch (NoSuchFieldException e) {
                        //TODO: Better error message
                        Mekanism.logger.error("Failed to find: {}, for class: {}", fieldName, clazz.getName());
                    }
                }
            }
        }

        //TODO - V10: FIXME, this does not work properly on the server as it can force client side only stuff to load
        /*for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ContainerSync.class)) {
                field.setAccessible(true);
                ContainerSync syncData = field.getAnnotation(ContainerSync.class);
                PropertyType type = PropertyType.getFromType(field.getType());
                SpecialPropertyHandler<?> handler = specialProperties.stream().filter(h -> h.fieldType.isAssignableFrom(field.getType())).findFirst().orElse(null);
                PropertyField newField;
                if (handler != null) {
                    newField = createSpecialProperty(handler, field, clazz, syncData);
                } else if (type != null) {
                    newField = new PropertyField(new TrackedFieldData(createGetter(field, clazz, syncData), createSetter(field, clazz, syncData), type));
                } else if (field.getType().isEnum()) {
                    newField = new PropertyField(new EnumFieldData(createGetter(field, clazz, syncData), createSetter(field, clazz, syncData), field.getType()));
                } else {
                    Mekanism.logger.error("Attempted to sync an invalid field '{}'", field.getName());
                    continue;
                }

                for (String tag : syncData.tags()) {
                    cache.propertyFieldMap.put(tag, newField);
                }
            }
        }*/
    }

    private static <O> PropertyField createSpecialProperty(SpecialPropertyHandler<O> handler, Field field, Class<?> objType, ContainerSync syncData) throws Throwable {
        PropertyField ret = new PropertyField();
        for (SpecialPropertyData<O> data : handler.specialData) {
            // create a getter for the actual property field itself
            Function<Object, O> fieldGetter = createGetter(field, objType, syncData);
            // create a new tracked field
            TrackedFieldData trackedField = TrackedFieldData.create(data.propertyType, obj -> data.get(fieldGetter.apply(obj)), (obj, val) -> data.set(fieldGetter.apply(obj), val));
            if (trackedField != null) {
                ret.addTrackedData(trackedField);
            }
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

        private final Multimap<String, PropertyField> propertyFieldMap = HashMultimap.create();
    }

    protected static class PropertyField {

        private final List<TrackedFieldData> trackedData = new ArrayList<>();

        private PropertyField(TrackedFieldData... data) {
            trackedData.addAll(Arrays.asList(data));
        }

        private void addTrackedData(TrackedFieldData data) {
            trackedData.add(data);
        }
    }

    protected static class TrackedFieldData {

        private PropertyType propertyType;
        private final Function<Object, Object> getter;
        private final BiConsumer<Object, Object> setter;

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

        protected ISyncableData createSyncableData(Supplier<Object> obj) {
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

        protected static TrackedFieldData create(Class<?> propertyType, Function<Object, Object> getter, BiConsumer<Object, Object> setter) {
            if (propertyType.isEnum()) {
                return new EnumFieldData(getter, setter, propertyType);
            }
            PropertyType type = PropertyType.getFromType(propertyType);
            if (type == null) {
                Mekanism.logger.error("Tried to create property data for invalid type '{}'.", propertyType.getName());
                return null;
            }
            return new TrackedFieldData(getter, setter, type);
        }
    }

    protected static class EnumFieldData extends TrackedFieldData {

        private final Object[] constants;

        private EnumFieldData(Function<Object, Object> getter, BiConsumer<Object, Object> setter, Class<?> enumClass) {
            super(getter, setter);
            constants = enumClass.getEnumConstants();
        }

        @Override
        protected ISyncableData create(Supplier<Object> getter, Consumer<Object> setter) {
            return createData((Enum[]) constants, getter, setter);
        }

        protected <ENUM extends Enum<ENUM>> ISyncableData createData(ENUM[] constants, Supplier<Object> getter, Consumer<Object> setter) {
            return SyncableEnum.create((val) -> constants[val], constants[0], () -> (ENUM) getter.get(), setter::accept);
        }

        @Override
        protected Object getDefault() {
            return constants[0];
        }
    }

    protected static class SpecialPropertyHandler<O> {

        private final Class<O> fieldType;
        private final List<SpecialPropertyData<O>> specialData = new ArrayList<>();

        @SafeVarargs
        private SpecialPropertyHandler(Class<O> fieldType, SpecialPropertyData<O>... data) {
            this.fieldType = fieldType;
            specialData.addAll(Arrays.asList(data));
        }
    }

    protected static class SpecialPropertyData<O> {

        private final Class<?> propertyType;
        private final Function<O, ?> getter;
        private final BiConsumer<O, Object> setter;

        private SpecialPropertyData(Class<?> propertyType, Function<O, ?> getter, BiConsumer<O, Object> setter) {
            this.propertyType = propertyType;
            this.getter = getter;
            this.setter = setter;
        }

        protected Object get(O obj) {
            return getter.apply(obj);
        }

        protected void set(O obj, Object val) {
            setter.accept(obj, val);
        }

        @SuppressWarnings("unchecked")
        protected static <O, V> SpecialPropertyData<O> create(Class<V> propertyType, Function<O, V> getter, BiConsumer<O, V> setter) {
            return new SpecialPropertyData<>(propertyType, getter, (BiConsumer<O, Object>) setter);
        }
    }
}