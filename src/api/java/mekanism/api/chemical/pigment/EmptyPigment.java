package mekanism.api.chemical.pigment;

import java.util.Optional;
import net.minecraftforge.registries.tags.IReverseTag;
import org.jetbrains.annotations.NotNull;

public final class EmptyPigment extends Pigment {

    public EmptyPigment() {
        super(PigmentBuilder.builder().hidden());
    }

    @NotNull
    @Override
    protected Optional<IReverseTag<Pigment>> getReverseTag() {
        //Empty pigment is in no tags
        return Optional.empty();
    }
}