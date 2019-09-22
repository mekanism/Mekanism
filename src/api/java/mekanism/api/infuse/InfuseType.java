package mekanism.api.infuse;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
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
//TODO: Allow for tints rather than just different textures
public class InfuseType extends Chemical<InfuseType> implements IInfuseTypeProvider {

    private final ReverseTagWrapper<InfuseType> reverseTags = new ReverseTagWrapper<>(this, InfuseTypeTags::getGeneration, InfuseTypeTags::getCollection);

    private int tint;

    public InfuseType(ResourceLocation registryName, int tint) {
        //TODO: Make a default texture
        this(registryName, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infuse_type/generic"), tint);
    }

    public InfuseType(ResourceLocation registryName, ResourceLocation texture) {
        this(registryName, texture, -1);
    }

    public InfuseType(ResourceLocation registryName, ResourceLocation iconLocation, int tint) {
        super(registryName, iconLocation);
        this.tint = tint;
    }

    @Nonnull
    public static InfuseType readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return MekanismAPI.EMPTY_INFUSE_TYPE;
        }
        return getFromRegistry(new ResourceLocation(nbtTags.getString("infuseTypeName")));
    }

    @Nonnull
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

    public int getTint() {
        return tint;
    }

    @Nonnull
    @Override
    public InfuseType getInfuseType() {
        return this;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putString("infuseTypeName", getRegistryName().toString());
        return nbtTags;
    }

    @Override
    public boolean isIn(Tag<InfuseType> tags) {
        return tags.contains(this);
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return reverseTags.getTagNames();
    }

    @Nonnull
    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeTranslationKey("infuse_type", getRegistryName());
    }
}