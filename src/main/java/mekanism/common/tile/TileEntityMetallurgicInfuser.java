package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.TileNetworkList;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.InfuseStorage;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismItem;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.tier.BaseTier;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;

public class TileEntityMetallurgicInfuser extends TileEntityOperationalMachine implements IComputerIntegration, ISideConfiguration, IConfigCardAccess, ITierUpgradeable,
      ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded", "getInfuse",
                                                         "getInfuseNeeded"};
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
        super(MekanismBlock.METALLURGIC_INFUSER, 0, 200);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{2}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{3}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{4}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Infuse", EnumColor.PURPLE, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{4, 0, 0, 3, 1, 2});

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));

        securityComponent = new TileComponentSecurity(this);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.discharge(4, this);
            ItemStack infuseInput = getInventory().get(1);
            if (!infuseInput.isEmpty()) {
                InfuseObject pendingInfuseInput = InfuseRegistry.getObject(infuseInput);
                if (pendingInfuseInput != null) {
                    if (infuseStored.getType() == null || infuseStored.getType() == pendingInfuseInput.type) {
                        if (infuseStored.getAmount() + pendingInfuseInput.stored <= MAX_INFUSE) {
                            infuseStored.increase(pendingInfuseInput);
                            infuseInput.shrink(1);
                        }
                    }
                }
            }

            MetallurgicInfuserRecipe recipe = RecipeHandler.getMetallurgicInfuserRecipe(getInput());
            if (canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= getEnergyPerTick()) {
                setActive(true);
                setEnergy(getEnergy() - getEnergyPerTick());
                if ((operatingTicks + 1) < ticksRequired) {
                    operatingTicks++;
                } else {
                    operate(recipe);
                    operatingTicks = 0;
                }
            } else if (prevEnergy >= getEnergy()) {
                setActive(false);
            }
            if (!canOperate(recipe)) {
                operatingTicks = 0;
            }
            prevEnergy = getEnergy();
        }
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier != BaseTier.BASIC) {
            return false;
        }

        world.removeBlock(getPos(), false);
        world.setBlockState(getPos(), MekanismBlock.BASIC_INFUSING_FACTORY.getBlock().getDefaultState(), 3);

        //TODO: Make this copy the settings over, probably make a method TileEntityMekanism#copySettings(TileEntityMekanism other)
        /*TileEntityFactory factory = Objects.requireNonNull((TileEntityFactory) world.getTileEntity(getPos()));
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
        factory.setControlType(getControlType());
        factory.prevEnergy = prevEnergy;
        factory.upgradeComponent.readFrom(upgradeComponent);
        factory.upgradeComponent.setUpgradeSlot(0);
        factory.ejectorComponent.readFrom(ejectorComponent);
        factory.ejectorComponent.setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
        factory.setRecipeType(type);
        factory.upgradeComponent.setSupported(Upgrade.GAS, type.fuelEnergyUpgrades());
        factory.securityComponent.readFrom(securityComponent);

        for (TransmissionType transmission : configComponent.getTransmissions()) {
            factory.configComponent.setConfig(transmission, configComponent.getConfig(transmission).asByteArray());
            factory.configComponent.setEjecting(transmission, configComponent.isEjecting(transmission));
        }

        //Infuser
        factory.infuseStored.copyFrom(infuseStored);

        factory.inventory.set(5, inventory.get(2));
        factory.inventory.set(1, inventory.get(4));
        factory.inventory.set(5 + 3, inventory.get(3));
        factory.inventory.set(0, inventory.get(0));
        factory.inventory.set(4, inventory.get(1));

        for (Upgrade upgrade : factory.upgradeComponent.getSupportedTypes()) {
            factory.recalculateUpgrades(upgrade);
        }
        factory.upgraded = true;
        factory.markDirty();*/
        return true;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 4) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        }
        return slotID == 3;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 3) {
            return false;
        } else if (slotID == 1) {
            InfuseObject infuseObject = InfuseRegistry.getObject(itemstack);
            return infuseObject != null && (infuseStored.getType() == null || infuseStored.getType() == infuseObject.type);
        } else if (slotID == 0) {
            return MekanismItem.SPEED_UPGRADE.itemMatches(itemstack) || MekanismItem.ENERGY_UPGRADE.itemMatches(itemstack);
        } else if (slotID == 2) {
            if (infuseStored.getType() != null) {
                return RecipeHandler.getMetallurgicInfuserRecipe(new InfusionInput(infuseStored, itemstack)) != null;
            }
            for (InfusionInput input : Recipe.METALLURGIC_INFUSER.get().keySet()) {
                if (ItemHandlerHelper.canItemStacksStack(input.inputStack, itemstack)) {
                    return true;
                }
            }
        } else if (slotID == 4) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return false;
    }

    public InfusionInput getInput() {
        return new InfusionInput(infuseStored, getInventory().get(2));
    }

    public void operate(MetallurgicInfuserRecipe recipe) {
        recipe.output(getInventory(), 2, 3, infuseStored);
        markDirty();
    }

    @Contract("null -> false")
    public boolean canOperate(MetallurgicInfuserRecipe recipe) {
        return recipe != null && recipe.canOperate(getInventory(), 2, 3, infuseStored);
    }

    public int getScaledInfuseLevel(int i) {
        return infuseStored.getAmount() * i / MAX_INFUSE;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        int amount = nbtTags.getInt("infuseStored");
        if (amount != 0) {
            infuseStored.setAmount(amount);
            infuseStored.setType(InfuseRegistry.get(nbtTags.getString("type")));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (infuseStored.getType() != null) {
            nbtTags.putString("type", infuseStored.getType().name);
            nbtTags.putInt("infuseStored", infuseStored.getAmount());
        } else {
            nbtTags.putString("type", "null");
        }
        nbtTags.putBoolean("sideDataStored", true);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int amount = dataStream.readInt();
            if (amount == 0) {
                infuseStored.setEmpty();
            } else {
                infuseStored.setAmount(amount);
            }
            return;
        }

        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            int amount = dataStream.readInt();
            if (amount > 0) {
                infuseStored.setAmount(amount);
                infuseStored.setType(InfuseRegistry.get(dataStream.readString()));
            } else {
                infuseStored.setEmpty();
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(infuseStored.getAmount());
        if (infuseStored.getAmount() > 0) {
            data.add(infuseStored.getType().name);
        }
        return data;
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
                return new Object[]{operatingTicks};
            case 2:
                return new Object[]{getDirection()};
            case 3:
                return new Object[]{canOperate(RecipeHandler.getMetallurgicInfuserRecipe(getInput()))};
            case 4:
                return new Object[]{getMaxEnergy()};
            case 5:
                return new Object[]{getNeededEnergy()};
            case 6:
                return new Object[]{infuseStored};
            case 7:
                return new Object[]{MAX_INFUSE - infuseStored.getAmount()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
    }

    @Override
    public boolean canSetFacing(@Nonnull Direction facing) {
        return facing != Direction.DOWN && facing != Direction.UP;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public Direction getOrientation() {
        return getDirection();
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        return configComponent.isCapabilityDisabled(capability, side, getDirection()) || super.isCapabilityDisabled(capability, side);
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