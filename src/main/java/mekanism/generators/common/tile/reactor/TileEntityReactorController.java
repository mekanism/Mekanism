package mekanism.generators.common.tile.reactor;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.client.sound.ISoundSource;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IHasSound;
import mekanism.common.base.SoundWrapper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.FusionReactor;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityReactorController extends TileEntityReactorBlock implements IActiveState, IHasSound,
      ISoundSource {

    public static final int MAX_WATER = 100 * Fluid.BUCKET_VOLUME;
    public static final int MAX_STEAM = MAX_WATER * 100;
    public static final int MAX_FUEL = 1 * Fluid.BUCKET_VOLUME;

    public FluidTank waterTank = new FluidTank(MAX_WATER);
    public FluidTank steamTank = new FluidTank(MAX_STEAM);

    public GasTank deuteriumTank = new GasTank(MAX_FUEL);
    public GasTank tritiumTank = new GasTank(MAX_FUEL);

    public GasTank fuelTank = new GasTank(MAX_FUEL);

    public AxisAlignedBB box;

    public ResourceLocation soundURL = new ResourceLocation("mekanism", "tile.machine.fusionreactor");

    @SideOnly(Side.CLIENT)
    public SoundWrapper sound;

    public double clientTemp = 0;
    public boolean clientBurning = false;

    public TileEntityReactorController() {
        super("ReactorController", 1000000000);
        inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    }

    public boolean isFrame() {
        return false;
    }

    public void radiateNeutrons(int neutrons) {
    } //future impl

    public void formMultiblock(boolean keepBurning) {
        if (getReactor() == null) {
            setReactor(new FusionReactor(this));
        }

        getReactor().formMultiblock(keepBurning);
    }

    public double getPlasmaTemp() {
        if (getReactor() == null || !getReactor().isFormed()) {
            return 0;
        }

        return getReactor().getPlasmaTemp();
    }

    public double getCaseTemp() {
        if (getReactor() == null || !getReactor().isFormed()) {
            return 0;
        }

        return getReactor().getCaseTemp();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            updateSound();
        }

        if (isFormed()) {
            getReactor().simulate();

            if (!world.isRemote && (getReactor().isBurning() != clientBurning
                  || Math.abs(getReactor().getPlasmaTemp() - clientTemp) > 1000000)) {
                Mekanism.packetHandler.sendToAllAround(
                      new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                      Coord4D.get(this).getTargetPoint(50D));
                clientBurning = getReactor().isBurning();
                clientTemp = getReactor().getPlasmaTemp();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateSound() {
        if (shouldPlaySound() && getSound().canRestart() && MekanismConfig.current().client.enableMachineSounds.val()) {
            getSound().reset();
            getSound().play();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        formMultiblock(true);
    }

    @Override
    public void onAdded() {
        super.onAdded();

        formMultiblock(false);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setBoolean("formed", isFormed());

        if (isFormed()) {
            tag.setDouble("plasmaTemp", getReactor().getPlasmaTemp());
            tag.setDouble("caseTemp", getReactor().getCaseTemp());
            tag.setInteger("injectionRate", getReactor().getInjectionRate());
            tag.setBoolean("burning", getReactor().isBurning());
        } else {
            tag.setDouble("plasmaTemp", 0);
            tag.setDouble("caseTemp", 0);
            tag.setInteger("injectionRate", 0);
            tag.setBoolean("burning", false);
        }

        tag.setTag("fuelTank", fuelTank.write(new NBTTagCompound()));
        tag.setTag("deuteriumTank", deuteriumTank.write(new NBTTagCompound()));
        tag.setTag("tritiumTank", tritiumTank.write(new NBTTagCompound()));
        tag.setTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
        tag.setTag("steamTank", steamTank.writeToNBT(new NBTTagCompound()));

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        boolean formed = tag.getBoolean("formed");

        if (formed) {
            setReactor(new FusionReactor(this));
            getReactor().setPlasmaTemp(tag.getDouble("plasmaTemp"));
            getReactor().setCaseTemp(tag.getDouble("caseTemp"));
            getReactor().setInjectionRate(tag.getInteger("injectionRate"));
            getReactor().setBurning(tag.getBoolean("burning"));
            getReactor().updateTemperatures();
        }

        fuelTank.read(tag.getCompoundTag("fuelTank"));
        deuteriumTank.read(tag.getCompoundTag("deuteriumTank"));
        tritiumTank.read(tag.getCompoundTag("tritiumTank"));
        waterTank.readFromNBT(tag.getCompoundTag("waterTank"));
        steamTank.readFromNBT(tag.getCompoundTag("steamTank"));
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(getReactor() != null && getReactor().isFormed());

        if (getReactor() != null) {
            data.add(getReactor().getPlasmaTemp());
            data.add(getReactor().getCaseTemp());
            data.add(getReactor().getInjectionRate());
            data.add(getReactor().isBurning());
            data.add(fuelTank.getStored());
            data.add(deuteriumTank.getStored());
            data.add(tritiumTank.getStored());
            data.add(waterTank.getCapacity());
            data.add(waterTank.getFluidAmount());
            data.add(steamTank.getCapacity());
            data.add(steamTank.getFluidAmount());
        }

        return data;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int type = dataStream.readInt();

            switch (type) {
                case 0:
                    if (getReactor() != null) {
                        getReactor().setInjectionRate(dataStream.readInt());
                    }
                    break;
            }

            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            boolean formed = dataStream.readBoolean();

            if (formed) {
                if (getReactor() == null || !getReactor().formed) {
                    Mekanism.proxy.doGenericSparkle(this, tile -> tile instanceof TileEntityReactorBlock);
                }

                if (getReactor() == null) {
                    setReactor(new FusionReactor(this));
                    MekanismUtils.updateBlock(world, getPos());
                }

                getReactor().formed = true;
                getReactor().setPlasmaTemp(dataStream.readDouble());
                getReactor().setCaseTemp(dataStream.readDouble());
                getReactor().setInjectionRate(dataStream.readInt());
                getReactor().setBurning(dataStream.readBoolean());
                fuelTank.setGas(new GasStack(MekanismFluids.FusionFuel, dataStream.readInt()));
                deuteriumTank.setGas(new GasStack(MekanismFluids.Deuterium, dataStream.readInt()));
                tritiumTank.setGas(new GasStack(MekanismFluids.Tritium, dataStream.readInt()));
                waterTank.setCapacity(dataStream.readInt());
                waterTank.setFluid(new FluidStack(FluidRegistry.getFluid("water"), dataStream.readInt()));
                steamTank.setCapacity(dataStream.readInt());
                steamTank.setFluid(new FluidStack(FluidRegistry.getFluid("steam"), dataStream.readInt()));
            } else if (getReactor() != null) {
                setReactor(null);
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    public boolean isFormed() {
        return getReactor() != null && getReactor().isFormed();
    }

    public boolean isBurning() {
        return getActive() && getReactor().isBurning();
    }

    @Override
    public boolean getActive() {
        return isFormed();
    }

    @Override
    public void setActive(boolean active) {
        if (active == (getReactor() == null)) {
            setReactor(active ? new FusionReactor(this) : null);
        }
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (box == null) {
            box = new AxisAlignedBB(getPos().getX() - 1, getPos().getY() - 3, getPos().getZ() - 1, getPos().getX() + 2,
                  getPos().getY(), getPos().getZ() + 2);
        }

        return box;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SoundWrapper getSound() {
        return sound;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldPlaySound() {
        return isBurning() && !isInvalid();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getSoundLocation() {
        return soundURL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getVolume() {
        return 2F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getFrequency() {
        return 1F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSoundPosition() {
        return new Vec3d(getPos()).addVector(0.5, 0.5, 0.5);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRepeat() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AttenuationType getAttenuation() {
        return AttenuationType.LINEAR;
    }

    @Override
    public void validate() {
        super.validate();

        if (world.isRemote) {
            initSounds();
        }
    }

    @SideOnly(Side.CLIENT)
    public void initSounds() {
        sound = new SoundWrapper(this, this);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return InventoryUtils.EMPTY;
    }
}
