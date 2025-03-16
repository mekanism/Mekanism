package mekanism.generators.common.tile.fission;

import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityFissionReactorCasing extends TileEntityMultiblock<FissionReactorMultiblockData> {

    private boolean handleSound;
    private boolean prevBurning;

    public TileEntityFissionReactorCasing(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FISSION_REACTOR_CASING, pos, state);
    }

    public TileEntityFissionReactorCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    protected boolean onUpdateServer(FissionReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        boolean burning = multiblock.isFormed() && multiblock.handlesSound(this) && multiblock.isBurning();
        if (burning != prevBurning) {
            prevBurning = burning;
            needsPacket = true;
        }
        return needsPacket;
    }

    public double getBoilEfficiency() {
        return (double) Math.round(getMultiblock().getBoilEfficiency() * 1_000) / 1_000;
    }

    public void setReactorActive(boolean active) {
        getMultiblock().setActive(active);
    }

    public Component getDamageString() {
        return MekanismLang.GENERIC_PERCENT.translate(getMultiblock().getDamagePercent());
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
        getMultiblock().setRateLimit(rate);
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

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        FissionReactorMultiblockData multiblock = getMultiblock();
        updateTag.putBoolean(SerializationConstants.HANDLE_SOUND, multiblock.isFormed() && multiblock.handlesSound(this));
        if (multiblock.isFormed()) {
            updateTag.putDouble(SerializationConstants.BURNING, multiblock.lastBurnRate);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        FissionReactorMultiblockData multiblock = getMultiblock();
        //boolean prevFormedMaster = isMaster() && multiblock.isFormed();
        //UUID previousID = multiblock.inventoryID;
        super.handleUpdateTag(tag, provider);
        NBTUtils.setBooleanIfPresent(tag, SerializationConstants.HANDLE_SOUND, value -> handleSound = value);
        //boolean formedMaster = false;
        //boolean wasBurning = false;
        if (multiblock.isFormed()) {
            //formedMaster = isMaster();
            //wasBurning = multiblock.isBurning();
            NBTUtils.setDoubleIfPresent(tag, SerializationConstants.BURNING, value -> multiblock.lastBurnRate = value);
        }
        //TODO: At some point make use of this if we are able to use the FuelAssemblyBakedModel?
        /*boolean sameID = Objects.equals(previousID, multiblock.inventoryID);
        if (formedMaster != prevFormedMaster || !sameID) {
            //If our master or formed status changed or our multiblock's id changed
            if (prevFormedMaster || !sameID) {
                // remove this block as being the master if it was formed or master and isn't now or if the id changed
                TileEntityFissionAssembly.removeMultiblockMaster(previousID, this);
            }
            if (formedMaster) {
                // add this block as being the master if it now is formed and the master and either wasn't before or had a different id
                TileEntityFissionAssembly.updateMultiblockMaster(multiblock.inventoryID, this);
            }
        } else if (formedMaster && wasBurning != multiblock.isBurning()) {
            // Otherwise, if we are the master and the burning status changed for this multiblock.
            // Update the elements in it so that they change active/inactive state
            TileEntityFissionAssembly.updateMultiblockMaster(multiblock.inventoryID, this);
        }*/
    }
}