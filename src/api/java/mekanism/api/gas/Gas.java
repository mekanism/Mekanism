package mekanism.api.gas;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IGasProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ReverseTagWrapper;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/**
 * Gas - a class used to set specific properties of gasses when used or seen in-game.
 *
 * @author aidancbrady
 */
//TODO: Add tags to gas
public class Gas extends Chemical<Gas> implements IGasProvider {

    private final ReverseTagWrapper<Gas> reverseTags = new ReverseTagWrapper<>(this, GasTags::getGeneration, GasTags::getCollection);

    //TODO: Make gas not be quite as "hard paired" with fluid as to directly know the reference fluid it is from
    // Instead use tags and giving the rotary condensentrator a recipe system. Instead make a helper that can create
    // a new gas or fluid given the other
    @Nonnull
    private Fluid fluid = Fluids.EMPTY;

    private boolean visible = true;
    private boolean from_fluid = false;

    private int tint = 0xFFFFFF;

    /**
     * Creates a new Gas object with a defined name or key value.
     *
     * @param registryName - name or key to associate this Gas with
     */
    public Gas(ResourceLocation registryName, ResourceLocation icon) {
        super(registryName, icon);
    }

    /**
     * Creates a new Gas object with a defined name or key value and a specified color tint.
     *
     * @param registryName - name or key to associate this Gas with
     * @param tint         - tint of this Gas
     */
    public Gas(ResourceLocation registryName, int tint) {
        //TODO: Rename the texture this points at
        this(registryName, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid"));
        setTint(tint);
    }

    /**
     * Creates a new Gas object that corresponds to the given Fluid
     */
    public Gas(@Nonnull Fluid fluid) {
        this(fluid.getRegistryName(), fluid.getAttributes().getStillTexture());
        this.fluid = fluid;
        from_fluid = true;
        setTint(fluid.getAttributes().getColor() & 0xFFFFFF);
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

    @Override
    public ResourceLocation getIcon() {
        if (from_fluid) {
            return this.getFluid().getAttributes().getStillTexture();
        }
        return super.getIcon();
    }

    @Override
    public void registerIcon(TextureStitchEvent.Pre event) {
        super.registerIcon(event);
        from_fluid = false;
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
     */
    public void setFluid(@Nonnull Fluid fluid) {
        //TODO: Don't allow setting it once it is already set?
        this.fluid = fluid;
    }

    public void createFluid() {
        int tint = getTint();
        //Fluids use ARGB so make sure that we are not using a fully transparent tint.
        // This fixes issues with some mods rendering our fluids as invisible
        if ((tint & 0xFF000000) == 0) {
            tint = 0xFF000000 | tint;
        }
        //TODO: Light?
        //TODO: Should we create both source and flowing?
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(() -> fluid, () -> fluid,
              FluidAttributes.builder(getIcon(), getIcon()).color(tint).gaseous());//.bucket(test_fluid_bucket).block(test_fluid_block);
        ForgeFlowingFluid flowingFluid = new ForgeFlowingFluid.Source(properties);
        flowingFluid.setRegistryName(getRegistryName());
        setFluid(flowingFluid);
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

    @Override
    public boolean isIn(Tag<Gas> tags) {
        return tags.contains(this);
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return reverseTags.getTagNames();
    }

    @Nonnull
    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeTranslationKey("gas", getRegistryName());
    }
}