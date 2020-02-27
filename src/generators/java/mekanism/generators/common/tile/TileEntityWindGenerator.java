package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloat;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityWindGenerator extends TileEntityGenerator implements IBoundingBlock {

    public static final float SPEED = 32F;
    public static final float SPEED_SCALED = 256F / SPEED;
    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getMultiplier"};

    private double angle;
    private float currentMultiplier;
    private boolean isBlacklistDimension;

    private EnergyInventorySlot energySlot;

    public TileEntityWindGenerator() {
        super(GeneratorsBlocks.WIND_GENERATOR, MekanismGeneratorsConfig.generators.windGenerationMax.get() * 2);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.charge(this, 143, 35));
        return builder.build();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        World world = getWorld();
        if (world == null) {
            return;
        }
        // Check the blacklist and force an update if we're in the blacklist. Otherwise, we'll never send
        // an initial activity status and the client (in MP) will show the windmills turning while not
        // generating any power
        isBlacklistDimension = MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get().contains(world.getDimension().getType().getRegistryName().toString());
        if (isBlacklistDimension) {
            setActive(false);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!isRemote()) {
            energySlot.charge(this);
            // If we're in a blacklisted dimension, there's nothing more to do
            if (isBlacklistDimension) {
                return;
            }
            if (ticker % 20 == 0) {
                // Recalculate the current multiplier once a second
                currentMultiplier = getMultiplier();
                setActive(currentMultiplier > 0);
            }
            if (getActive()) {
                setEnergy(getEnergy() + (MekanismGeneratorsConfig.generators.windGenerationMin.get() * currentMultiplier));
            }
        } else if (getActive()) {
            angle = (angle + (getPos().getY() + 4F) / SPEED_SCALED) % 360;
        }
    }

    /**
     * Determines the current output multiplier, taking sky visibility and height into account.
     **/
    private float getMultiplier() {
        World world = getWorld();
        if (world != null && world.canBlockSeeSky(getPos().up(4))) {
            final float minY = MekanismGeneratorsConfig.generators.windGenerationMinY.get();
            final float maxY = MekanismGeneratorsConfig.generators.windGenerationMaxY.get();
            final float minG = (float) MekanismGeneratorsConfig.generators.windGenerationMin.get();
            final float maxG = (float) MekanismGeneratorsConfig.generators.windGenerationMax.get();
            final float slope = (maxG - minG) / (maxY - minY);
            final float intercept = minG - slope * minY;
            final float clampedY = Math.min(maxY, Math.max(minY, (float) (getPos().getY() + 4)));
            final float toGen = slope * clampedY + intercept;
            return toGen / minG;
        }
        return 0;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{output};
            case 2:
                return new Object[]{getBaseStorage()};
            case 3:
                return new Object[]{getBaseStorage() - getEnergy()};
            case 4:
                return new Object[]{getMultiplier()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && getMultiplier() > 0 && MekanismUtils.canFunction(this);
    }

    @Override
    public void onPlace() {
        World world = getWorld();
        if (world == null) {
            return;
        }
        BlockPos pos = getPos();
        MekanismUtils.makeBoundingBlock(world, pos.up(), pos);
        MekanismUtils.makeBoundingBlock(world, pos.up(2), pos);
        MekanismUtils.makeBoundingBlock(world, pos.up(3), pos);
        MekanismUtils.makeBoundingBlock(world, pos.up(4), pos);
        // Check to see if the placement is happening in a blacklisted dimension
        isBlacklistDimension = MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get().contains(world.getDimension().getType().getRegistryName().toString());
    }

    @Override
    public void onBreak() {
        World world = getWorld();
        if (world == null) {
            return;
        }
        world.removeBlock(getPos().add(0, 1, 0), false);
        world.removeBlock(getPos().add(0, 2, 0), false);
        world.removeBlock(getPos().add(0, 3, 0), false);
        world.removeBlock(getPos().add(0, 4, 0), false);
        world.removeBlock(getPos(), false);
    }

    public float getCurrentMultiplier() {
        return currentMultiplier;
    }

    public double getAngle() {
        return angle;
    }

    public boolean isBlacklistDimension() {
        return isBlacklistDimension;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.WEATHER;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloat.create(this::getCurrentMultiplier, value -> currentMultiplier = value));
        container.track(SyncableBoolean.create(this::isBlacklistDimension, value -> isBlacklistDimension = value));
    }
}