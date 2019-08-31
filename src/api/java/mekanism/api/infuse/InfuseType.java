package mekanism.api.infuse;

import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * The types of infuse currently available in Mekanism.
 *
 * @author AidanBrady
 */
//TODO: Promote infuse type to proper forge registry, and add tag support similar to how gases have Tag<Gas>
// Also allow for tints rather than just different textures
public final class InfuseType implements IForgeRegistryEntry<InfuseType>, IHasTranslationKey, IInfuseTypeProvider {

    /**
     * The name of this infusion.
     */
    public String name;

    /**
     * This infuse GUI's icon
     */
    public ResourceLocation iconResource;

    /**
     * The texture representing this infuse type.
     */
    public TextureAtlasSprite sprite;
    private String translationKey;
    private ResourceLocation registryName;
    //TODO: Actually use the tint
    private int tint;

    public InfuseType(ResourceLocation registryName, int tint) {
        //TODO: Make a default texture
        this(registryName, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infuse_type/generic"), tint);
    }

    public InfuseType(ResourceLocation registryName, ResourceLocation texture) {
        this(registryName, texture, -1);
    }

    public InfuseType(ResourceLocation registryName, ResourceLocation texture, int tint) {
        this.registryName = registryName;
        translationKey = Util.makeTranslationKey("infuse_type", getRegistryName());
        iconResource = texture;
        this.tint = tint;
    }

    public void setIcon(TextureAtlasSprite tex) {
        sprite = tex;
    }

    public InfuseType setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public InfuseType setRegistryName(ResourceLocation name) {
        //TODO: Check to make sure there is no name set and throw an error if there already is one set
        registryName = name;
        return this;
    }

    @Override
    public InfuseType getInfuseType() {
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public Class<InfuseType> getRegistryType() {
        return InfuseType.class;
    }

    public static InfuseType readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return null;
        }
        return MekanismAPI.INFUSE_TYPE_REGISTRY.getValue(new ResourceLocation(nbtTags.getString("infuseTypeName")));
    }

    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putString("infuseTypeName", getRegistryName().toString());
        return nbtTags;
    }
}