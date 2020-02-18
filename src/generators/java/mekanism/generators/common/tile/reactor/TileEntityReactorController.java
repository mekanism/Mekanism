package mekanism.generators.common.tile.reactor;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasTank;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.FusionReactor;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityReactorController extends TileEntityReactorBlock implements IActiveState {

    public static final int MAX_WATER = 100 * FluidAttributes.BUCKET_VOLUME;
    public static final int MAX_STEAM = MAX_WATER * 100;
    public static final int MAX_FUEL = FluidAttributes.BUCKET_VOLUME;

    public FluidTank waterTank = new FluidTank(MAX_WATER);
    public FluidTank steamTank = new FluidTank(MAX_STEAM);

    public GasTank deuteriumTank = new GasTank(MAX_FUEL);
    public GasTank tritiumTank = new GasTank(MAX_FUEL);

    public GasTank fuelTank = new GasTank(MAX_FUEL);

    public AxisAlignedBB box;
    public double clientTemp = 0;
    public boolean clientBurning = false;
    /**
     * Only used by the client
     */
    private ISound activeSound;
    private int playSoundCooldown = 0;

    private IInventorySlot reactorSlot;

    public TileEntityReactorController() {
        super(GeneratorsBlocks.REACTOR_CONTROLLER);
        doAutoSync = true;
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO: FIXME, make the slot only "exist" or at least be accessible when the reactor is formed
        builder.addSlot(reactorSlot = BasicInventorySlot.at(stack -> stack.getItem() instanceof ItemHohlraum, this, 80, 39));
        return builder.build();
    }

    public IInventorySlot getReactorSlot() {
        return reactorSlot;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    public void radiateNeutrons(int neutrons) {
        //future impl
    }

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
        if (isRemote()) {
            updateSound();
        }
        if (isFormed()) {
            getReactor().simulate();
            if (!isRemote() && (getReactor().isBurning() != clientBurning || Math.abs(getReactor().getPlasmaTemp() - clientTemp) > 1_000_000)) {
                Mekanism.packetHandler.sendUpdatePacket(this);
                clientBurning = getReactor().isBurning();
                clientTemp = getReactor().getPlasmaTemp();
            }
        }
    }

    /**
     * Only used by the client
     */
    private void updateSound() {
        // If machine sounds are disabled, noop
        if (!MekanismConfig.client.enableMachineSounds.get()) {
            return;
        }
        if (isBurning() && !isRemoved()) {
            // If sounds are being muted, we can attempt to start them on every tick, only to have them
            // denied by the event bus, so use a cooldown period that ensures we're only trying once every
            // second or so to start a sound.
            if (--playSoundCooldown > 0) {
                return;
            }
            if (activeSound == null || !Minecraft.getInstance().getSoundHandler().isPlaying(activeSound)) {
                activeSound = SoundHandler.startTileSound(GeneratorsSounds.FUSION_REACTOR.getSoundEvent(), getSoundCategory(), 1.0F, getPos());
                playSoundCooldown = 20;
            }
        } else if (activeSound != null) {
            SoundHandler.stopTileSound(getPos());
            activeSound = null;
            playSoundCooldown = 0;
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (isRemote()) {
            updateSound();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        formMultiblock(true);
    }

    @Override
    public void onAdded() {
        super.onAdded();
        formMultiblock(false);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putBoolean("formed", isFormed());
        if (isFormed()) {
            tag.putDouble("plasmaTemp", getReactor().getPlasmaTemp());
            tag.putDouble("caseTemp", getReactor().getCaseTemp());
            tag.putInt("injectionRate", getReactor().getInjectionRate());
            tag.putBoolean("burning", getReactor().isBurning());
        } else {
            tag.putDouble("plasmaTemp", 0);
            tag.putDouble("caseTemp", 0);
            tag.putInt("injectionRate", 0);
            tag.putBoolean("burning", false);
        }
        tag.put("fuelTank", fuelTank.write(new CompoundNBT()));
        tag.put("deuteriumTank", deuteriumTank.write(new CompoundNBT()));
        tag.put("tritiumTank", tritiumTank.write(new CompoundNBT()));
        tag.put("waterTank", waterTank.writeToNBT(new CompoundNBT()));
        tag.put("steamTank", steamTank.writeToNBT(new CompoundNBT()));
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        boolean formed = tag.getBoolean("formed");
        if (formed) {
            setReactor(new FusionReactor(this));
            getReactor().setPlasmaTemp(tag.getDouble("plasmaTemp"));
            getReactor().setCaseTemp(tag.getDouble("caseTemp"));
            getReactor().setInjectionRate(tag.getInt("injectionRate"));
            getReactor().setBurning(tag.getBoolean("burning"));
            getReactor().updateTemperatures();
        }
        fuelTank.read(tag.getCompound("fuelTank"));
        deuteriumTank.read(tag.getCompound("deuteriumTank"));
        tritiumTank.read(tag.getCompound("tritiumTank"));
        waterTank.readFromNBT(tag.getCompound("waterTank"));
        steamTank.readFromNBT(tag.getCompound("steamTank"));
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
            data.add(waterTank.getFluid());
            data.add(steamTank.getFluid());
        }
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                if (getReactor() != null) {
                    getReactor().setInjectionRate(dataStream.readInt());
                }
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            boolean formed = dataStream.readBoolean();
            World world = getWorld();
            if (formed) {
                if (getReactor() == null || !getReactor().formed) {
                    BlockPos corner = getPos().subtract(new Vec3i(2, 4, 2));
                    Mekanism.proxy.doMultiblockSparkle(this, corner, 5, 5, 6, tile -> tile instanceof TileEntityReactorBlock);
                }
                if (getReactor() == null && world != null) {
                    setReactor(new FusionReactor(this));
                    MekanismUtils.updateBlock(world, getPos());
                }

                getReactor().formed = true;
                getReactor().setPlasmaTemp(dataStream.readDouble());
                getReactor().setCaseTemp(dataStream.readDouble());
                getReactor().setInjectionRate(dataStream.readInt());
                getReactor().setBurning(dataStream.readBoolean());
                fuelTank.setStack(MekanismGases.FUSION_FUEL.getGasStack(dataStream.readInt()));
                deuteriumTank.setStack(MekanismGases.DEUTERIUM.getGasStack(dataStream.readInt()));
                tritiumTank.setStack(MekanismGases.TRITIUM.getGasStack(dataStream.readInt()));
                waterTank.setFluid(dataStream.readFluidStack());
                steamTank.setFluid(dataStream.readFluidStack());
            } else if (getReactor() != null && world != null) {
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
        //TODO: Improve how we are handling the "active" state for this
        // We currently call super just so that it updates the block state properly
        super.setActive(active);
        if (active == (getReactor() == null)) {
            setReactor(active ? new FusionReactor(this) : null);
        }
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (box == null) {
            box = new AxisAlignedBB(getPos().getX() - 1, getPos().getY() - 3, getPos().getZ() - 1, getPos().getX() + 2, getPos().getY(), getPos().getZ() + 2);
        }
        return box;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return !isFormed();
        }
        return super.isCapabilityDisabled(capability, side);
    }
}