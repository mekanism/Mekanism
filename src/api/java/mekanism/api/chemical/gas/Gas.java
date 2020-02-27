package mekanism.api.chemical.gas;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IGasProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.common.util.ReverseTagWrapper;

/**
 * Gas - a class used to set specific properties of gases when used or seen in-game.
 *
 * @author aidancbrady
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Gas extends Chemical<Gas> implements IGasProvider {

    private final ReverseTagWrapper<Gas> reverseTags = new ReverseTagWrapper<>(this, GasTags::getGeneration, GasTags::getCollection);

    private boolean hidden;

    public Gas(GasAttributes attributes) {
        super(attributes);
        hidden = attributes.isHidden();
    }

    /**
     * Returns the Gas stored in the defined tag compound.
     *
     * @param nbtTags - tag compound to get the Gas from
     *
     * @return Gas stored in the tag compound
     */
    public static Gas readFromNBT(@Nullable CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return MekanismAPI.EMPTY_GAS;
        }
        return getFromRegistry(new ResourceLocation(nbtTags.getString("gasName")));
    }

    public static Gas getFromRegistry(@Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return MekanismAPI.EMPTY_GAS;
        }
        Gas gas = MekanismAPI.GAS_REGISTRY.getValue(resourceLocation);
        if (gas == null) {
            return MekanismAPI.EMPTY_GAS;
        }
        return gas;
    }

    /**
     * Whether or not this gas is hidden.
     *
     * @return if this gas is hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Writes this Gas to a defined tag compound.
     *
     * @param nbtTags - tag compound to write this Gas to
     *
     * @return the tag compound this gas was written to
     */
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putString("gasName", getRegistryName().toString());
        return nbtTags;
    }

    @Override
    public Gas getGas() {
        return this;
    }

    @Override
    public String toString() {
        //TODO: better to string representation
        return "Gas: " + getRegistryName();
    }

    @Override
    public boolean isIn(@Nonnull Tag<Gas> tag) {
        return tag.contains(this);
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return reverseTags.getTagNames();
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_GAS;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeTranslationKey("gas", getRegistryName());
    }
}