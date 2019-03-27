package mekanism.generators.common.tile;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityWindGenerator extends TileEntityGenerator implements IBoundingBlock {

    public static final float SPEED = 32F;
    public static final float SPEED_SCALED = 256F / SPEED;
    public static final int[] SLOTS = {0};
    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded",
          "getMultiplier"};
    /**
     * The angle the blades of this Wind Turbine are currently at.
     */
    public double angle;
    public float currentMultiplier;

    public TileEntityWindGenerator() {
        super("wind", "WindGenerator", 200000, (MekanismConfig.current().generators.windGenerationMax.val()) * 2);
        inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.charge(0, this);

            if (ticker % 20 == 0) {
                setActive((currentMultiplier = getMultiplier()) > 0);
            }

            if (getActive()) {
                setEnergy(electricityStored + (MekanismConfig.current().generators.windGenerationMin.val()
                      * currentMultiplier));
            }
        } else {
            if (getActive()) {
                angle = (angle + (getPos().getY() + 4F) / SPEED_SCALED) % 360;
            }
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (world.isRemote) {
            currentMultiplier = dataStream.readFloat();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(currentMultiplier);

        return data;
    }

    /**
     * Determines the current output multiplier, taking sky visibility and height into account.
     **/
    public float getMultiplier() {
        if (isInBlacklistedDimension()) {
            return 0;
        }
        if (world.canSeeSky(getPos().add(0, 4, 0))) {
            final float minY = (float) MekanismConfig.current().generators.windGenerationMinY.val();
            final float maxY = (float) MekanismConfig.current().generators.windGenerationMaxY.val();
            final float minG = (float) MekanismConfig.current().generators.windGenerationMin.val();
            final float maxG = (float) MekanismConfig.current().generators.windGenerationMax.val();

            final float slope = (maxG - minG) / (maxY - minY);
            final float intercept = minG - slope * minY;

            final float clampedY = Math.min(maxY, Math.max(minY, (float) (getPos().getY() + 4)));
            final float toGen = slope * clampedY + intercept;

            return toGen / minG;
        } else {
            return 0;
        }
    }

    public boolean isInBlacklistedDimension() {
        return MekanismConfig.current().generators.windGenerationBlacklist.val()
              .contains(world.provider.getDimension());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getVolume() {
        return 1.5F * super.getVolume();
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0:
                return new Object[]{electricityStored};
            case 1:
                return new Object[]{output};
            case 2:
                return new Object[]{BASE_MAX_ENERGY};
            case 3:
                return new Object[]{(BASE_MAX_ENERGY - electricityStored)};
            case 4:
                return new Object[]{getMultiplier()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public boolean canOperate() {
        return electricityStored < BASE_MAX_ENERGY && getMultiplier() > 0 && MekanismUtils.canFunction(this);
    }

    @Override
    public void onPlace() {
        Coord4D current = Coord4D.get(this);
        MekanismUtils.makeBoundingBlock(world, getPos().offset(EnumFacing.UP, 1), current);
        MekanismUtils.makeBoundingBlock(world, getPos().offset(EnumFacing.UP, 2), current);
        MekanismUtils.makeBoundingBlock(world, getPos().offset(EnumFacing.UP, 3), current);
        MekanismUtils.makeBoundingBlock(world, getPos().offset(EnumFacing.UP, 4), current);
    }

    @Override
    public void onBreak() {
        world.setBlockToAir(getPos().add(0, 1, 0));
        world.setBlockToAir(getPos().add(0, 2, 0));
        world.setBlockToAir(getPos().add(0, 3, 0));
        world.setBlockToAir(getPos().add(0, 4, 0));

        world.setBlockToAir(getPos());
    }

    @Override
    public boolean renderUpdate() {
        return false;
    }

    @Override
    public boolean lightUpdate() {
        return false;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return sideIsOutput(side) ? InventoryUtils.EMPTY : SLOTS;
    }
}
