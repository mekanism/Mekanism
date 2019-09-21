package mekanism.api.infuse;

import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.ReverseTagWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * The types of infuse currently available in Mekanism.
 *
 * @author AidanBrady
 */
//TODO: Allow for tints rather than just different textures
public class InfuseType extends ForgeRegistryEntry<InfuseType> implements IHasTranslationKey, IInfuseTypeProvider {

    private final ReverseTagWrapper<InfuseType> reverseTags = new ReverseTagWrapper<>(this, InfuseTypeTags::getGeneration, InfuseTypeTags::getCollection);

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
        setRegistryName(registryName);
        translationKey = Util.makeTranslationKey("infuse_type", getRegistryName());
        iconResource = texture;
        this.tint = tint;
    }

    public void setIcon(TextureAtlasSprite tex) {
        sprite = tex;
    }

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(getTranslationKey());
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Nonnull
    @Override
    public InfuseType getInfuseType() {
        return this;
    }

    @Nonnull
    public static InfuseType readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return MekanismAPI.EMPTY_INFUSE_TYPE;
        }
        InfuseType infuseType = MekanismAPI.INFUSE_TYPE_REGISTRY.getValue(new ResourceLocation(nbtTags.getString("infuseTypeName")));
        if (infuseType == null) {
            return MekanismAPI.EMPTY_INFUSE_TYPE;
        }
        return infuseType;
    }

    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putString("infuseTypeName", getRegistryName().toString());
        return nbtTags;
    }

    public boolean isIn(Tag<InfuseType> tags) {
        return tags.contains(this);
    }

    public Set<ResourceLocation> getTags() {
        return reverseTags.getTagNames();
    }
}