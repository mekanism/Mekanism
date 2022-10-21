package mekanism.common.integration.projecte;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IGasProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Gas}s.
 */
public final class NSSGas extends AbstractNSSTag<Gas> {

    private NSSGas(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link GasStack}
     */
    @NotNull
    public static NSSGas createGas(@NotNull GasStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createGas(stack.getType());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from an {@link IGasProvider}
     */
    @NotNull
    public static NSSGas createGas(@NotNull IGasProvider gasProvider) {
        return createGas(gasProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link Gas}
     */
    @NotNull
    public static NSSGas createGas(@NotNull Gas gas) {
        if (gas.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSGas with an empty gas");
        }
        //This should never be null, or it would have crashed on being registered
        return createGas(gas.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSGas createGas(@NotNull ResourceLocation gasID) {
        return new NSSGas(gasID, false);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSGas createTag(@NotNull ResourceLocation tagId) {
        return new NSSGas(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a tag from a {@link TagKey<Gas>}
     */
    @NotNull
    public static NSSGas createTag(@NotNull TagKey<Gas> tag) {
        return createTag(tag.location());
    }

    @Override
    protected boolean isInstance(AbstractNSSTag o) {
        return o instanceof NSSGas;
    }

    @NotNull
    @Override
    public String getJsonPrefix() {
        return "GAS|";
    }

    @NotNull
    @Override
    public String getType() {
        return "Gas";
    }

    @NotNull
    @Override
    protected Optional<Either<Named<Gas>, ITag<Gas>>> getTag() {
        return getTag(MekanismAPI.gasRegistry());
    }

    @Override
    protected Function<Gas, NormalizedSimpleStack> createNew() {
        return NSSGas::createGas;
    }
}