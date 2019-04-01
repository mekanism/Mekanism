package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.InfuseStorage;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory.MachineFuelType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StatUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;

public class TileEntityFactory extends TileEntityMachine implements IComputerIntegration, ISideConfiguration,
      IGasHandler, ISpecialConfigData, ITierUpgradeable, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "facing", "canOperate",
          "getMaxEnergy", "getEnergyNeeded"};
    private final MachineRecipe[] cachedRecipe;
    /**
     * This Factory's tier.
     */
    public FactoryTier tier;
    /**
     * An int[] used to track all current operations' progress.
     */
    public int[] progress;
    public int BASE_MAX_INFUSE = 1000;
    public int maxInfuse;
    /**
     * How many ticks it takes, by default, to run an operation.
     */
    public int BASE_TICKS_REQUIRED = 200;
    /**
     * How many ticks it takes, with upgrades, to run an operation
     */
    public int ticksRequired = 200;
    /**
     * How much secondary energy each operation consumes per tick
     */
    public double secondaryEnergyPerTick = 0;
    public int secondaryEnergyThisTick;
    /**
     * How long it takes this factory to switch recipe types.
     */
    public int RECIPE_TICKS_REQUIRED = 40;
    /**
     * How many recipe ticks have progressed.
     */
    public int recipeTicks;
    /**
     * The amount of infuse this machine has stored.
     */
    public InfuseStorage infuseStored = new InfuseStorage();

    public GasTank gasTank;

    public boolean sorting;

    public boolean upgraded;

    public double lastUsage;

    /**
     * This machine's current RedstoneControl type.
     */
    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    /**
     * This machine's recipe type.
     */
    private RecipeType recipeType = RecipeType.SMELTING;

    public TileEntityFactory() {
        this(FactoryTier.BASIC, BlockStateMachine.MachineType.BASIC_FACTORY);

        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY,
              TransmissionType.GAS);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{5, 6, 7}));
        configComponent
              .addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{8, 9, 10}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{1}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Extra", EnumColor.PURPLE, new int[]{4}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{4, 0, 0, 3, 1, 2});

        configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.DARK_RED, new int[]{0}));
        configComponent.fillConfig(TransmissionType.GAS, 1);
        configComponent.setCanEject(TransmissionType.GAS, false);

        configComponent.setInputConfig(TransmissionType.ENERGY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
    }

    public TileEntityFactory(FactoryTier type, MachineType machine) {
        super("null", machine.blockName, 0, 0, 0);

        tier = type;
        inventory = NonNullList.withSize(5 + type.processes * 2, ItemStack.EMPTY);
        progress = new int[type.processes];
        isActive = false;
        cachedRecipe = new MachineRecipe[tier.processes];

        gasTank = new GasTank(TileEntityAdvancedElectricMachine.MAX_GAS * tier.processes);
        maxInfuse = BASE_MAX_INFUSE * tier.processes;

        setRecipeType(recipeType);
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier.ordinal() != tier.ordinal() + 1 || tier == FactoryTier.ELITE) {
            return false;
        }

        world.setBlockToAir(getPos());
        world.setBlockState(getPos(), MekanismBlocks.MachineBlock.getStateFromMeta(5 + tier.ordinal() + 1), 3);

        TileEntityFactory factory = (TileEntityFactory) world.getTileEntity(getPos());

        //Basic
        factory.facing = facing;
        factory.clientFacing = clientFacing;
        factory.ticker = ticker;
        factory.redstone = redstone;
        factory.redstoneLastTick = redstoneLastTick;
        factory.doAutoSync = doAutoSync;

        //Electric
        factory.electricityStored = electricityStored;

        //Factory
        System.arraycopy(progress, 0, factory.progress, 0, tier.processes);

        factory.recipeTicks = recipeTicks;
        factory.isActive = isActive;
        factory.prevEnergy = prevEnergy;
        factory.gasTank.setGas(gasTank.getGas());
        factory.sorting = sorting;
        factory.controlType = controlType;
        factory.upgradeComponent.readFrom(upgradeComponent);
        factory.ejectorComponent.readFrom(ejectorComponent);
        factory.configComponent.readFrom(configComponent);
        factory.ejectorComponent
              .setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
        factory.setRecipeType(recipeType);
        factory.upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        factory.securityComponent.readFrom(securityComponent);
        factory.infuseStored = infuseStored;

        for (int i = 0; i < tier.processes + 5; i++) {
            factory.inventory.set(i, inventory.get(i));
        }

        for (int i = 0; i < tier.processes; i++) {
            int output = getOutputSlot(i);

            if (!inventory.get(output).isEmpty()) {
                int newOutput = 5 + factory.tier.processes + i;

                factory.inventory.set(newOutput, inventory.get(output));
            }
        }

        for (Upgrade upgrade : factory.upgradeComponent.getSupportedTypes()) {
            factory.recalculateUpgradables(upgrade);
        }

        factory.upgraded = true;

        factory.markDirty();
        Mekanism.packetHandler.sendToReceivers(
              new TileEntityMessage(Coord4D.get(factory), factory.getNetworkedData(new TileNetworkList())),
              new Range4D(Coord4D.get(factory)));

        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            if (ticker == 1) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
            }

            ChargeUtils.discharge(1, this);

            handleSecondaryFuel();
            sortInventory();

            if (!inventory.get(2).isEmpty() && inventory.get(3).isEmpty()) {
                RecipeType toSet = null;

                for (RecipeType type : RecipeType.values()) {
                    if (inventory.get(2).isItemEqual(type.getStack())) {
                        toSet = type;
                        break;
                    }
                }

                if (toSet != null && recipeType != toSet) {
                    if (recipeTicks < RECIPE_TICKS_REQUIRED) {
                        recipeTicks++;
                    } else {
                        recipeTicks = 0;

                        ItemStack returnStack = getMachineStack();

                        upgradeComponent.write(ItemDataUtils.getDataMap(returnStack));
                        upgradeComponent.setSupported(Upgrade.GAS, toSet.fuelEnergyUpgrades());
                        upgradeComponent.read(ItemDataUtils.getDataMapIfPresentNN(inventory.get(2)));

                        inventory.set(2, ItemStack.EMPTY);
                        inventory.set(3, returnStack);

                        setRecipeType(toSet);
                        gasTank.setGas(null);

                        secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);

                        world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);

                        MekanismUtils.saveChunk(this);
                    }
                } else {
                    recipeTicks = 0;
                }
            } else {
                recipeTicks = 0;
            }

            double prev = getEnergy();

            secondaryEnergyThisTick = recipeType.fuelEnergyUpgrades() ? StatUtils.inversePoisson(secondaryEnergyPerTick)
                  : (int) Math.ceil(secondaryEnergyPerTick);

            for (int process = 0; process < tier.processes; process++) {
                if (MekanismUtils.canFunction(this) && canOperate(getInputSlot(process), getOutputSlot(process))
                      && getEnergy() >= energyPerTick && gasTank.getStored() >= secondaryEnergyThisTick) {
                    if ((progress[process] + 1) < ticksRequired) {
                        progress[process]++;
                        gasTank.draw(secondaryEnergyThisTick, true);
                        electricityStored -= energyPerTick;
                    } else if ((progress[process] + 1) >= ticksRequired) {
                        operate(getInputSlot(process), getOutputSlot(process));

                        progress[process] = 0;
                        gasTank.draw(secondaryEnergyThisTick, true);
                        electricityStored -= energyPerTick;
                    }
                }

                if (!canOperate(getInputSlot(process), getOutputSlot(process))) {
                    if (!(recipeType.getFuelType() == MachineFuelType.ADVANCED && recipeType
                          .hasRecipe(inventory.get(getInputSlot(process))))) {
                        progress[process] = 0;
                    }
                }
            }

            boolean hasOperation = false;

            for (int i = 0; i < tier.processes; i++) {
                if (canOperate(getInputSlot(i), getOutputSlot(i))) {
                    hasOperation = true;
                    break;
                }
            }

            if (MekanismUtils.canFunction(this) && hasOperation && getEnergy() >= energyPerTick
                  && gasTank.getStored() >= secondaryEnergyThisTick) {
                setActive(true);
            } else {
                if (prevEnergy >= getEnergy()) {
                    setActive(false);
                }
            }

            if (infuseStored.amount <= 0) {
                infuseStored.amount = 0;
                infuseStored.type = null;
            }

            lastUsage = prev - getEnergy();
            prevEnergy = getEnergy();
        }
    }

    public RecipeType getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(RecipeType type) {
        recipeType = type;
        BASE_MAX_ENERGY = maxEnergy = 200 * tier.processes * recipeType.getEnergyUsage();
        BASE_ENERGY_PER_TICK = energyPerTick = recipeType.getEnergyUsage();
        upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);

        for (Upgrade upgrade : upgradeComponent.getSupportedTypes()) {
            recalculateUpgradables(upgrade);
        }

        if (hasWorld() && world.isRemote) {
            setSoundEvent(type.getSound());
        }
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
        return configComponent.hasSideForData(TransmissionType.ENERGY, facing, 1, side);
    }

    //can be optimised a lot to be linear
    public void sortInventory() {
        if (sorting) {
            boolean didOp = false;

            int[] inputSlots = null;

            List<InvID> invStacks = new ArrayList<>();

            if (tier == FactoryTier.BASIC) {
                inputSlots = new int[]{5, 6, 7};
            } else if (tier == FactoryTier.ADVANCED) {
                inputSlots = new int[]{5, 6, 7, 8, 9};
            } else if (tier == FactoryTier.ELITE) {
                inputSlots = new int[]{5, 6, 7, 8, 9, 10, 11};
            }

            for (int id : inputSlots) {
                invStacks.add(InvID.get(id, inventory));
            }

            for (InvID invID1 : invStacks) {
                for (InvID invID2 : invStacks) {
                    if (invID1.ID == invID2.ID || StackUtils.diffIgnoreEmpty(invID1.stack, invID2.stack)
                          || Math.abs(invID1.size() - invID2.size()) < 2) {
                        continue;
                    }

                    Pair<ItemStack, ItemStack> evened = StackUtils
                          .even(inventory.get(invID1.ID), inventory.get(invID2.ID));
                    inventory.set(invID1.ID, evened.getLeft());
                    inventory.set(invID2.ID, evened.getRight());

                    didOp = true;
                    break;
                }

                if (didOp) {
                    markDirty();
                    break;
                }
            }
        }
    }

    public double getSecondaryEnergyPerTick(RecipeType type) {
        return MekanismUtils.getSecondaryEnergyPerTickMean(this, type.getSecondaryEnergyPerTick());
    }

    public void handleSecondaryFuel() {
        if (!inventory.get(4).isEmpty()) {
            if (recipeType.getFuelType() == MachineFuelType.ADVANCED && gasTank.getNeeded() > 0) {
                if (inventory.get(4).getItem() instanceof IGasItem) {
                    GasStack gas = ((IGasItem) inventory.get(4).getItem()).getGas(inventory.get(4));

                    if (gas != null && recipeType.isValidGas(gas.getGas())) {
                        GasStack removed = GasUtils
                              .removeGas(inventory.get(4), gasTank.getGasType(), gasTank.getNeeded());
                        gasTank.receive(removed, true);
                    }

                    return;
                }

                GasStack stack = recipeType.getItemGas(inventory.get(4));
                int gasNeeded = gasTank.getNeeded();

                if (stack != null && stack.amount <= gasNeeded) {
                    gasTank.receive(stack, true);

                    inventory.get(4).shrink(1);
                }
            } else if (recipeType == RecipeType.INFUSING) {
                if (InfuseRegistry.getObject(inventory.get(4)) != null) {
                    InfuseObject infuse = InfuseRegistry.getObject(inventory.get(4));

                    if (infuseStored.type == null || infuseStored.type == infuse.type) {
                        if (infuseStored.amount + infuse.stored <= maxInfuse) {
                            infuseStored.amount += infuse.stored;
                            infuseStored.type = infuse.type;
                            inventory.get(4).shrink(1);
                        }
                    }
                }
            }
        }
    }

    public ItemStack getMachineStack() {
        return recipeType.getStack();
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        } else if (tier == FactoryTier.BASIC && slotID >= 8 && slotID <= 10) {
            return true;
        } else if (tier == FactoryTier.ADVANCED && slotID >= 10 && slotID <= 14) {
            return true;
        } else {
            return tier == FactoryTier.ELITE && slotID >= 12 && slotID <= 18;
        }

    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (tier == FactoryTier.BASIC) {
            if (slotID >= 8 && slotID <= 10) {
                return false;
            } else if (slotID >= 5 && slotID <= 7) {
                return recipeType.getAnyRecipe(itemstack, inventory.get(4), gasTank.getGasType(), infuseStored) != null;
            }
        } else if (tier == FactoryTier.ADVANCED) {
            if (slotID >= 10 && slotID <= 14) {
                return false;
            } else if (slotID >= 5 && slotID <= 9) {
                return recipeType.getAnyRecipe(itemstack, inventory.get(4), gasTank.getGasType(), infuseStored) != null;
            }
        } else if (tier == FactoryTier.ELITE) {
            if (slotID >= 12 && slotID <= 18) {
                return false;
            } else if (slotID >= 5 && slotID <= 11) {
                return recipeType.getAnyRecipe(itemstack, inventory.get(4), gasTank.getGasType(), infuseStored) != null;
            }
        }

        if (slotID == 0) {
            return itemstack.getItem() == MekanismItems.SpeedUpgrade
                  || itemstack.getItem() == MekanismItems.EnergyUpgrade;
        } else if (slotID == 1) {
            return ChargeUtils.canBeDischarged(itemstack);
        } else if (slotID == 4) {
            if (recipeType.getFuelType() == MachineFuelType.ADVANCED) {
                return recipeType.getItemGas(itemstack) != null;
            } else if (recipeType.getFuelType() == MachineFuelType.DOUBLE) {
                return recipeType.hasRecipeForExtra(itemstack);
            } else if (recipeType == RecipeType.INFUSING) {
                return InfuseRegistry.getObject(itemstack) != null && (infuseStored.type == null
                      || infuseStored.type == InfuseRegistry.getObject(itemstack).type);
            }
        }

        return false;
    }

    public int getScaledProgress(int i, int process) {
        return progress[process] * i / ticksRequired;
    }

    public int getScaledInfuseLevel(int i) {
        return infuseStored.amount * i / maxInfuse;
    }

    public int getScaledGasLevel(int i) {
        return gasTank.getStored() * i / gasTank.getMaxGas();
    }

    public int getScaledRecipeProgress(int i) {
        return recipeTicks * i / RECIPE_TICKS_REQUIRED;
    }

    public boolean canOperate(int inputSlot, int outputSlot) {
        if (inventory.get(inputSlot).isEmpty()) {
            return false;
        }

        int process = getOperation(inputSlot);

        if (recipeType.getFuelType() == MachineFuelType.ADVANCED) {
            if (cachedRecipe[process] instanceof AdvancedMachineRecipe &&
                  ((AdvancedMachineRecipe) cachedRecipe[process])
                        .inputMatches(inventory, inputSlot, gasTank, secondaryEnergyThisTick)) {
                return ((AdvancedMachineRecipe) cachedRecipe[process])
                      .canOperate(inventory, inputSlot, outputSlot, gasTank, secondaryEnergyThisTick);
            }

            AdvancedMachineRecipe<?> recipe = recipeType.getRecipe(inventory.get(inputSlot), gasTank.getGasType());
            cachedRecipe[process] = recipe;

            return recipe != null && recipe
                  .canOperate(inventory, inputSlot, outputSlot, gasTank, secondaryEnergyThisTick);

        } else if (recipeType.getFuelType() == MachineFuelType.DOUBLE) {
            if (cachedRecipe[process] instanceof DoubleMachineRecipe &&
                  ((DoubleMachineRecipe) cachedRecipe[process]).inputMatches(inventory, inputSlot, 4)) {
                return ((DoubleMachineRecipe) cachedRecipe[process]).canOperate(inventory, inputSlot, 4, outputSlot);
            }
            DoubleMachineRecipe<?> recipe = recipeType.getRecipe(inventory.get(inputSlot), inventory.get(4));
            cachedRecipe[process] = recipe;

            return recipe != null && recipe.canOperate(inventory, inputSlot, 4, outputSlot);
        }

        if (recipeType == RecipeType.INFUSING) {
            if (cachedRecipe[process] instanceof MetallurgicInfuserRecipe &&
                  ((MetallurgicInfuserRecipe) cachedRecipe[process]).inputMatches(inventory, inputSlot, infuseStored)) {
                return ((MetallurgicInfuserRecipe) cachedRecipe[process])
                      .canOperate(inventory, inputSlot, outputSlot, infuseStored);
            }
            InfusionInput input = new InfusionInput(infuseStored, inventory.get(inputSlot));
            MetallurgicInfuserRecipe recipe = RecipeHandler.getMetallurgicInfuserRecipe(input);
            cachedRecipe[process] = recipe;

            if (recipe == null) {
                return false;
            }

            return recipe.canOperate(inventory, inputSlot, outputSlot, infuseStored);
        }

        if (cachedRecipe[process] instanceof BasicMachineRecipe && ((BasicMachineRecipe) cachedRecipe[process])
              .inputMatches(inventory, inputSlot)) {
            return (((BasicMachineRecipe) cachedRecipe[process]).canOperate(inventory, inputSlot, outputSlot));
        }

        BasicMachineRecipe<?> recipe = recipeType.getRecipe(inventory.get(inputSlot));
        cachedRecipe[process] = recipe;

        return recipe != null && recipe.canOperate(inventory, inputSlot, outputSlot);

    }

    public void operate(int inputSlot, int outputSlot) {
        if (!canOperate(inputSlot, outputSlot)) {
            return;
        }

        int process = getOperation(inputSlot);

        if (cachedRecipe[process] == null) {//should never happen, but cant be too sure.
            Mekanism.logger
                  .debug("cachedRecipe was null, but we were asked to operate anyway?! {} @ {}", this, this.pos);
            return;
        }

        if (recipeType.getFuelType() == MachineFuelType.ADVANCED
              && cachedRecipe[process] instanceof AdvancedMachineRecipe) {
            AdvancedMachineRecipe<?> recipe = (AdvancedMachineRecipe<?>) cachedRecipe[process];

            recipe.operate(inventory, inputSlot, outputSlot, gasTank, secondaryEnergyThisTick);
        } else if (recipeType.getFuelType() == MachineFuelType.DOUBLE
              && cachedRecipe[process] instanceof DoubleMachineRecipe) {
            DoubleMachineRecipe<?> recipe = (DoubleMachineRecipe<?>) cachedRecipe[process];

            recipe.operate(inventory, inputSlot, 4, outputSlot);
        } else if (recipeType == RecipeType.INFUSING && cachedRecipe[process] instanceof MetallurgicInfuserRecipe) {
            MetallurgicInfuserRecipe recipe = (MetallurgicInfuserRecipe) cachedRecipe[process];

            recipe.output(inventory, inputSlot, outputSlot, infuseStored);
        } else {
            BasicMachineRecipe<?> recipe = (BasicMachineRecipe<?>) cachedRecipe[process];

            recipe.operate(inventory, inputSlot, outputSlot);
        }

        markDirty();
        ejectorComponent.outputItems();
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int type = dataStream.readInt();

            if (type == 0) {
                sorting = !sorting;
            } else if (type == 1) {
                gasTank.setGas(null);
                infuseStored.amount = 0;
                infuseStored.type = null;
            }

            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            RecipeType oldRecipe = recipeType;
            recipeType = RecipeType.values()[dataStream.readInt()];
            upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
            recipeTicks = dataStream.readInt();
            sorting = dataStream.readBoolean();
            upgraded = dataStream.readBoolean();
            lastUsage = dataStream.readDouble();
            infuseStored.amount = dataStream.readInt();
            if (infuseStored.amount > 0) {
                infuseStored.type = InfuseRegistry.get(PacketHandler.readString(dataStream));
            }

            if (recipeType != oldRecipe) {
                setRecipeType(recipeType);

                if (!upgraded) {
                    MekanismUtils.updateBlock(world, getPos());
                }
            }

            for (int i = 0; i < tier.processes; i++) {
                progress[i] = dataStream.readInt();
            }

            TileUtils.readTankData(dataStream, gasTank);
            if (upgraded) {
                markDirty();
                MekanismUtils.updateBlock(world, getPos());
                upgraded = false;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        setRecipeType(RecipeType.values()[nbtTags.getInteger("recipeType")]);
        upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        recipeTicks = nbtTags.getInteger("recipeTicks");
        sorting = nbtTags.getBoolean("sorting");
        infuseStored.amount = nbtTags.getInteger("infuseStored");
        infuseStored.type = InfuseRegistry.get(nbtTags.getString("type"));

        for (int i = 0; i < tier.processes; i++) {
            progress[i] = nbtTags.getInteger("progress" + i);
        }

        gasTank.read(nbtTags.getCompoundTag("gasTank"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("recipeType", recipeType.ordinal());
        nbtTags.setInteger("recipeTicks", recipeTicks);
        nbtTags.setBoolean("sorting", sorting);
        nbtTags.setInteger("infuseStored", infuseStored.amount);

        if (infuseStored.type != null) {
            nbtTags.setString("type", infuseStored.type.name);
        } else {
            nbtTags.setString("type", "null");
        }

        for (int i = 0; i < tier.processes; i++) {
            nbtTags.setInteger("progress" + i, progress[i]);
        }

        nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));

        return nbtTags;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(recipeType.ordinal());
        data.add(recipeTicks);
        data.add(sorting);
        data.add(upgraded);
        data.add(lastUsage);

        data.add(infuseStored.amount);
        if (infuseStored.amount > 0) {
            data.add(infuseStored.type.name);
        }

        data.add(progress);
        TileUtils.addTankData(data, gasTank);

        upgraded = false;

        return data;
    }

    public int getInputSlot(int operation) {
        return 5 + operation;
    }

    /* reverse of the above */
    private int getOperation(int inputSlot) {
        return inputSlot - 5;
    }

    public int getOutputSlot(int operation) {
        return 5 + tier.processes + operation;
    }

    @Nonnull
    @Override
    public String getName() {
        if (I18n.canTranslate("tile." + tier.getBaseTier().getName() + recipeType.getTranslationKey() + "Factory")) {
            return LangUtils
                  .localize("tile." + tier.getBaseTier().getName() + recipeType.getTranslationKey() + "Factory");
        }

        return tier.getBaseTier().getLocalizedName() + " " + recipeType.getLocalizedName() + " " + super.getName();
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
                if (arguments[0] == null) {
                    return new Object[]{"Please provide a target operation."};
                }

                if (!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer)) {
                    return new Object[]{"Invalid characters."};
                }

                if ((Integer) arguments[0] < 0 || (Integer) arguments[0] > progress.length) {
                    return new Object[]{"No such operation found."};
                }

                return new Object[]{progress[(Integer) arguments[0]]};
            case 2:
                return new Object[]{facing};
            case 3:
                if (arguments[0] == null) {
                    return new Object[]{"Please provide a target operation."};
                }

                if (!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer)) {
                    return new Object[]{"Invalid characters."};
                }

                if ((Integer) arguments[0] < 0 || (Integer) arguments[0] > progress.length) {
                    return new Object[]{"No such operation found."};
                }

                return new Object[]{
                      canOperate(getInputSlot((Integer) arguments[0]), getOutputSlot((Integer) arguments[0]))};
            case 4:
                return new Object[]{getMaxEnergy()};
            case 5:
                return new Object[]{getMaxEnergy() - getEnergy()};
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
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return gasTank.receive(stack, doTransfer);
        }

        return 0;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        if (configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(0)) {
            return recipeType.canReceiveGas(side, type);
        }

        return false;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY
              || capability == Capabilities.CONFIG_CARD_CAPABILITY
              || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY
              || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.CONFIG_CARD_CAPABILITY
              || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (configComponent.isCapabilityDisabled(capability, side, facing)) {
            return true;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return !recipeType.supportsGas();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);

        switch (upgrade) {
            case ENERGY:
                energyPerTick = MekanismUtils
                      .getEnergyPerTick(this, BASE_ENERGY_PER_TICK); // incorporate speed upgrades
                break;
            case GAS:
                secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
                break;
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
                secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
                break;
            default:
                break;
        }
    }

    @Override
    public NBTTagCompound getConfigurationData(NBTTagCompound nbtTags) {
        nbtTags.setBoolean("sorting", sorting);

        return nbtTags;
    }

    @Override
    public void setConfigurationData(NBTTagCompound nbtTags) {
        sorting = nbtTags.getBoolean("sorting");
    }

    @Override
    public String getDataType() {
        return tier.getBaseTier().getLocalizedName() + " " + recipeType.getLocalizedName() + " " + super.getName();
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        infuseStored.writeSustainedData(itemStack);
        GasUtils.writeSustainedData(gasTank, itemStack);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        infuseStored.readSustainedData(itemStack);
        GasUtils.readSustainedData(gasTank, itemStack);
    }

    public static class InvID {

        public ItemStack stack;
        public int ID;

        public InvID(ItemStack s, int i) {
            stack = s;
            ID = i;
        }

        public static InvID get(int id, NonNullList<ItemStack> inv) {
            return new InvID(inv.get(id), id);
        }

        public int size() {
            return !stack.isEmpty() ? stack.getCount() : 0;
        }

        public Item item() {
            return !stack.isEmpty() ? stack.getItem() : null;
        }
    }
}
