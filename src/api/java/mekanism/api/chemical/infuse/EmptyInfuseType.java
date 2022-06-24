package mekanism.api.chemical.infuse;

import java.util.Optional;
import net.minecraftforge.registries.tags.IReverseTag;
import org.jetbrains.annotations.NotNull;

public final class EmptyInfuseType extends InfuseType {

    public EmptyInfuseType() {
        super(InfuseTypeBuilder.builder().hidden());
    }

    @NotNull
    @Override
    protected Optional<IReverseTag<InfuseType>> getReverseTag() {
        //Empty infuse type is in no tags
        return Optional.empty();
    }
}