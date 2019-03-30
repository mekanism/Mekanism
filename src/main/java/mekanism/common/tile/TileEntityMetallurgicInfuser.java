package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.TileNetworkList;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.InfuseStorage;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityMetallurgicInfuser extends TileEntityOperationalMachine implements IComputerIntegration,
      ISideConfiguration, IConfigCardAccess, ITierUpgradeable, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "facing", "canOperate",
          "getMaxEnergy", "getEnergyNeeded", "getInfuse", "getInfuseNeeded"};
    /**
     * The maxiumum amount of infuse this machine can store.
     */
    public int MAX_INFUSE = 1000;
    /**
     * The amount of infuse this machine has stored.
     */
    public InfuseStorage infuseStored = new InfuseStorage();
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    public TileEntityMetallurgicInfuser() {
        super("machine.metalinfuser", "MetallurgicInfuser",
              BlockStateMachine.MachineType.METALLURGIC_INFUSER.baseEnergy, usage.metallurgicInfuserUsage, 0, 200);

        configComponent = new TileComponentConfig(this, TransmissionType.ITEM);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{2}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{3}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{4}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Infuse", EnumColor.PURPLE, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{4, 0, 0, 3, 1, 2});

        inventory = NonNullList.withSize(5, ItemStack.EMPTY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));

        securityComponent = new TileComponentSecurity(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.discharge(4, this);

            if (!inventory.get(1).isEmpty()) {
                if (InfuseRegistry.getObject(inventory.get(1)) != null) {
                    InfuseObject infuse = InfuseRegistry.getObject(inventory.get(1));

                    if (infuseStored.type == null || infuseStored.type == infuse.type) {
                        if (infuseStored.amount + infuse.stored <= MAX_INFUSE) {
                            infuseStored.amount += infuse.stored;
                            infuseStored.type = infuse.type;
                            inventory.get(1).shrink(1);
                        }
                    }
                }
            }

            MetallurgicInfuserRecipe recipe = RecipeHandler.getMetallurgicInfuserRecipe(getInput());

            if (canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= energyPerTick) {
                setActive(true);
                setEnergy(getEnergy() - energyPerTick);

                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    operate(recipe);
                    operatingTicks = 0;
                }
            } else {
                if (prevEnergy >= getEnergy()) {
                    setActive(false);
                }
            }

            if (!canOperate(recipe)) {
                operatingTicks = 0;
            }

            if (infuseStored.amount <= 0) {
                infuseStored.amount = 0;
                infuseStored.type = null;
            }

            prevEnergy = getEnergy();
        }
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier != BaseTier.BASIC) {
            return false;
        }

        world.setBlockToAir(getPos());
        world.setBlockState(getPos(), MekanismBlocks.MachineBlock.getStateFromMeta(5), 3);

        TileEntityFactory factory = (TileEntityFactory) world.getTileEntity(getPos());
        RecipeType type = RecipeType.INFUSING;

        //Basic
        factory.facing = facing;
        factory.clientFacing = clientFacing;
        factory.ticker = ticker;
        factory.redstone = redstone;
        factory.redstoneLastTick = redstoneLastTick;
        factory.doAutoSync = doAutoSync;

        //Electric
        factory.electricityStored = electricityStored;

        //Machine
        factory.progress[0] = operatingTicks;
        factory.setActive(isActive);
        factory.controlType = controlType;
        factory.prevEnergy = prevEnergy;
        factory.upgradeComponent.readFrom(upgradeComponent);
        factory.upgradeComponent.setUpgradeSlot(0);
        factory.ejectorComponent.readFrom(ejectorComponent);
        factory.ejectorComponent
              .setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
        factory.setRecipeType(type);
        factory.upgradeComponent.setSupported(Upgrade.GAS, type.fuelEnergyUpgrades());
        factory.securityComponent.readFrom(securityComponent);

        for (TransmissionType transmission : configComponent.transmissions) {
            factory.configComponent.setConfig(transmission, configComponent.getConfig(transmission).asByteArray());
            factory.configComponent.setEjecting(transmission, configComponent.isEjecting(transmission));
        }

        //Infuser
        factory.infuseStored = infuseStored;

        factory.inventory.set(5, inventory.get(2));
        factory.inventory.set(1, inventory.get(4));
        factory.inventory.set(5 + 3, inventory.get(3));
        factory.inventory.set(0, inventory.get(0));
        factory.inventory.set(4, inventory.get(1));

        for (Upgrade upgrade : factory.upgradeComponent.getSupportedTypes()) {
            factory.recalculateUpgradables(upgrade);
        }

        factory.upgraded = true;
        factory.markDirty();

        return true;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 4) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        } else {
            return slotID == 3;
        }

    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 3) {
            return false;
        } else if (slotID == 1) {
            return InfuseRegistry.getObject(itemstack) != null && (infuseStored.type == null
                  || infuseStored.type == InfuseRegistry.getObject(itemstack).type);
        } else if (slotID == 0) {
            return itemstack.getItem() == MekanismItems.SpeedUpgrade
                  || itemstack.getItem() == MekanismItems.EnergyUpgrade;
        } else if (slotID == 2) {
            if (infuseStored.type != null) {
                return RecipeHandler.getMetallurgicInfuserRecipe(new InfusionInput(infuseStored, itemstack)) != null;
            } else {
                return Recipe.METALLURGIC_INFUSER.get().keySet().stream()
                      .anyMatch(input -> input.inputStack.isItemEqual(itemstack));
            }
        } else if (slotID == 4) {
            return ChargeUtils.canBeDischarged(itemstack);
        }

        return false;
    }

    public InfusionInput getInput() {
        return new InfusionInput(infuseStored, inventory.get(2));
    }

    public void operate(MetallurgicInfuserRecipe recipe) {
        recipe.output(inventory, 2, 3, infuseStored);

        markDirty();
        ejectorComponent.outputItems();
    }

    public boolean canOperate(MetallurgicInfuserRecipe recipe) {
        return recipe != null && recipe.canOperate(inventory, 2, 3, infuseStored);
    }

    public int getScaledInfuseLevel(int i) {
        return infuseStored.amount * i / MAX_INFUSE;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        infuseStored.amount = nbtTags.getInteger("infuseStored");
        infuseStored.type = InfuseRegistry.get(nbtTags.getString("type"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("infuseStored", infuseStored.amount);

        if (infuseStored.type != null) {
            nbtTags.setString("type", infuseStored.type.name);
        } else {
            nbtTags.setString("type", "null");
        }

        nbtTags.setBoolean("sideDataStored", true);

        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            infuseStored.amount = dataStream.readInt();
            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            infuseStored.amount = dataStream.readInt();
            if (infuseStored.amount > 0) {
                infuseStored.type = InfuseRegistry.get(PacketHandler.readString(dataStream));
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(infuseStored.amount);
        if (infuseStored.amount > 0) {
            data.add(infuseStored.type.name);
        }

        return data;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{operatingTicks};
            case 2:
                return new Object[]{facing};
            case 3:
                return new Object[]{canOperate(RecipeHandler.getMetallurgicInfuserRecipe(getInput()))};
            case 4:
                return new Object[]{getMaxEnergy()};
            case 5:
                return new Object[]{getMaxEnergy() - getEnergy()};
            case 6:
                return new Object[]{infuseStored};
            case 7:
                return new Object[]{MAX_INFUSE - infuseStored.amount};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, facing).availableSlots;
    }

    @Override
    public boolean canSetFacing(int side) {
        return side != 0 && side != 1;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public EnumFacing getOrientation() {
        return facing;
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        return CapabilityUtils.isCapabilityDisabled(capability, side, this) || super
              .isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        infuseStored.writeSustainedData(itemStack);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        infuseStored.readSustainedData(itemStack);
    }
}
