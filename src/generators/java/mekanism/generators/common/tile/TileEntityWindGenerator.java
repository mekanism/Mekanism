package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityWindGenerator extends TileEntityGenerator implements IBoundingBlock {

    public static final float SPEED = 32F;
    public static final float SPEED_SCALED = 256F / SPEED;

    private double angle;
    private FloatingLong currentMultiplier = FloatingLong.ZERO;
    private boolean isBlacklistDimension;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityWindGenerator() {
        super(GeneratorsBlocks.WIND_GENERATOR, MekanismGeneratorsConfig.generators.windGenerationMax.get().multiply(2));
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), this, 143, 35));
        return builder.build();
    }

    @Override
    protected RelativeSide[] getEnergySides() {
        return new RelativeSide[]{RelativeSide.FRONT, RelativeSide.BOTTOM};
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            // Check the blacklist and force an update if we're in the blacklist. Otherwise, we'll never send
            // an initial activity status and the client (in MP) will show the windmills turning while not
            // generating any power
            isBlacklistDimension = MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get().contains(level.dimension().location());
            if (isBlacklistDimension) {
                setActive(false);
            }
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.drainContainer();
        // If we're in a blacklisted dimension, there's nothing more to do
        if (isBlacklistDimension) {
            return;
        }
        if (ticker % 20 == 0) {
            // Recalculate the current multiplier once a second
            currentMultiplier = getMultiplier();
            setActive(MekanismUtils.canFunction(this) && !currentMultiplier.isZero());
        }
        if (!currentMultiplier.isZero() && MekanismUtils.canFunction(this) && !getEnergyContainer().getNeeded().isZero()) {
            getEnergyContainer().insert(MekanismGeneratorsConfig.generators.windGenerationMin.get().multiply(currentMultiplier), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            angle = (angle + (getBlockPos().getY() + 4F) / SPEED_SCALED) % 360;
        }
    }

    /**
     * Determines the current output multiplier, taking sky visibility and height into account.
     **/
    private FloatingLong getMultiplier() {
        if (level != null) {
            BlockPos top = getBlockPos().above(4);
            if (level.getFluidState(top).isEmpty() && level.canSeeSky(top)) {
                //Validate it isn't fluid logged to help try and prevent https://github.com/mekanism/Mekanism/issues/7344
                int minY = MekanismGeneratorsConfig.generators.windGenerationMinY.get();
                int maxY = MekanismGeneratorsConfig.generators.windGenerationMaxY.get();
                float clampedY = Math.min(maxY, Math.max(minY, top.getY()));
                FloatingLong minG = MekanismGeneratorsConfig.generators.windGenerationMin.get();
                FloatingLong maxG = MekanismGeneratorsConfig.generators.windGenerationMax.get();
                FloatingLong slope = maxG.subtract(minG).divide(maxY - minY);
                FloatingLong toGen = minG.add(slope.multiply(clampedY - minY));
                return toGen.divide(minG);
            }
        }
        return FloatingLong.ZERO;
    }

    @Override
    public void onPlace() {
        super.onPlace();
        if (level != null) {
            BlockPos pos = getBlockPos();
            WorldUtils.makeBoundingBlock(level, pos.above(), pos);
            WorldUtils.makeBoundingBlock(level, pos.above(2), pos);
            WorldUtils.makeBoundingBlock(level, pos.above(3), pos);
            WorldUtils.makeBoundingBlock(level, pos.above(4), pos);
            // Check to see if the placement is happening in a blacklisted dimension
            isBlacklistDimension = MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get().contains(level.dimension().location());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            level.removeBlock(getBlockPos().above(), false);
            level.removeBlock(getBlockPos().above(2), false);
            level.removeBlock(getBlockPos().above(3), false);
            level.removeBlock(getBlockPos().above(4), false);
        }
    }

    public FloatingLong getCurrentMultiplier() {
        return currentMultiplier;
    }

    public double getAngle() {
        return angle;
    }

    @ComputerMethod(nameOverride = "isBlacklistedDimension")
    public boolean isBlacklistDimension() {
        return isBlacklistDimension;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.WEATHER;
    }

    @Override
    public BlockPos getSoundPos() {
        return super.getSoundPos().above(4);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getCurrentMultiplier, value -> currentMultiplier = value));
        container.track(SyncableBoolean.create(this::isBlacklistDimension, value -> isBlacklistDimension = value));
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        //Note: we just extend it to the max size it could be ignoring what direction it is actually facing
        return new AxisAlignedBB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 7, 3));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private FloatingLong getProductionRate() {
        return getActive() ? MekanismGeneratorsConfig.generators.windGenerationMin.get().multiply(getCurrentMultiplier()) : FloatingLong.ZERO;
    }
    //End methods IComputerTile
}