package mekanism.common.integration.computer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.filter.IFilter;
import mekanism.common.lib.frequency.Frequency;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MethodHelpData(String methodName, @Nullable List<Param> params, Returns returns, @Nullable String description, MethodRestriction restriction, boolean requiresPublicSecurity) {

    public static MethodHelpData from(BoundMethodHolder.BoundMethodData<?> data) {
        return from(data.method());
    }

    public static MethodHelpData from(ComputerMethodFactory.MethodData<?> data) {
        return from(data.name(), data.argumentNames(), data.argClasses(), data.returnType(), data.methodDescription(), data.restriction(), data.requiresPublicSecurity());
    }

    @NotNull
    private static MethodHelpData from(String methodName, String[] argumentNames, Class<?>[] argumentClasses, Class<?> returnType, @Nullable String methodDescription, MethodRestriction restriction, boolean requiresPublicSecurity) {
        List<Param> params = new ArrayList<>();
        for (int i = 0; i < argumentNames.length; i++) {
            Class<?> argClass = argumentClasses[i];
            params.add(new Param(argumentNames[i], getHumanType(argClass), argClass, getEnumConstantNames(argClass)));
        }

        Returns returns = returnType != void.class ? new Returns(getHumanType(returnType), returnType, getEnumConstantNames(returnType)) : Returns.NOTHING;

        return new MethodHelpData(methodName, params.isEmpty() ? null : params, returns, methodDescription, restriction, requiresPublicSecurity);
    }

    @NotNull
    private static String getHumanType(Class<?> clazz) {
        if (clazz == UUID.class || clazz == ResourceLocation.class || clazz == Item.class) {
            return "String ("+clazz.getSimpleName()+")";
        }
        if (Frequency.class.isAssignableFrom(clazz) || clazz == Coord4D.class || clazz == Vec3i.class || clazz == FluidStack.class || clazz == ItemStack.class || clazz == BlockState.class || ChemicalStack.class.isAssignableFrom(clazz) || IFilter.class.isAssignableFrom(clazz)) {
            return "Table ("+clazz.getSimpleName()+")";
        }
        if (clazz == int.class || clazz == long.class || clazz == float.class || clazz == double.class || clazz == FloatingLong.class) {
            return "Number ("+clazz.getSimpleName()+")";
        }
        if (clazz == Convertable.class) {
            return "Varies";//technically can be anything, but so far only map used
        }
        return clazz == Map.class ? "Table" : clazz.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    private static List<String> getEnumConstantNames(Class<?> argClass) {
        if (!Enum.class.isAssignableFrom(argClass)) {
            return null;
        }
        Enum<?>[] enumConstants = ((Class<? extends Enum<?>>) argClass).getEnumConstants();
        return Arrays.stream(enumConstants).map(Enum::name).toList();
    }

    private static final Codec<MethodRestriction> METHOD_RESTRICTION_CODEC = ExtraCodecs.stringResolverCodec(MethodRestriction::name, MethodRestriction::valueOf);

    public static final Codec<Class<?>> CLASS_TO_STRING_CODEC = ExtraCodecs.stringResolverCodec(Class::getName, s->{
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            return null;
        }
    });

    public static final Codec<MethodHelpData> CODEC = RecordCodecBuilder.create(instance ->
          instance.group(
                Codec.STRING.fieldOf("methodName").forGetter(MethodHelpData::methodName),
                Param.CODEC.listOf().optionalFieldOf("params", null).forGetter(MethodHelpData::params),
                Returns.CODEC.optionalFieldOf("returns", Returns.NOTHING).forGetter(MethodHelpData::returns),
                Codec.STRING.optionalFieldOf("description", null).forGetter(MethodHelpData::description),
                METHOD_RESTRICTION_CODEC.optionalFieldOf("restriction", MethodRestriction.NONE).forGetter(MethodHelpData::restriction),
                Codec.BOOL.optionalFieldOf("requiresPublicSecurity", false).forGetter(MethodHelpData::requiresPublicSecurity)
          ).apply(instance, MethodHelpData::new)
    );

    public record Param(String name, String type, Class<?> javaType, @Nullable List<String> values){
        public static final Codec<Param> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(
                    Codec.STRING.fieldOf("name").forGetter(Param::name),
                    Codec.STRING.fieldOf("type").forGetter(Param::type),
                    CLASS_TO_STRING_CODEC.fieldOf("javaType").forGetter(Param::javaType),
                    Codec.STRING.listOf().optionalFieldOf("values", null).forGetter(Param::values)
              ).apply(instance, Param::new)
        );
    }

    public record Returns(String type, Class<?> javaType, @Nullable List<String> values){
        public static final Returns NOTHING = new Returns("Nothing", void.class, null);
        public static final Codec<Returns> CODEC = RecordCodecBuilder.create(instance->instance.group(
              Codec.STRING.fieldOf("type").forGetter(Returns::type),
              CLASS_TO_STRING_CODEC.fieldOf("javaType").forGetter(Returns::javaType),
              Codec.STRING.listOf().optionalFieldOf("values", null).forGetter(Returns::values)
        ).apply(instance, Returns::new));
    }
}
