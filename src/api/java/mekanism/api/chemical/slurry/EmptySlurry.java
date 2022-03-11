package mekanism.api.chemical.slurry;

import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraftforge.registries.tags.IReverseTag;

public final class EmptySlurry extends Slurry {

    public EmptySlurry() {
        super(SlurryBuilder.clean().hidden());
    }

    @Nonnull
    @Override
    protected Optional<IReverseTag<Slurry>> getReverseTag() {
        //Empty slurry is in no tags
        return Optional.empty();
    }
}