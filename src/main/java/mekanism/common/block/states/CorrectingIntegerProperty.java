package mekanism.common.block.states;

import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.state.IntegerProperty;

@Deprecated//TODO - 1.17: Remove this
public class CorrectingIntegerProperty extends IntegerProperty {

    public static CorrectingIntegerProperty create(@Nonnull String name, int min, int max) {
        return new CorrectingIntegerProperty(name, min, max);
    }

    protected CorrectingIntegerProperty(@Nonnull String name, int min, int max) {
        super(name, min, max);
    }

    @Nonnull
    @Override
    public Optional<Integer> parseValue(String value) {
        //Handle loading the old "boolean" value data type
        if (value.equals("false")) {
            return Optional.of(0);
        } else if (value.equals("true")) {
            return Optional.of(1);
        }
        return super.parseValue(value);
    }
}