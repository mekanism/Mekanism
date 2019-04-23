package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.matrix.MatrixCache;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityInductionCasing extends TileEntityMultiblock<SynchronizedMatrixData> implements
      IStrictEnergyStorage, IComputerIntegration {

    protected static final int[] CHARGE_SLOT = {0};
    protected static final int[] DISCHARGE_SLOT = {1};

    public static final String[] methods = new String[]{"getEnergy", "getMaxEnergy", "getInput", "getOutput",
          "getTransferCap"};
    public int clientCells;
    public int clientProviders;

    public TileEntityInductionCasing() {
        this("InductionCasing");
    }

    public TileEntityInductionCasing(String name) {
        super(name);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            if (structure != null && isRendering) {
                structure.lastInput = structure.transferCap - structure.remainingInput;
                structure.remainingInput = structure.transferCap;

                structure.lastOutput = structure.transferCap - structure.remainingOutput;
                structure.remainingOutput = structure.transferCap;

                ChargeUtils.charge(0, this);
                ChargeUtils.discharge(1, this);
            }
        }
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (!player.isSneaking() && structure != null) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
            player.openGui(Mekanism.instance, 49, world, getPos().getX(), getPos().getY(), getPos().getZ());

            return true;
        }

        return false;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        if (structure != null) {
            data.add(structure.getEnergy(world));
            data.add(structure.storageCap);
            data.add(structure.transferCap);
            data.add(structure.lastInput);
            data.add(structure.lastOutput);

            data.add(structure.volWidth);
            data.add(structure.volHeight);
            data.add(structure.volLength);

            data.add(structure.cells.size());
            data.add(structure.providers.size());
        }

        return data;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            if (clientHasStructure) {
                structure.clientEnergy = dataStream.readDouble();
                structure.storageCap = dataStream.readDouble();
                structure.transferCap = dataStream.readDouble();
                structure.lastInput = dataStream.readDouble();
                structure.lastOutput = dataStream.readDouble();

                structure.volWidth = dataStream.readInt();
                structure.volHeight = dataStream.readInt();
                structure.volLength = dataStream.readInt();

                clientCells = dataStream.readInt();
                clientProviders = dataStream.readInt();
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

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize("gui.inductionMatrix");
    }

    public int getScaledEnergyLevel(int i) {
        return (int) (getEnergy() * i / getMaxEnergy());
    }

    @Override
    public double getEnergy() {
        if (!world.isRemote) {
            return structure != null ? structure.getEnergy(world) : 0;
        } else {
            return structure != null ? structure.clientEnergy : 0;
        }
    }

    @Override
    public void setEnergy(double energy) {
        if (structure != null) {
            structure.setEnergy(world, Math.max(Math.min(energy, getMaxEnergy()), 0));
            MekanismUtils.saveChunk(this);
        }
    }

    @Override
    public double getMaxEnergy() {
        return structure != null ? structure.storageCap : 0;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        if (structure == null) {
            return new Object[]{"Unformed."};
        }

        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{getMaxEnergy()};
            case 2:
                return new Object[]{structure.lastInput};
            case 3:
                return new Object[]{structure.lastOutput};
            case 4:
                return new Object[]{structure.transferCap};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, net.minecraft.util.EnumFacing facing) {
        return capability == Capabilities.ENERGY_STORAGE_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return InventoryUtils.EMPTY;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}
