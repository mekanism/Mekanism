package mekanism.api.chemical.gas;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IGasProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Gas - a class used to set specific properties of gases when used or seen in-game.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public class Gas extends Chemical<Gas> implements IGasProvider {

    public static final Codec<Gas> CODEC = MekanismAPI.GAS_REGISTRY.byNameCodec();

    public Gas(GasBuilder builder) {
        super(builder);
    }

    /**
     * Returns the Gas stored in the defined tag compound.
     *
     * @param nbtTags - tag compound to get the Gas from
     *
     * @return Gas stored in the tag compound
     */
    public static Gas readFromNBT(@Nullable CompoundTag nbtTags) {
        return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_GAS, NBTConstants.GAS_NAME, Gas::getFromRegistry);
    }

    public static Gas getFromRegistry(@Nullable ResourceLocation name) {
        return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_GAS, MekanismAPI.GAS_REGISTRY);
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        nbtTags.putString(NBTConstants.GAS_NAME, getRegistryName().toString());
        return nbtTags;
    }

    @Override
    public String toString() {
        return "[Gas: " + getRegistryName() + "]";
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_GAS;
    }

    @Override
    protected final Registry<Gas> getRegistry() {
        return MekanismAPI.GAS_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("gas", getRegistryName());
    }
}