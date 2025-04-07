package mekanism.common.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.MethodRestriction;
import net.neoforged.neoforge.fluids.FluidStack;

public class MekCodecs {

    public static final Codec<MethodRestriction> METHOD_RESTRICTION_CODEC = Codec.stringResolver(MethodRestriction::name, MethodRestriction::valueOf);
    public static final Codec<Class<?>> CLASS_TO_STRING_CODEC = Codec.stringResolver(Class::getName, s -> {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            return null;
        }
    });

    public static final Codec<Object> FLUID_OR_CHEMICAL_STACK = Codec.xor(
          FluidStack.CODEC.fieldOf(SerializationConstants.FLUID).codec(),
          ChemicalStack.CODEC.fieldOf(SerializationConstants.CHEMICAL).codec()
    ).flatComapMap(Either::unwrap, stack -> switch (stack) {
        case FluidStack fluid -> DataResult.success(Either.left(fluid));
        case ChemicalStack chemical -> DataResult.success(Either.right(chemical));
        default -> DataResult.error(() -> "Bad stack: expected fluid or chemical, got " + stack);
    });

    //TODO - 1.22: remove backcompat
    @Deprecated(forRemoval = true, since = "10.7.13")
    public static final Codec<Object> FLUID_OR_CHEMICAL_STACK_LEGACY = Codec.withAlternative(FLUID_OR_CHEMICAL_STACK, ChemicalStack.CODEC);

    public static MapCodec<Class<?>[]> optionalClassArrayCodec(String fieldName) {
        return CLASS_TO_STRING_CODEC.listOf().optionalFieldOf(fieldName, Collections.emptyList()).xmap(cl -> cl.toArray(new Class[0]), Arrays::asList);
    }

    public static <B, L extends B, R extends B> MapCodec<B> alternativeElement(MapCodec<L> leftBase, MapCodec<R> rightBase,
                                                                               final Function<? super B, ? extends DataResult<? extends Either<L, R>>> from) {
        MapCodec<Either<L, R>> base = Codec.mapEither(leftBase, rightBase);
        return Codec.of(base.flatComap(from), base.map(Either::unwrap), () -> base + "[flatComapMapped]");
    }
}
