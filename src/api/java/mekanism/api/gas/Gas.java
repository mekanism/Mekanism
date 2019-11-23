package mekanism.api.gas;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IGasProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.common.util.ReverseTagWrapper;

/**
 * Gas - a class used to set specific properties of gasses when used or seen in-game.
 *
 * @author aidancbrady
 */
public class Gas extends Chemical<Gas> implements IGasProvider {

    private final ReverseTagWrapper<Gas> reverseTags = new ReverseTagWrapper<>(this, GasTags::getGeneration, GasTags::getCollection);

    private boolean visible = true;

    public Gas(ResourceLocation icon) {
        super(icon);
    }

    /**
     * Creates a new Gas object with a specified color tint.
     *
     * @param tint         - tint of this Gas
     */
    public Gas(int tint) {
        //TODO: Rename the texture this points at
        this(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid"));
        setTint(tint);
    }

    /**
     * Returns the Gas stored in the defined tag compound.
     *
     * @param nbtTags - tag compound to get the Gas from
     *
     * @return Gas stored in the tag compound
     */
    @Nonnull
    public static Gas readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return MekanismAPI.EMPTY_GAS;
        }
        return getFromRegistry(new ResourceLocation(nbtTags.getString("gasName")));
    }

    @Nonnull
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
     * Whether or not this is a visible gas.
     *
     * @return if this gas is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets this gas's "visible" state to a new value. Setting it to 'false' will treat this gas as an internal gas, and it will not be displayed or accessed by other
     * mods.
     *
     * @param v - new visible state
     */
    public void setVisible(boolean v) {
        visible = v;
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

    @Nonnull
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
    public boolean isIn(Tag<Gas> tag) {
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

    @Nonnull
    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeTranslationKey("gas", getRegistryName());
    }
}