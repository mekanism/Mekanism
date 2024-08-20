package mekanism.common.inventory.container.sync.dynamic;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.annotation.ElementType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.MekAnnotationScanner.BaseAnnotationScanner;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.network.to_client.container.property.PropertyType;
import mekanism.common.util.LambdaMetaFactoryUtil;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;

public class SyncMapper extends BaseAnnotationScanner {

    public static final SyncMapper INSTANCE = new SyncMapper();
    public static final String DEFAULT_TAG = "default";
    private final List<SpecialPropertyHandler<?>> specialProperties = new ArrayList<>();
    private final Map<Class<?>, PropertyDataClassCache> syncablePropertyMap = new Object2ObjectOpenHashMap<>();

    private SyncMapper() {
        //Note: We use unchecked setters here as there is no good way to determine if we are on the server or the client
        // and when we are on the server the setters should not end up being called anyway. The reason that we need to
        // used unchecked setters is that if a recipe got removed so there is a substance in a tank that was valid but no
        // longer is valid, we want to ensure that the client is able to properly render it instead of printing an error due
        // to the client thinking that it is invalid
        specialProperties.add(new SpecialPropertyHandler<>(IExtendedFluidTank.class,
              SpecialPropertyData.create(FluidStack.class, IFluidTank::getFluid, IExtendedFluidTank::setStackUnchecked)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(IChemicalTank.class,
              SpecialPropertyData.create(ChemicalStack.class, IChemicalTank::getStack, IChemicalTank::setStackUnchecked)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(IEnergyContainer.class,
              SpecialPropertyData.create(Long.TYPE, IEnergyContainer::getEnergy, IEnergyContainer::setEnergy)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(BasicHeatCapacitor.class,
              SpecialPropertyData.create(Double.TYPE, BasicHeatCapacitor::getHeatCapacity, BasicHeatCapacitor::setHeatCapacityFromPacket),
              SpecialPropertyData.create(Double.TYPE, IHeatCapacitor::getHeat, IHeatCapacitor::setHeat)
        ));
        specialProperties.add(new SpecialPropertyHandler<>(MergedTank.class,
              SpecialPropertyData.create(FluidStack.class, obj -> obj.getFluidTank().getFluid(), (obj, val) -> obj.getFluidTank().setStackUnchecked(val)),
              SpecialPropertyData.create(ChemicalStack.class, obj -> obj.getChemicalTank().getStack(), (obj, val) -> obj.getChemicalTank().setStackUnchecked(val))
        ));
        specialProperties.add(new SpecialPropertyHandler<>(VoxelCuboid.class,
              SpecialPropertyData.create(BlockPos.class, VoxelCuboid::getMinPos, VoxelCuboid::setMinPos),
              SpecialPropertyData.create(BlockPos.class, VoxelCuboid::getMaxPos, VoxelCuboid::setMaxPos)
        ));
    }

    @Override
    protected Map<ElementType, Type[]> getSupportedTypes() {
        return Collections.singletonMap(ElementType.FIELD, new Type[]{Type.getType(ContainerSync.class)});
    }

    @Override
    protected void collectScanData(Map<String, Class<?>> classNameCache, Map<Class<?>, List<AnnotationData>> knownClasses, Set<IModFileInfo> modFileData) {
        Map<Class<?>, List<PropertyFieldInfo>> rawPropertyMap = new Object2ObjectOpenHashMap<>();
        //Only create the list once for the default fallback
        List<String> fallbackTagsList = Collections.singletonList(DEFAULT_TAG);
        for (Entry<Class<?>, List<AnnotationData>> entry : knownClasses.entrySet()) {
            Class<?> annotatedClass = entry.getKey();
            List<PropertyFieldInfo> propertyInfo = new ArrayList<>();
            rawPropertyMap.put(annotatedClass, propertyInfo);
            for (AnnotationData data : entry.getValue()) {
                String fieldName = data.memberName();
                Field field = getField(annotatedClass, fieldName);
                if (field == null) {
                    continue;
                }
                String getterName = getAnnotationValue(data, "getter", "");
                PropertyField newField;
                Class<?> fieldType = field.getType();
                SpecialPropertyHandler<?> handler = null;
                for (SpecialPropertyHandler<?> h : specialProperties) {
                    if (h.fieldType.isAssignableFrom(fieldType)) {
                        handler = h;
                        break;
                    }
                }
                try {
                    if (handler == null) {
                        PropertyType type = PropertyType.getFromType(fieldType);
                        String setterName = getAnnotationValue(data, "setter", "");
                        if (type != null) {
                            newField = new PropertyField(new TrackedFieldData(LambdaMetaFactoryUtil.createGetter(field, annotatedClass, getterName),
                                  LambdaMetaFactoryUtil.createSetter(field, annotatedClass, setterName), type));
                        } else if (fieldType.isEnum()) {
                            newField = new PropertyField(new EnumFieldData(LambdaMetaFactoryUtil.createGetter(field, annotatedClass, getterName),
                                  LambdaMetaFactoryUtil.createSetter(field, annotatedClass, setterName), fieldType));
                        } else if (fieldType.isArray()) {
                            Class<?> arrayFieldType = fieldType.getComponentType();
                            PropertyType arrayType = PropertyType.getFromType(arrayFieldType);
                            if (arrayType != null) {
                                newField = new PropertyField(new ArrayFieldData(LambdaMetaFactoryUtil.createGetter(field, annotatedClass, getterName), arrayType));
                            } else {
                                Mekanism.logger.error("Attempted to sync an invalid array field '{}' in class '{}'.", fieldName, annotatedClass.getSimpleName());
                                continue;
                            }
                        } else {
                            Mekanism.logger.error("Attempted to sync an invalid field '{}' in class '{}'.", fieldName, annotatedClass.getSimpleName());
                            continue;
                        }
                    } else {
                        newField = createSpecialProperty(handler, field, annotatedClass, getterName);
                    }
                } catch (Throwable throwable) {
                    Mekanism.logger.error("Failed to create sync data for field '{}' in class '{}'.", fieldName, annotatedClass.getSimpleName(), throwable);
                    continue;
                }
                String fullPath = annotatedClass.getName() + "#" + fieldName;
                //If the annotation data has tags add them, and otherwise fallback to the default tag
                for (String tag : getAnnotationValue(data, "tags", fallbackTagsList)) {
                    propertyInfo.add(new PropertyFieldInfo(fullPath, tag, newField));
                }
            }
        }
        List<ClassBasedInfo<PropertyFieldInfo>> propertyMap = combineWithParents(rawPropertyMap);
        for (ClassBasedInfo<PropertyFieldInfo> classPropertyInfo : propertyMap) {
            PropertyDataClassCache cache = new PropertyDataClassCache();
            classPropertyInfo.infoList().sort(Comparator.comparing(info -> info.fieldPath() + "|" + info.tag()));
            for (PropertyFieldInfo field : classPropertyInfo.infoList()) {
                cache.propertyFieldMap.put(field.tag, field.field);
            }
            syncablePropertyMap.put(classPropertyInfo.clazz(), cache);
        }
    }

    public void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier) {
        setup(container, holderClass, holderSupplier, DEFAULT_TAG);
    }

    public void setup(MekanismContainer container, Class<?> holderClass, Supplier<Object> holderSupplier, String tag) {
        PropertyDataClassCache cache = syncablePropertyMap.get(holderClass);
        if (cache == null) {
            cache = getData(syncablePropertyMap, holderClass, PropertyDataClassCache.EMPTY);
            syncablePropertyMap.put(holderClass, cache);
        }
        for (PropertyField field : cache.propertyFieldMap.get(tag)) {
            for (TrackedFieldData data : field.trackedData) {
                data.track(container, holderSupplier);
            }
        }
    }

    private static <O> PropertyField createSpecialProperty(SpecialPropertyHandler<O> handler, Field field, Class<?> objType, String getterName) throws Throwable {
        PropertyField ret = new PropertyField();
        // create a getter for the actual property field itself
        Function<Object, O> fieldGetter = LambdaMetaFactoryUtil.createGetter(field, objType, getterName);
        for (SpecialPropertyData<O> data : handler.specialData) {
            // create a new tracked field
            TrackedFieldData trackedField = TrackedFieldData.create(data.propertyType, obj -> data.get(fieldGetter.apply(obj)), (obj, val) -> data.set(fieldGetter.apply(obj), val));
            if (trackedField != null) {
                ret.addTrackedData(trackedField);
            }
        }
        return ret;
    }

    private static class PropertyDataClassCache {

        private static final PropertyDataClassCache EMPTY = new PropertyDataClassCache();

        //Note: This needs to be a linked map to ensure that the order is preserved
        private final Multimap<String, PropertyField> propertyFieldMap = LinkedHashMultimap.create();
    }

    private static class PropertyField {

        private final List<TrackedFieldData> trackedData = new ArrayList<>();

        private PropertyField(TrackedFieldData... data) {
            Collections.addAll(trackedData, data);
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

        protected void track(MekanismContainer container, Supplier<Object> holderSupplier) {
            container.track(createSyncableData(holderSupplier));
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
            }, val -> {
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
            } else if (propertyType.isArray()) {
                Class<?> arrayType = propertyType.getComponentType();
                //Note: We don't support arrays of arrays
                PropertyType type = PropertyType.getFromType(arrayType);
                if (type == null) {
                    Mekanism.logger.error("Tried to create property data for invalid array type '{}'.", arrayType.getName());
                    return null;
                }
                return new ArrayFieldData(getter, type);
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
            return SyncableEnum.create(val -> constants[val], constants[0], () -> (ENUM) getter.get(), setter::accept);
        }

        @Override
        protected Object getDefault() {
            return constants[0];
        }
    }

    //Assumes length of array is constant regardless of holder implementation
    protected static class ArrayFieldData extends TrackedFieldData {

        protected ArrayFieldData(Function<Object, Object> getter, PropertyType propertyType) {
            super(getter, null, propertyType);
        }

        @Override
        protected void track(MekanismContainer container, Supplier<Object> holderSupplier) {
            Object holder = holderSupplier.get();
            if (holder != null) {
                //Try to get the current holder
                Object field = get(holder);
                if (field.getClass().isArray()) {
                    //Validate the field is an array
                    int length = Array.getLength(field);
                    for (int i = 0; i < length; i++) {
                        int index = i;
                        //For each element in the array, add a tracker for it
                        container.track(create(() -> {
                            Object obj = holderSupplier.get();
                            return obj == null ? getDefault() : Array.get(get(obj), index);
                        }, value -> {
                            Object obj = holderSupplier.get();
                            if (obj != null) {
                                Array.set(get(obj), index, value);
                            }
                        }));
                    }
                } else {
                    Mekanism.logger.error("Unexpected field type '{}' is not an array.", field.getClass());
                }
            } else {
                Mekanism.logger.error("Unable to get holder object to add array tracker to.");
            }
        }

        @Override
        protected void set(Object dataObj, Object value) {
            throw new UnsupportedOperationException("Unsupported, uses overridden.");
        }

        @Override
        protected ISyncableData createSyncableData(Supplier<Object> obj) {
            throw new UnsupportedOperationException("Unsupported, uses overridden.");
        }
    }

    private static class SpecialPropertyHandler<O> {

        private final Class<O> fieldType;
        private final List<SpecialPropertyData<O>> specialData;

        @SafeVarargs
        private SpecialPropertyHandler(Class<O> fieldType, SpecialPropertyData<O>... data) {
            this.fieldType = fieldType;
            specialData = List.of(data);
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

    private record PropertyFieldInfo(String fieldPath, String tag, PropertyField field) {
    }
}