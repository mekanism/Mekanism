package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.OutputInput;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileEntityInductionPort extends TileEntityInductionCasing implements IEnergyWrapper, IConfigurable, IActiveState, IComparatorSupport {

    //TODO: IC2
    //private boolean ic2Registered = false;
    private int currentRedstoneLevel;

    /**
     * false = input, true = output
     */
    public boolean mode;
    private CapabilityWrapperManager<IEnergyWrapper, ForgeEnergyIntegration> forgeEnergyManager = new CapabilityWrapperManager<>(IEnergyWrapper.class, ForgeEnergyIntegration.class);

    public TileEntityInductionPort() {
        super(MekanismBlock.INDUCTION_PORT);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        //TODO: IC2
        /*if (!ic2Registered && MekanismUtils.useIC2()) {
            register();
        }*/
        if (!isRemote()) {
            if (structure != null && mode) {
                CableUtils.emit(this);
            }
            World world = getWorld();
            if (world != null) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        if (structure != null && mode) {
            return !structure.locations.contains(Coord4D.get(this).offset(side));
        }
        return false;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return (structure != null && !mode);
    }

    @Override
    public double getMaxOutput() {
        return structure != null ? structure.getRemainingOutput() : 0;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            boolean prevMode = mode;
            mode = dataStream.readBoolean();
            if (prevMode != mode) {
                MekanismUtils.updateBlock(getWorld(), getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(mode);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        mode = nbtTags.getBoolean("mode");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean("mode", mode);
        return nbtTags;
    }

    //TODO: IC2
    /*@Override
    public void onAdded() {
        super.onAdded();
        if (MekanismUtils.useIC2()) {
            register();
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (MekanismUtils.useIC2()) {
            deregister();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void remove() {
        super.remove();
        if (MekanismUtils.useIC2()) {
            deregister();
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void register() {
        if (!isRemote()) {
            IEnergyTile registered = EnergyNet.instance.getTile(world, getPos());
            if (registered != this) {
                if (registered != null && ic2Registered) {
                    MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(registered));
                    ic2Registered = false;
                } else {
                    MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
                    ic2Registered = true;
                }
            }
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void deregister() {
        if (!isRemote()) {
            IEnergyTile registered = EnergyNet.instance.getTile(world, getPos());
            if (registered != null && ic2Registered) {
                MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(registered));
                ic2Registered = false;
            }
        }
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int addEnergy(int amount) {
        addEnergy(IC2Integration.fromEU(amount), false);
        //IC2 returns the amount of energy inside after the value, instead of amount actually added/removed
        return IC2Integration.toEUAsInt(getEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getDemandedEnergy() {
        return IC2Integration.toEU(getMaxEnergy() - getEnergy());
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double injectEnergy(Direction direction, double amount, double voltage) {
        TileEntity tile = MekanismUtils.getTileEntity(world, getPos().offset(direction));
        if (tile == null || CapabilityUtils.getCapabilityHelper(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, direction.getOpposite()).isPresent()) {
            return amount;
        }
        return amount - IC2Integration.toEU(acceptEnergy(direction, IC2Integration.fromEU(amount), false));
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void drawEnergy(double amount) {
        removeEnergy(IC2Integration.fromEU(amount), false);
    }*/

    @Override
    public double acceptEnergy(Direction side, double amount, boolean simulate) {
        return side == null || canReceiveEnergy(side) ? addEnergy(amount, simulate) : 0;
    }

    @Override
    public double pullEnergy(Direction side, double amount, boolean simulate) {
        return side == null || canOutputEnergy(side) ? removeEnergy(amount, simulate) : 0;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            mode = !mode;
            player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                  Translation.of("tooltip.mekanism.configurator.inductionPortMode"), " ", (mode ? EnumColor.DARK_RED : EnumColor.DARK_GREEN), OutputInput.of(mode), "."));
            Mekanism.packetHandler.sendUpdatePacket(this);
            markDirty();
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public boolean getActive() {
        return mode;
    }

    @Override
    public void setActive(boolean active) {
        mode = active;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY) {
            return Capabilities.ENERGY_ACCEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
            return Capabilities.ENERGY_OUTPUTTER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> forgeEnergyManager.getWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        if (!hasInventory() || structure == null) {
            //TODO: Previously we had a check like !isRemote() ? structure == null : !clientHasStructure
            // Do we still need this if we ever actually needed it?
            //If we don't have a structure then return that we have no slots accessible
            return Collections.emptyList();
        }
        //TODO: Cache this??
        return Collections.singletonList(structure.getInventorySlots().get(mode ? 0 : 1));
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getEnergy(), getMaxEnergy());
    }
}