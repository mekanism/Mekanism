package mekanism.api.chemical.pigment;

import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraftforge.registries.tags.IReverseTag;

public final class EmptyPigment extends Pigment {

    public EmptyPigment() {
        super(PigmentBuilder.builder().hidden());
    }

    @Nonnull
    @Override
    protected Optional<IReverseTag<Pigment>> getReverseTag() {
        //Empty pigment is in no tags
        return Optional.empty();
    }
}