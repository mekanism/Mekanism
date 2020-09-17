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
import net.minecraft.block.BlockState;
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
    protected void onUpdateServer(FissionReactorMultiblockData multiblock) {
        super.onUpdateServer(multiblock);
        boolean burning = multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.isBurning();
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
        FissionReactorMultiblockData multiblock = getMultiblock();
        return multiblock.isFormed() && multiblock.isBurning() && handleSound;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        FissionReactorMultiblockData multiblock = getMultiblock();
        updateTag.putBoolean(NBTConstants.HANDLE_SOUND, multiblock.isFormed() && multiblock.handlesSound(this));
        if (multiblock.isFormed()) {
            updateTag.putDouble(NBTConstants.BURNING, multiblock.lastBurnRate);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HANDLE_SOUND, value -> handleSound = value);
        FissionReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            NBTUtils.setDoubleIfPresent(tag, NBTConstants.BURNING, value -> multiblock.lastBurnRate = value);
        }
    }
}
