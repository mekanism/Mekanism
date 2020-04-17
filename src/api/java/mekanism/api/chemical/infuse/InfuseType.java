package mekanism.api.chemical.infuse;

import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.common.util.ReverseTagWrapper;

/**
 * The types of infuse currently available in Mekanism.
 *
 * @author AidanBrady
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfuseType extends Chemical<InfuseType> implements IInfuseTypeProvider {

    private final ReverseTagWrapper<InfuseType> reverseTags = new ReverseTagWrapper<>(this, InfuseTypeTags::getGeneration, InfuseTypeTags::getCollection);

    public InfuseType(InfuseTypeBuilder builder) {
        super(builder);
    }

    public static InfuseType readFromNBT(@Nullable CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return MekanismAPI.EMPTY_INFUSE_TYPE;
        }
        return getFromRegistry(new ResourceLocation(nbtTags.getString(NBTConstants.INFUSE_TYPE_NAME)));
    }

    public static InfuseType getFromRegistry(@Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return MekanismAPI.EMPTY_INFUSE_TYPE;
        }
        InfuseType infuseType = MekanismAPI.INFUSE_TYPE_REGISTRY.getValue(resourceLocation);
        if (infuseType == null) {
            return MekanismAPI.EMPTY_INFUSE_TYPE;
        }
        return infuseType;
    }

    @Override
    public InfuseType getInfuseType() {
        return this;
    }

    @Override
    public String toString() {
        return "[InfuseType: " + getRegistryName() + "]";
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putString(NBTConstants.INFUSE_TYPE_NAME, getRegistryName().toString());
        return nbtTags;
    }

    @Override
    public boolean isIn(Tag<InfuseType> tag) {
        return tag.contains(this);
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return reverseTags.getTagNames();
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_INFUSE_TYPE;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeTranslationKey("infuse_type", getRegistryName());
    }
}