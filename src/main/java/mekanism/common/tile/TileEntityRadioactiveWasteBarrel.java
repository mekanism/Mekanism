package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityRadioactiveWasteBarrel extends TileEntityMekanism {

    private static final float TOLERANCE = 0.05F;
    private static final int MAX_PROCESS_TICKS = 1200;

    private long lastProcessTick;
    private IGasTank gasTank;
    private float prevScale;
    private int processTicks;

    public TileEntityRadioactiveWasteBarrel() {
        super(MekanismBlocks.RADIOACTIVE_WASTE_BARREL);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSide(this::getDirection);
        builder.addTank(gasTank = ChemicalTankBuilder.GAS.createWithValidator(MekanismConfig.general.radioactiveWasteBarrelMaxGas.get(),
              ChemicalAttributeValidator.createStrict(GasAttributes.Radiation.class), this),
              RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        if (world.getGameTime() > lastProcessTick) {
            //If we are not on the same tick do stuff, otherwise ignore it (anti tick accellerator protection)
            lastProcessTick = world.getGameTime();
            if (++processTicks >= MAX_PROCESS_TICKS) {
                processTicks = 0;
                gasTank.shrinkStack(1, Action.EXECUTE);
            }

            float scale = getGasScale();
            if (Math.abs(scale - prevScale) > TOLERANCE) {
                sendUpdatePacket();
                prevScale = scale;
            }
        }
    }

    public float getGasScale() {
        return gasTank.getStored() / (float) gasTank.getCapacity();
    }

    public GasStack getGas() {
        return gasTank.getStack();
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.GAS_STORED, gasTank.serializeNBT());
        updateTag.putInt(NBTConstants.PROGRESS, processTicks);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.GAS_STORED, nbt -> gasTank.deserializeNBT(nbt));
        NBTUtils.setIntIfPresent(tag, NBTConstants.PROGRESS, val -> processTicks = val);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
    }
}