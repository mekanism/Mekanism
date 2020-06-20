package mekanism.generators.common.tile.fission;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityFissionReactorCasing extends TileEntityMultiblock<FissionReactorMultiblockData> {

    private boolean handleSound;
    private boolean prevBurning;

    public TileEntityFissionReactorCasing() {
        super(GeneratorsBlocks.FISSION_REACTOR_CASING);
    }

    public TileEntityFissionReactorCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean burning = getMultiblock().isFormed() && getMultiblock().handlesSound(this) && getMultiblock().isBurning();
        if (burning != prevBurning) {
            prevBurning = burning;
            sendUpdatePacket();
        }
    }

    public double getBoilEfficiency() {
        return (double) Math.round(getMultiblock().getBoilEfficiency() * 1_000) / 1_000;
    }

    public long getMaxBurnRate() {
        return getMultiblock().fuelAssemblies * FissionReactorMultiblockData.BURN_PER_ASSEMBLY;
    }

    public void setReactorActive(boolean active) {
        getMultiblock().setActive(active);
    }

    public String getDamageString() {
        return Math.round((getMultiblock().reactorDamage / FissionReactorMultiblockData.MAX_DAMAGE) * 100) + "%";
    }

    public EnumColor getDamageColor() {
        double damage = getMultiblock().reactorDamage / FissionReactorMultiblockData.MAX_DAMAGE;
        return damage < 0.25 ? EnumColor.BRIGHT_GREEN : (damage < 0.5 ? EnumColor.YELLOW : (damage < 0.75 ? EnumColor.ORANGE : EnumColor.DARK_RED));
    }

    public EnumColor getTempColor() {
        double temp = getMultiblock().heatCapacitor.getTemperature();
        return temp < 600 ? EnumColor.BRIGHT_GREEN : (temp < 1_000 ? EnumColor.YELLOW :
                                                      (temp < 1_200 ? EnumColor.ORANGE : (temp < 1_600 ? EnumColor.RED : EnumColor.DARK_RED)));
    }

    public void setRateLimitFromPacket(double rate) {
        getMultiblock().rateLimit = Math.min(getMaxBurnRate(), rate);
        markDirty(false);
    }

    @Override
    public FissionReactorMultiblockData createMultiblock() {
        return new FissionReactorMultiblockData(this);
    }

    @Override
    public MultiblockManager<FissionReactorMultiblockData> getManager() {
        return MekanismGenerators.fissionReactorManager;
    }

    @Override
    protected boolean canPlaySound() {
        return getMultiblock().isFormed() && getMultiblock().isBurning() && handleSound;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.HANDLE_SOUND, getMultiblock().isFormed() && getMultiblock().handlesSound(this));
        if (getMultiblock().isFormed()) {
            updateTag.putDouble(NBTConstants.BURNING, getMultiblock().lastBurnRate);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HANDLE_SOUND, value -> handleSound = value);
        if (getMultiblock().isFormed()) {
            NBTUtils.setDoubleIfPresent(tag, NBTConstants.BURNING, value -> getMultiblock().lastBurnRate = value);
        }
    }
}
