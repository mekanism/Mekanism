package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.matrix.MatrixCache;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.multiblock.MultiblockManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityInductionCasing extends TileEntityMultiblock<SynchronizedMatrixData> implements IStrictEnergyStorage, IComputerIntegration {

    public static final String[] methods = new String[]{"getEnergy", "getMaxEnergy", "getInput", "getOutput", "getTransferCap"};

    public TileEntityInductionCasing() {
        this(MekanismBlock.INDUCTION_CASING);
    }

    public TileEntityInductionCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            if (structure != null && isRendering) {
                structure.tick(getWorld());
                //TODO: FIXME??
                List<IInventorySlot> inventorySlots = getInventorySlots(null);
                ((EnergyInventorySlot) inventorySlots.get(0)).charge(this);
                ((EnergyInventorySlot) inventorySlots.get(1)).discharge(this);
            }
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (structure == null) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        if (structure != null) {
            structure.addStructureData(data);
        }
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            if (clientHasStructure) {
                structure.readStructureData(dataStream);
            }
        }
    }

    @Override
    protected SynchronizedMatrixData getNewStructure() {
        return new SynchronizedMatrixData();
    }

    @Override
    public MatrixCache getNewCache() {
        return new MatrixCache();
    }

    @Override
    protected MatrixUpdateProtocol getProtocol() {
        return new MatrixUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedMatrixData> getManager() {
        return Mekanism.matrixManager;
    }

    @Override
    public double getEnergy() {
        //Uses post queue as that is the actual total we just haven't saved it yet
        return structure != null ? structure.getEnergyPostQueue() : 0;
    }

    @Override
    public void setEnergy(double energy) {
        if (structure != null) {
            structure.queueSetEnergy(Math.max(Math.min(energy, getMaxEnergy()), 0));
        }
    }

    public double addEnergy(double energy, boolean simulate) {
        return structure != null ? structure.queueEnergyAddition(energy, simulate) : 0;
    }

    public double removeEnergy(double energy, boolean simulate) {
        return structure != null ? structure.queueEnergyRemoval(energy, simulate) : 0;
    }

    @Override
    public double getMaxEnergy() {
        return structure != null ? structure.getStorageCap() : 0;
    }

    public double getLastInput() {
        return structure != null ? structure.getLastInput() : 0;
    }

    public double getLastOutput() {
        return structure != null ? structure.getLastOutput() : 0;
    }

    public double getTransferCap() {
        return structure != null ? structure.getTransferCap() : 0;
    }

    public int getCellCount() {
        return structure != null ? structure.getCellCount() : 0;
    }

    public int getProviderCount() {
        return structure != null ? structure.getProviderCount() : 0;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (structure == null) {
            return new Object[]{"Unformed."};
        }
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{getMaxEnergy()};
            case 2:
                return new Object[]{structure.getLastInput()};
            case 3:
                return new Object[]{structure.getLastOutput()};
            case 4:
                return new Object[]{structure.getTransferCap()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}