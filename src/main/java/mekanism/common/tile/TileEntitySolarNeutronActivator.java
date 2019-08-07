package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntitySolarNeutronActivator extends TileEntityMekanism implements IBoundingBlock, IGasHandler, IActiveState, ISustainedData, ITankManager, IUpgradeTile,
      IUpgradeInfoHandler, IComparatorSupport {

    public static final int MAX_GAS = 10000;
    private static final int[] INPUT_SLOT = {0};
    private static final int[] OUTPUT_SLOT = {1};

    public GasTank inputTank = new GasTank(MAX_GAS);
    public GasTank outputTank = new GasTank(MAX_GAS);

    public int gasOutput = 256;

    private SolarNeutronRecipe cachedRecipe;

    private int currentRedstoneLevel;
    private boolean needsRainCheck;

    public TileComponentUpgrade<TileEntitySolarNeutronActivator> upgradeComponent = new TileComponentUpgrade<>(this, 3);

    public TileEntitySolarNeutronActivator() {
        super(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR);
        upgradeComponent.setSupported(Upgrade.ENERGY, false);
    }

    @Override
    public void validate() {
        super.validate();
        // Cache the flag to know if rain matters where this block is placed
        needsRainCheck = world.provider.getBiomeForCoords(getPos()).canRain();
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            TileUtils.receiveGas(getInventory().get(0), inputTank);
            TileUtils.drawGas(getInventory().get(1), outputTank);
            SolarNeutronRecipe recipe = getRecipe();

            // TODO: Ideally the neutron activator should use the sky brightness to determine throughput; but
            // changing this would dramatically affect a lot of setups with Fusion reactors which can take
            // a long time to relight. I don't want to be chased by a mob right now, so just doing basic
            // rain checks.
            boolean seesSun = world.isDaytime() && world.canSeeSky(getPos().up()) && !world.provider.isNether();
            if (needsRainCheck) {
                seesSun &= !(world.isRaining() || world.isThundering());
            }

            if (seesSun && canOperate(recipe) && MekanismUtils.canFunction(this)) {
                setActive(true);
                operate(recipe);
            } else {
                setActive(false);
            }

            TileUtils.emitGas(this, outputTank, gasOutput, getDirection());
            // Every 20 ticks (once a second), send update to client. Note that this is a 50% reduction in network
            // traffic from previous implementation that send the update every 10 ticks.
            if (world.getTotalWorldTime() % 20 == 0) {
                Mekanism.packetHandler.sendUpdatePacket(this);
            }

            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    public int getUpgradedUsage() {
        int possibleProcess = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        possibleProcess = Math.min(Math.min(inputTank.getStored(), outputTank.getNeeded()), possibleProcess);
        return possibleProcess;
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

    public void operate(SolarNeutronRecipe recipe) {
        recipe.operate(inputTank, outputTank, getUpgradedUsage());
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(dataStream, inputTank);
            TileUtils.readTankData(dataStream, outputTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, inputTank);
        TileUtils.addTankData(data, outputTank);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        inputTank.read(nbtTags.getCompound("inputTank"));
        outputTank.read(nbtTags.getCompound("outputTank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("inputTank", inputTank.write(new CompoundNBT()));
        nbtTags.put("outputTank", outputTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Override
    public boolean canSetFacing(@Nonnull Direction facing) {
        return facing != Direction.DOWN && facing != Direction.UP;
    }

    @Override
    public void onPlace() {
        MekanismUtils.makeBoundingBlock(world, Coord4D.get(this).offset(Direction.UP).getPos(), Coord4D.get(this));
    }

    @Override
    public void onBreak() {
        world.removeBlock(getPos().up(), false);
        world.removeBlock(getPos(), false);
    }

    @Override
    public int receiveGas(Direction side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack != null ? stack.getGas() : null)) {
            return inputTank.receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
        if (canDrawGas(side, null)) {
            return outputTank.draw(amount, doTransfer);
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(Direction side, Gas type) {
        return side == Direction.DOWN && inputTank.canReceive(type);
    }

    @Override
    public boolean canDrawGas(Direction side, Gas type) {
        return side == getDirection() && outputTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{inputTank, outputTank};
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side != null && side != getDirection() && side != Direction.DOWN;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (inputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "inputTank", inputTank.getGas().write(new CompoundNBT()));
        }
        if (outputTank.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "outputTank", outputTank.getGas().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        inputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "inputTank")));
        outputTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "outputTank")));
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{inputTank, outputTank};
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgradeType) {

    }

    @Override
    public List<String> getInfo(Upgrade upgrade) {
        return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    public double getProgress() {
        if (getActive()) {
            return .16 * (1 + (world.getTotalWorldTime() % 6));
        }
        return 0;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return side == getDirection() ? OUTPUT_SLOT : INPUT_SLOT;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof IGasItem;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getMaxGas());
    }
}