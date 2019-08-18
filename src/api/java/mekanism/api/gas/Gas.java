package mekanism.api.gas;

import mekanism.api.text.IHasTranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;

/**
 * Gas - a class used to set specific properties of gasses when used or seen in-game.
 *
 * @author aidancbrady
 */
public class Gas implements IHasTranslationKey {

    private final String name;

    private String unlocalizedName;

    private Fluid fluid;

    private ResourceLocation iconLocation;

    private TextureAtlasSprite sprite;

    private boolean visible = true;

    private boolean from_fluid = false;

    private int tint = 0xFFFFFF;

    /**
     * Creates a new Gas object with a defined name or key value.
     *
     * @param s - name or key to associate this Gas with
     */
    public Gas(String s, String icon) {
        this(s, new ResourceLocation(icon));
    }

    public Gas(String s, ResourceLocation icon) {
        unlocalizedName = name = s;
        iconLocation = icon;
    }

    /**
     * Creates a new Gas object with a defined name or key value and a specified color tint.
     *
     * @param s - name or key to associate this Gas with
     * @param t - tint of this Gas
     */
    public Gas(String s, int t) {
        this(s, "mekanism:blocks/liquid/liquid");
        setTint(t);
    }

    /**
     * Creates a new Gas object that corresponds to the given Fluid
     */
    public Gas(Fluid f) {
        unlocalizedName = name = f.getName();
        iconLocation = f.getStill();
        fluid = f;
        from_fluid = true;
        setTint(f.getColor() & 0xFFFFFF);
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

        return GasRegistry.getGas(nbtTags.getString("gasName"));
    }

    /**
     * Gets the name (key) of this Gas. This is NOT a translated or localized display name.
     *
     * @return this Gas's name or key
     */
    public String getName() {
        return name;
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
        return "gas." + unlocalizedName;
    }

    /**
     * Sets the unlocalized name of this Gas.
     *
     * @param s - unlocalized name to set
     *
     * @return this Gas object
     */
    public Gas setTranslationKey(String s) {
        unlocalizedName = s;
        return this;
    }

    /**
     * Gets the IIcon associated with this Gas.
     *
     * @return associated IIcon
     */
    public ResourceLocation getIcon() {
        if (from_fluid) {
            return this.getFluid().getStill();
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
            return texMap.getAtlasSprite(fluid.getStill().toString());
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
     * Gets the ID associated with this gas.
     *
     * @return the associated gas ID
     */
    public int getID() {
        return GasRegistry.getGasID(this);
    }

    /**
     * Writes this Gas to a defined tag compound.
     *
     * @param nbtTags - tag compound to write this Gas to
     *
     * @return the tag compound this gas was written to
     */
    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putString("gasName", getName());
        return nbtTags;
    }

    /**
     * Whether or not this Gas has an associated fluid.
     *
     * @return if this gas has a fluid
     */
    public boolean hasFluid() {
        return fluid != null;
    }

    /**
     * Gets the fluid associated with this Gas.
     *
     * @return fluid associated with this gas
     */
    public Fluid getFluid() {
        return fluid;
    }

    /**
     * Registers a new fluid out of this Gas or gets one from the FluidRegistry.
     *
     * @return this Gas object
     */
    public Gas registerFluid(String name) {
        if (fluid == null) {
            //TODO: Fluids
            /*Fluid fromRegistry = FluidRegistry.getFluid(name);
            if (fromRegistry == null) {
                int tint = getTint();
                //Fluids use ARGB so make sure that we are not using a fully transparent tint.
                // This fixes issues with some mods rendering our fluids as invisible
                if ((tint & 0xFF000000) == 0) {
                    tint = 0xFF000000 | tint;
                }
                FluidRegistry.registerFluid(fluid = new Fluid(name, getIcon(), getIcon(), tint));
            } else {
                fluid = fromRegistry;
            }*/
        }
        return this;
    }

    /**
     * Registers a new fluid out of this Gas or gets one from the FluidRegistry. Uses default gas name.
     *
     * @return this Gas object
     */
    public Gas registerFluid() {
        return registerFluid(getName());
    }

    @Override
    public String toString() {
        return name;
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