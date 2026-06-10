package mekanism.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import mekanism.common.integration.computer.MethodRestriction;
import org.jspecify.annotations.Nullable;

public class MekCodecs {

    public static final Codec<MethodRestriction> METHOD_RESTRICTION_CODEC = Codec.stringResolver(MethodRestriction::name, MethodRestriction::valueOf);
    public static final Codec<Class<?>> CLASS_TO_STRING_CODEC = stringResolver(Class::getName, s -> {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            return null;
        }
    });

    public static MapCodec<Class<?>[]> optionalClassArrayCodec(String fieldName) {
        return CLASS_TO_STRING_CODEC.listOf().optionalFieldOf(fieldName, Collections.emptyList()).xmap(cl -> cl.toArray(new Class[0]), Arrays::asList);
    }

    @SuppressWarnings("NullableProblems")
    public static <E> Codec<E> stringResolver(final Function<E, @Nullable String> toString, final Function<String, @Nullable E> fromString) {
        return Codec.stringResolver(toString, fromString);
    }
}
