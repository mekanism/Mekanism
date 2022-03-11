package mekanism.api.chemical.infuse;

import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraftforge.registries.tags.IReverseTag;

public final class EmptyInfuseType extends InfuseType {

    public EmptyInfuseType() {
        super(InfuseTypeBuilder.builder().hidden());
    }

    @Nonnull
    @Override
    protected Optional<IReverseTag<InfuseType>> getReverseTag() {
        //Empty infuse type is in no tags
        return Optional.empty();
    }
}