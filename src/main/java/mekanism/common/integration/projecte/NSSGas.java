package mekanism.common.integration.projecte;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IGasProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;


/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Gas}s.
 */
public final class NSSGas extends AbstractNSSTag<Gas> {

    private NSSGas(@Nonnull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link GasStack}
     */
    @Nonnull
    public static NSSGas createGas(@Nonnull GasStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyways for being empty
        return createGas(stack.getType());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from an {@link IGasProvider}
     */
    @Nonnull
    public static NSSGas createGas(@Nonnull IGasProvider gasProvider) {
        return createGas(gasProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link Gas}
     */
    @Nonnull
    public static NSSGas createGas(@Nonnull Gas gas) {
        if (gas.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSGas with an empty gas");
        }
        //This should never be null or it would have crashed on being registered
        return createGas(gas.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link ResourceLocation}
     */
    @Nonnull
    public static NSSGas createGas(@Nonnull ResourceLocation gasID) {
        return new NSSGas(gasID, false);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a tag from a {@link ResourceLocation}
     */
    @Nonnull
    public static NSSGas createTag(@Nonnull ResourceLocation tagId) {
        return new NSSGas(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a tag from a {@link Tag<Gas>}
     */
    @Nonnull
    public static NSSGas createTag(@Nonnull ITag<Gas> tag) {
        return createTag(ChemicalTags.GAS.lookupTag(tag));
    }

    @Override
    protected boolean isInstance(AbstractNSSTag o) {
        return o instanceof NSSGas;
    }

    @Nonnull
    @Override
    public String getJsonPrefix() {
        return "GAS|";
    }

    @Nonnull
    @Override
    public String getType() {
        return "Gas";
    }

    @Nonnull
    @Override
    protected ITagCollection<Gas> getTagCollection() {
        return ChemicalTags.GAS.getCollection();
    }

    @Override
    protected Function<Gas, NormalizedSimpleStack> createNew() {
        return NSSGas::createGas;
    }
}