package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySolarNeutronActivator extends TileEntityContainerBlock implements IRedstoneControl,
      IBoundingBlock, IGasHandler, ITubeConnection, IActiveState, ISustainedData, ITankManager, ISecurityTile,
      IUpgradeTile, IUpgradeInfoHandler {

    public static final int MAX_GAS = 10000;
    private static final int[] SLOTS = {0, 1, 2};
    public GasTank inputTank = new GasTank(MAX_GAS);
    public GasTank outputTank = new GasTank(MAX_GAS);
    public int updateDelay;

    public boolean isActive;

    public boolean clientActive;

    public int gasOutput = 256;

    public SolarNeutronRecipe cachedRecipe;

    /**
     * This machine's current RedstoneControl type.
     */
    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, SLOTS.length);
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    public TileEntitySolarNeutronActivator() {
        super("SolarNeutronActivator");
        upgradeComponent.setSupported(Upgrade.ENERGY, false);
        inventory = NonNullList.withSize(SLOTS.length + 1, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        if (world.isRemote && updateDelay > 0) {
            updateDelay--;

            if (updateDelay == 0 && clientActive != isActive) {
                isActive = clientActive;
                MekanismUtils.updateBlock(world, getPos());
            }
        }

        if (!world.isRemote) {
            if (updateDelay > 0) {
                updateDelay--;

                if (updateDelay == 0 && clientActive != isActive) {
                    Mekanism.packetHandler.sendToReceivers(
                          new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                          new Range4D(Coord4D.get(this)));
                }
            }

            if (!inventory.get(0).isEmpty() && (inputTank.getGas() == null || inputTank.getStored() < inputTank
                  .getMaxGas())) {
                inputTank.receive(GasUtils.removeGas(inventory.get(0), inputTank.getGasType(), inputTank.getNeeded()),
                      true);
            }

            if (!inventory.get(1).isEmpty() && outputTank.getGas() != null) {
                outputTank.draw(GasUtils.addGas(inventory.get(1), outputTank.getGas()), true);
            }

            SolarNeutronRecipe recipe = getRecipe();

            boolean sky =
                  ((!world.isRaining() && !world.isThundering()) || isDesert()) && !world.provider.isNether() && world
                        .canSeeSky(getPos().up()); // TODO Check isNether call, maybe it should be hasSkyLight

            if (world.isDaytime() && sky && canOperate(recipe) && MekanismUtils.canFunction(this)) {
                setActive(true);

                int operations = operate(recipe);
            } else {
                setActive(false);
            }

            if (outputTank.getGas() != null) {
                GasStack toSend = new GasStack(outputTank.getGas().getGas(),
                      Math.min(outputTank.getStored(), gasOutput));
                outputTank.draw(GasUtils.emit(toSend, this, ListUtils.asList(facing)), true);
            }
        }
    }

    public int getUpgradedUsage() {
        int possibleProcess = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        possibleProcess = Math.min(Math.min(inputTank.getStored(), outputTank.getNeeded()), possibleProcess);

        return possibleProcess;
    }

    public boolean isDesert() {
        return world.provider.getBiomeForCoords(getPos()) instanceof BiomeDesert;
    }

    public SolarNeutronRecipe getRecipe() {
        GasInput input = getInput();

        if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
            cachedRecipe = RecipeHandler.getSolarNeutronRecipe(getInput());
        }

        return cachedRecipe;
    }

    public GasInput getInput() {
        return new GasInput(inputTank.getGas());
    }

    public boolean canOperate(SolarNeutronRecipe recipe) {
        return recipe != null && recipe.canOperate(inputTank, outputTank);
    }

    public int operate(SolarNeutronRecipe recipe) {
        int operations = getUpgradedUsage();

        recipe.operate(inputTank, outputTank, operations);

        return operations;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            isActive = dataStream.readBoolean();
            controlType = RedstoneControl.values()[dataStream.readInt()];

            if (dataStream.readBoolean()) {
                inputTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
            } else {
                inputTank.setGas(null);
            }

            if (dataStream.readBoolean()) {
                outputTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
            } else {
                outputTank.setGas(null);
            }

            if (updateDelay == 0 && clientActive != isActive) {
                updateDelay = MekanismConfig.current().general.UPDATE_DELAY.val();
                isActive = clientActive;
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(isActive);
        data.add(controlType.ordinal());

        if (inputTank.getGas() != null) {
            data.add(true);
            data.add(inputTank.getGas().getGas().getID());
            data.add(inputTank.getStored());
        } else {
            data.add(false);
        }

        if (outputTank.getGas() != null) {
            data.add(true);
            data.add(outputTank.getGas().getGas().getID());
            data.add(outputTank.getStored());
        } else {
            data.add(false);
        }

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        isActive = nbtTags.getBoolean("isActive");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

        inputTank.read(nbtTags.getCompoundTag("inputTank"));
        outputTank.read(nbtTags.getCompoundTag("outputTank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());

        nbtTags.setTag("inputTank", inputTank.write(new NBTTagCompound()));
        nbtTags.setTag("outputTank", outputTank.write(new NBTTagCompound()));

        return nbtTags;
    }

    @Override
    public boolean canSetFacing(int i) {
        return i != 0 && i != 1;
    }

    @Override
    public void onPlace() {
        MekanismUtils.makeBoundingBlock(world, Coord4D.get(this).offset(EnumFacing.UP).getPos(), Coord4D.get(this));
    }

    @Override
    public void onBreak() {
        world.setBlockToAir(getPos().up());
        world.setBlockToAir(getPos());
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack != null ? stack.getGas() : null)) {
            return inputTank.receive(stack, doTransfer);
        }

        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if (canDrawGas(side, null)) {
            return outputTank.draw(amount, doTransfer);
        }

        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return side == EnumFacing.DOWN && inputTank.canReceive(type);
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return side == facing && outputTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputTank, outputTank};
    }

    @Override
    public boolean canTubeConnect(EnumFacing side) {
        return side == facing || side == EnumFacing.DOWN;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY
              || capability == Capabilities.TUBE_CONNECTION_CAPABILITY
              || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY
              || capability == Capabilities.TUBE_CONNECTION_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (inputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "inputTank", inputTank.getGas().write(new NBTTagCompound()));
        }

        if (outputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getGas().write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        inputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputTank")));
        outputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type) {
        controlType = type;
        MekanismUtils.saveChunk(this);
    }

    @Override
    public boolean canPulse() {
        return false;
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;

        if (clientActive != active && updateDelay == 0) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));

            updateDelay = 10;
            clientActive = active;
        }
    }

    @Override
    public boolean renderUpdate() {
        return false;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputTank, outputTank};
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public List<String> getInfo(Upgrade upgrade) {
        return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return canTubeConnect(side) ? InventoryUtils.EMPTY : SLOTS;
    }
}
