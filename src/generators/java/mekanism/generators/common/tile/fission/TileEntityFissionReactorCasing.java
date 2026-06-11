package mekanism.generators.common.tile.fission;

import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.lib.multiblock.MultiblockType;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.generators.common.content.MekanismGeneratorsMultiblocks;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    protected boolean onUpdateServer(ServerLevel level, FissionReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(level, multiblock);
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
    public MultiblockType<FissionReactorMultiblockData> getMultiblockType() {
        return MekanismGeneratorsMultiblocks.FISSION_REACTOR;
    }

    @Override
    protected boolean canPlaySound() {
        FissionReactorMultiblockData multiblock = getMultiblock();
        return multiblock.isFormed() && multiblock.isBurning() && handleSound;
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        FissionReactorMultiblockData multiblock = getMultiblock();
        output.putBoolean(SerializationConstants.HANDLE_SOUND, multiblock.isFormed() && multiblock.handlesSound(this));
        if (multiblock.isFormed()) {
            output.putDouble(SerializationConstants.BURNING, multiblock.lastBurnRate);
        }
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        FissionReactorMultiblockData multiblock = getMultiblock();
        //boolean prevFormedMaster = isMaster() && multiblock.isFormed();
        //UUID previousID = multiblock.inventoryID;
        super.handleUpdateTag(input);
        handleSound = input.getBooleanOr(SerializationConstants.HANDLE_SOUND, handleSound);
        //boolean formedMaster = false;
        //boolean wasBurning = false;
        if (multiblock.isFormed()) {
            //formedMaster = isMaster();
            //wasBurning = multiblock.isBurning();
            multiblock.lastBurnRate = input.getDoubleOr(SerializationConstants.BURNING, multiblock.lastBurnRate);
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