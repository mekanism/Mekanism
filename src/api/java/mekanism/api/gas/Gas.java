package mekanism.api.gas;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Gas - a class used to set specific properties of gasses when used or seen in-game.
 *
 * @author aidancbrady
 */
//TODO: Add tags to gas
public class Gas implements IForgeRegistryEntry<Gas>, IHasTranslationKey, IGasProvider {

    private String translationKey;

    @Nonnull
    private Fluid fluid = Fluids.EMPTY;
    private ResourceLocation iconLocation;
    private TextureAtlasSprite sprite;

    private ResourceLocation registryName;

    private boolean visible = true;
    private boolean from_fluid = false;

    private int tint = 0xFFFFFF;

    /**
     * Creates a new Gas object with a defined name or key value.
     *
     * @param registryName - name or key to associate this Gas with
     */
    public Gas(ResourceLocation registryName, ResourceLocation icon) {
        this.registryName = registryName;
        iconLocation = icon;
        translationKey = Util.makeTranslationKey("gas", getRegistryName());
    }

    /**
     * Creates a new Gas object with a defined name or key value and a specified color tint.
     *
     * @param registryName - name or key to associate this Gas with
     * @param t - tint of this Gas
     */
    public Gas(ResourceLocation registryName, int t) {
        this(registryName, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid"));
        setTint(t);
    }

    /**
     * Creates a new Gas object that corresponds to the given Fluid
     */
    public Gas(@Nonnull Fluid fluid) {
        registryName = fluid.getRegistryName();
        iconLocation = fluid.getAttributes().getStillTexture();
        this.fluid = fluid;
        from_fluid = true;
        setTint(fluid.getAttributes().getColor() & 0xFFFFFF);
        translationKey = Util.makeTranslationKey("gas", getRegistryName());
    }

    /**
     * Returns the Gas stored in the defined tag compound.
     *
     * @param nbtTags - tag compound to get the Gas from
     *
     * @return Gas stored in the tag compound
     */
    public static Gas readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return null;
        }
        //TODO: Different string value
        return MekanismAPI.GAS_REGISTRY.getValue(new ResourceLocation(nbtTags.getString("gasName")));
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
     *
     * @return this Gas object
     */
    public Gas setVisible(boolean v) {
        visible = v;

        return this;
    }

    /**
     * Gets the unlocalized name of this Gas.
     *
     * @return this Gas's unlocalized name
     */
    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * Sets the unlocalized name of this Gas.
     *
     * @param key - unlocalized name to set
     *
     * @return this Gas object
     */
    public Gas setTranslationKey(String key) {
        translationKey = key;
        return this;
    }

    /**
     * Gets the IIcon associated with this Gas.
     *
     * @return associated IIcon
     */
    public ResourceLocation getIcon() {
        if (from_fluid) {
            return this.getFluid().getAttributes().getStillTexture();
        }

        return iconLocation;
    }

    /**
     * Gets the Sprite associated with this Gas.
     *
     * @return associated IIcon
     */
    public TextureAtlasSprite getSprite() {
        AtlasTexture texMap = Minecraft.getInstance().getTextureMap();
        if (from_fluid) {
            return texMap.getAtlasSprite(fluid.getAttributes().getStillTexture().toString());
        }

        if (sprite == null) {
            sprite = texMap.getAtlasSprite(getIcon().toString());
        }

        return sprite;
    }

    TextureAtlasSprite getSpriteRaw() {
        return sprite;
    }

    /**
     * Sets this gas's icon.
     *
     * @return this Gas object
     */
    public Gas registerIcon(TextureStitchEvent.Pre event) {
        event.addSprite(iconLocation);
        from_fluid = false;
        return this;
    }

    public Gas updateIcon(AtlasTexture map) {
        sprite = map.getSprite(iconLocation);
        return this;
    }

    /**
     * Writes this Gas to a defined tag compound.
     *
     * @param nbtTags - tag compound to write this Gas to
     *
     * @return the tag compound this gas was written to
     */
    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putString("gasName", getRegistryName().toString());
        return nbtTags;
    }

    /**
     * Whether or not this Gas has an associated fluid.
     *
     * @return if this gas has a fluid
     */
    public boolean hasFluid() {
        return fluid != Fluids.EMPTY;
    }

    /**
     * Gets the fluid associated with this Gas.
     *
     * @return fluid associated with this gas
     */
    @Nonnull
    @Override
    public Fluid getFluid() {
        return fluid;
    }

    /**
     * Registers a new fluid out of this Gas or gets one from the FluidRegistry. Uses same registry name as this.
     *
     * @return this Gas object
     */
    public Gas setFluid(Fluid fluid) {
        this.fluid = fluid;
        return this;
    }

    @Override
    public Gas setRegistryName(ResourceLocation name) {
        registryName = name;
        return this;
    }

    @Override
    public Gas getGas() {
        return this;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public Class<Gas> getRegistryType() {
        return Gas.class;
    }

    @Override
    public String toString() {
        //TODO: better to string representation
        return "Gas: " + getRegistryName();
    }

    /**
     * Get the tint for rendering the gas
     *
     * @return int representation of color in 0xRRGGBB format
     */
    public int getTint() {
        return tint;
    }

    /**
     * Sets the tint for the gas
     *
     * @param tint int representation of color in 0xRRGGBB format
     */
    public void setTint(int tint) {
        this.tint = tint;
    }
}