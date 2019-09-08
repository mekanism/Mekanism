package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.cache.CachedRecipe;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public abstract class TileEntityBasicMachine<RECIPE extends IMekanismRecipe> extends TileEntityOperationalMachine implements IComputerIntegration, ISideConfiguration,
      IConfigCardAccess {

    public ResourceLocation guiLocation;

    public CachedRecipe<RECIPE> cachedRecipe = null;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    /**
     * The foundation of all machines - a simple tile entity with a facing, active state, initialized state, sound effect, and animated texture.
     *
     * @param soundPath         - location of the sound effect
     * @param type              - the type of this machine
     * @param baseTicksRequired - how many ticks it takes to run a cycle
     */
    public TileEntityBasicMachine(String soundPath, MachineType type, int upgradeSlot, int baseTicksRequired, ResourceLocation location) {
        super("machine." + soundPath, type, upgradeSlot, baseTicksRequired);
        guiLocation = location;
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
        return configComponent.hasSideForData(TransmissionType.ENERGY, facing, 1, side);
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, facing).availableSlots;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public EnumFacing getOrientation() {
        return facing;
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        return configComponent.isCapabilityDisabled(capability, side, facing) || super.isCapabilityDisabled(capability, side);
    }

    //TODO: Move the declaration of this stuff to an interface
    @Nonnull
    public abstract Recipe<RECIPE> getRecipes();

    //TODO: Maybe rename this method
    @Nullable
    protected CachedRecipe<RECIPE> getOrFindCachedRecipe() {
        //If there is no cached recipe or the input doesn't match, attempt to get the recipe based on the input
        if (cachedRecipe == null || !cachedRecipe.hasResourcesForTick()) {
            //TODO: Should this use a separate method than hasResourcesForTick?
            RECIPE recipe = getRecipe();
            if (recipe != null) {
                CachedRecipe<RECIPE> cached = createNewCachedRecipe(recipe);
                if (cachedRecipe == null || cached != null) {
                    //Only override our cached recipe if we were able to find a recipe that matches, or we don't have a cached recipe.
                    // This way if we end up getting back to the same recipe we won't have to recalculate quite as much
                    cachedRecipe = cached;
                }
            }
        }
        return cachedRecipe;
    }

    //TODO: JavaDoc this and other recipe methods. This one gets recipe from inputs in machine ignoring what is currently cached
    // mainly for purposes of creating the new cached recipe
    @Nullable
    protected abstract RECIPE getRecipe();

    protected abstract CachedRecipe<RECIPE> createNewCachedRecipe(@Nonnull RECIPE recipe);
}