package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityRadioactiveWasteBarrel extends TileEntityMekanism {

    private static final float TOLERANCE = 0.05F;

    private IGasTank gasTank;
    private float prevScale;

    public TileEntityRadioactiveWasteBarrel() {
        super(MekanismBlocks.RADIOACTIVE_WASTE_BARREL);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSide(this::getDirection);
        builder.addTank(gasTank = ChemicalTankBuilder.GAS.create(MekanismConfig.general.radioactiveWasteBarrelMaxGas.get(),
              ChemicalAttributeValidator.createStrict(GasAttributes.Radiation.class), this),
              RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();

        float scale = getGasScale();
        if (Math.abs(scale - prevScale) > TOLERANCE) {
            sendUpdatePacket();
            prevScale = scale;
        }
    }

    public float getGasScale() {
        return (float) gasTank.getStored() / (float) gasTank.getCapacity();
    }

    public GasStack getGas() {
        return gasTank.getStack();
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.GAS_STORED, gasTank.serializeNBT());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.GAS_STORED, nbt -> gasTank.deserializeNBT(nbt));
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote() && gasTank.getStored() > 0) {
            // should always be true
            if (gasTank.getStack().has(GasAttributes.Radiation.class)) {
                double radioactivity = gasTank.getStack().get(GasAttributes.Radiation.class).getRadioactivity();
                Mekanism.radiationManager.radiate(Coord4D.get(this), gasTank.getStored() * radioactivity);
            }
        }
    }
}
