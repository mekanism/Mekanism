package mekanism.common.integration.computer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.content.filter.IFilter;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.util.MekCodecs;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record MethodHelpData(String methodName, @Nullable List<Param> params, Returns returns, @Nullable String description, MethodRestriction restriction,
                             boolean requiresPublicSecurity) {

    public MethodHelpData {
        if (params != null && params.isEmpty()) {
            params = null;
        }
    }

    private static final Class<?>[] NO_CLASSES = ComputerMethodFactory.NO_CLASSES;

    public static MethodHelpData from(BoundMethodHolder.BoundMethodData<?> data) {
        return from(data.method());
    }

    public static MethodHelpData from(MethodData<?> data) {
        List<Param> params = new ArrayList<>();
        for (int i = 0; i < data.argumentNames().length; i++) {
            params.add(Param.from(data.argClasses()[i], data.argumentNames()[i]));
        }

        return new MethodHelpData(data.name(), params, Returns.from(data), data.methodDescription(), data.restriction(), data.requiresPublicSecurity());
    }

    @NotNull
    private static String getHumanType(Class<?> clazz) {
        return getHumanType(clazz, NO_CLASSES);
    }

    @NotNull
    public static String getHumanType(Class<?> clazz, Class<?>[] extraTypes) {
        if (clazz == UUID.class || clazz == ResourceLocation.class || clazz == Item.class || clazz.isEnum()) {
            return "String (" + clazz.getSimpleName() + ")";
        }
        if (Frequency.class.isAssignableFrom(clazz) || clazz == GlobalPos.class || Vec3i.class.isAssignableFrom(clazz) || clazz == FluidStack.class || clazz == ItemStack.class || clazz == BlockState.class || ChemicalStack.class.isAssignableFrom(clazz) || IFilter.class.isAssignableFrom(clazz)) {
            return "Table (" + clazz.getSimpleName() + ")";
        }
        if (clazz == int.class || clazz == long.class || clazz == float.class || clazz == double.class || Number.class.isAssignableFrom(clazz)) {
            if (ClassUtils.isPrimitiveWrapper(clazz)) {
                clazz = Objects.requireNonNull(ClassUtils.wrapperToPrimitive(clazz), clazz::getName);
            }
            return "Number (" + clazz.getSimpleName() + ")";
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            String humanType = "List";
            if (extraTypes.length > 0) {
                humanType += " (" + getHumanType(extraTypes[0]) + ")";
            }
            return humanType;
        }
        if (clazz == Convertable.class || clazz == Either.class) {
            if (extraTypes.length > 0) {
                return Arrays.stream(extraTypes).map(MethodHelpData::getHumanType).collect(Collectors.joining(" or "));
            }
            return "Varies";
        }
        if (Map.class.isAssignableFrom(clazz)) {
            String humanType = "Table";
            if (extraTypes.length == 2) {
                humanType += " (" + getHumanType(extraTypes[0]) + " => " + getHumanType(extraTypes[1]) + ")";
            }
            return humanType;
        }
        return clazz.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static List<String> getEnumConstantNames(Class<?> argClass) {
        if (!argClass.isEnum()) {
            return null;
        }
        Enum<?>[] enumConstants = ((Class<? extends Enum<?>>) argClass).getEnumConstants();
        return Arrays.stream(enumConstants).map(Enum::name).toList();
    }

    public static final Codec<MethodHelpData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.STRING.fieldOf(SerializationConstants.METHOD_NAME).forGetter(MethodHelpData::methodName),
          Param.CODEC.listOf().optionalFieldOf(SerializationConstants.PARAMETERS, null).forGetter(MethodHelpData::params),
          Returns.CODEC.optionalFieldOf(SerializationConstants.RETURNS, Returns.NOTHING).forGetter(MethodHelpData::returns),
          Codec.STRING.optionalFieldOf(SerializationConstants.DESCRIPTION, null).forGetter(MethodHelpData::description),
          MekCodecs.METHOD_RESTRICTION_CODEC.optionalFieldOf(SerializationConstants.RESTRICTION, MethodRestriction.NONE).forGetter(MethodHelpData::restriction),
          Codec.BOOL.optionalFieldOf(SerializationConstants.REQUIRES_PUBLIC_SECURITY, false).forGetter(MethodHelpData::requiresPublicSecurity)
    ).apply(instance, MethodHelpData::new));

    public record Param(String name, String type, Class<?> javaType, @Nullable List<String> values) {

        public Param(String name, String type, Class<?> javaType) {
            this(name, type, javaType, null);
        }

        public static final Codec<Param> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              Codec.STRING.fieldOf(SerializationConstants.NAME).forGetter(Param::name),
              Codec.STRING.fieldOf(SerializationConstants.TYPE).forGetter(Param::type),
              MekCodecs.CLASS_TO_STRING_CODEC.fieldOf(SerializationConstants.JAVA_TYPE).forGetter(Param::javaType)/*,
              Codec.STRING.listOf().optionalFieldOf(SerializationConstants.VALUES, null).forGetter(Param::values)*/
        ).apply(instance, Param::new));

        @NotNull
        private static Param from(Class<?> argClass, String paramName) {
            return new Param(paramName, getHumanType(argClass), argClass, getEnumConstantNames(argClass));
        }
    }

    public record Returns(String type, Class<?> javaType, Class<?>[] javaExtra, @Nullable List<String> values) {

        public Returns(String type, Class<?> javaType, Class<?>[] javaExtra) {
            this(type, javaType, javaExtra, null);
        }

        public static final Returns NOTHING = new Returns("Nothing", void.class, NO_CLASSES, null);
        public static final Codec<Returns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              Codec.STRING.fieldOf(SerializationConstants.TYPE).forGetter(Returns::type),
              MekCodecs.CLASS_TO_STRING_CODEC.fieldOf(SerializationConstants.JAVA_TYPE).forGetter(Returns::javaType),
              MekCodecs.optionalClassArrayCodec(SerializationConstants.JAVA_EXTRA).forGetter(Returns::javaExtra)/*,
              Codec.STRING.listOf().optionalFieldOf(SerializationConstants.VALUES, null).forGetter(Returns::values)*/
        ).apply(instance, Returns::new));

        public static Returns from(MethodData<?> data) {
            if (data.returnType() != void.class) {
                List<String> enumConstantNames = getEnumConstantNames(data.returnType());
                for (int i = 0; i < data.returnExtra().length && enumConstantNames == null; i++) {
                    enumConstantNames = getEnumConstantNames(data.returnExtra()[i]);
                }
                return new Returns(getHumanType(data.returnType(), data.returnExtra()), data.returnType(), data.returnExtra(), enumConstantNames);
            }
            return NOTHING;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Returns other = (Returns) o;
            return javaType == other.javaType && type.equals(other.type) && Objects.equals(values, other.values) && Arrays.equals(javaExtra, other.javaExtra);
        }

        @Override
        public int hashCode() {
            int result = javaType.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + Arrays.hashCode(javaExtra);
            result = 31 * result + Objects.hashCode(values);
            return result;
        }
    }

}
