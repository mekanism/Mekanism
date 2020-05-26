package mekanism.generators.common.tile.fission;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityFissionReactorCasing extends TileEntityMultiblock<FissionReactorMultiblockData> implements IValveHandler {

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
        return (double) Math.round(getMultiblock().getBoilEfficiency() * 1000) / 1000;
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
        return temp < 600 ? EnumColor.BRIGHT_GREEN : (temp < 1000 ? EnumColor.YELLOW :
                                                      (temp < 1200 ? EnumColor.ORANGE : (temp < 1600 ? EnumColor.RED : EnumColor.DARK_RED)));
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
            if (isMaster) {
                updateTag.putFloat(NBTConstants.SCALE, getMultiblock().prevCoolantScale);
                updateTag.putFloat(NBTConstants.SCALE_ALT, getMultiblock().prevFuelScale);
                updateTag.putFloat(NBTConstants.SCALE_ALT_2, getMultiblock().prevHeatedCoolantScale);
                updateTag.putFloat(NBTConstants.SCALE_ALT_3, getMultiblock().prevWasteScale);
                updateTag.putInt(NBTConstants.VOLUME, getMultiblock().getVolume());
                updateTag.put(NBTConstants.FLUID_STORED, getMultiblock().fluidCoolantTank.getFluid().writeToNBT(new CompoundNBT()));
                updateTag.put(NBTConstants.GAS_STORED, getMultiblock().fuelTank.getStack().write(new CompoundNBT()));
                updateTag.put(NBTConstants.GAS_STORED_ALT, getMultiblock().heatedCoolantTank.getStack().write(new CompoundNBT()));
                updateTag.put(NBTConstants.GAS_STORED_ALT_2, getMultiblock().wasteTank.getStack().write(new CompoundNBT()));
                writeValves(updateTag);
                ListNBT list = new ListNBT();
                getMultiblock().assemblies.forEach(assembly -> list.add(assembly.write()));
                updateTag.put(NBTConstants.ASSEMBLIES, list);
            }
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HANDLE_SOUND, value -> handleSound = value);
        if (getMultiblock().isFormed()) {
            NBTUtils.setDoubleIfPresent(tag, NBTConstants.BURNING, value -> getMultiblock().lastBurnRate = value);
            if (isMaster) {
                NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> getMultiblock().prevCoolantScale = scale);
                NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> getMultiblock().prevFuelScale = scale);
                NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT_2, scale -> getMultiblock().prevHeatedCoolantScale = scale);
                NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT_3, scale -> getMultiblock().prevWasteScale = scale);
                NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> getMultiblock().setVolume(value));
                NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> getMultiblock().fluidCoolantTank.setStack(value));
                NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> getMultiblock().fuelTank.setStack(value));
                NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED_ALT, value -> getMultiblock().heatedCoolantTank.setStack(value));
                NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED_ALT_2, value -> getMultiblock().wasteTank.setStack(value));
                readValves(tag);
                getMultiblock().assemblies.clear();
                if (tag.contains(NBTConstants.ASSEMBLIES)) {
                    ListNBT list = tag.getList(NBTConstants.ASSEMBLIES, NBT.TAG_COMPOUND);
                    for (int i = 0; i < list.size(); i++) {
                        getMultiblock().assemblies.add(FormedAssembly.read(list.getCompound(i)));
                    }
                }
            }
        }
    }
}
