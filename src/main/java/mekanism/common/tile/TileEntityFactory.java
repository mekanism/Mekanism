package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.TileNetworkList;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CombinerCachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.cache.ItemStackGasToItemStackCachedRecipe;
import mekanism.api.recipes.cache.ItemStackToItemStackCachedRecipe;
import mekanism.api.recipes.cache.MetallurgicInfuserCachedRecipe;
import mekanism.api.recipes.cache.SawmillCachedRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.InfuseStorage;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFactory.MachineFuelType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.FactoryTier;
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
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.ItemHandlerHelper;

//TODO: 1.14, as all factories have their own "TileEntityType", extend TileEntityFactory and have it pass the proper IMekanismRecipe type to this class
// That should allow for things to be a lot cleaner and allow for not having ICachedRecipeHolder be untyped
public class TileEntityFactory extends TileEntityMachine implements IComputerIntegration, ISideConfiguration, IGasHandler,
      ISpecialConfigData, ITierUpgradeable, ISustainedData, IComparatorSupport, ICachedRecipeHolder {

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
    private final CachedRecipe[] cachedRecipes;
    private boolean[] activeStates;
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
    private double secondaryEnergyPerTick = 0;
    private int secondaryEnergyThisTick;
    /**
     * How long it takes this factory to switch recipe types.
     */
    private static int RECIPE_TICKS_REQUIRED = 40;
    /**
     * How many recipe ticks have progressed.
     */
    private int recipeTicks;
    /**
     * The amount of infuse this machine has stored.
     */
    public final InfuseStorage infuseStored = new InfuseStorage();

    public final GasTank gasTank;

    public boolean sorting;

    public boolean upgraded;

    public double lastUsage;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    /**
     * This machine's recipe type.
     */
    @Nonnull
    private RecipeType recipeType = RecipeType.SMELTING;

    public TileEntityFactory() {
        this(FactoryTier.BASIC, MachineType.BASIC_FACTORY);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.GAS);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[]{5, 6, 7}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[]{8, 9, 10}));
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
        super("null", machine, 0);
        tier = type;
        inventory = NonNullList.withSize(5 + type.processes * 2, ItemStack.EMPTY);
        progress = new int[type.processes];
        isActive = false;
        cachedRecipes = new CachedRecipe[tier.processes];
        activeStates = new boolean[cachedRecipes.length];
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

        TileEntityFactory factory = Objects.requireNonNull((TileEntityFactory) world.getTileEntity(getPos()));

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
        //TODO: Transfer cached recipe
        //factory.prevEnergy = prevEnergy;
        factory.gasTank.setGas(gasTank.getGas());
        factory.sorting = sorting;
        factory.setControlType(getControlType());
        factory.upgradeComponent.readFrom(upgradeComponent);
        factory.ejectorComponent.readFrom(ejectorComponent);
        factory.configComponent.readFrom(configComponent);
        factory.ejectorComponent.setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
        factory.setRecipeType(recipeType);
        factory.upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        factory.securityComponent.readFrom(securityComponent);
        factory.infuseStored.copyFrom(infuseStored);

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
        Mekanism.packetHandler.sendUpdatePacket(factory);
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
                    if (ItemHandlerHelper.canItemStacksStack(inventory.get(2), type.getStack())) {
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
            secondaryEnergyThisTick = recipeType.fuelEnergyUpgrades() ? StatUtils.inversePoisson(secondaryEnergyPerTick) : (int) Math.ceil(secondaryEnergyPerTick);

            for (int i = 0; i < cachedRecipes.length; i++) {
                CachedRecipe cachedRecipe = cachedRecipes[i] = getUpdatedCache(cachedRecipes[i], i);
                if (cachedRecipe != null) {
                    cachedRecipe.process();
                } else {
                    //If we don't have a recipe in that slot make sure that our active state for that position is false
                    //TODO: Check if this is needed, it probably is already the case that if the cached recipe is null then
                    // we should already have activeState as false
                    activeStates[i] = false;
                }
            }
            //Update the active state based on the current active state of each recipe
            boolean isActive = false;
            for (boolean state : activeStates) {
                if (state) {
                    isActive = true;
                    break;
                }
            }
            setActive(isActive);
        }
    }

    @Nonnull
    public RecipeType getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(@Nonnull RecipeType type) {
        recipeType = Objects.requireNonNull(type);
        BASE_MAX_ENERGY = maxEnergy = tier.processes * Math.max(0.5D * recipeType.getEnergyStorage(), recipeType.getEnergyUsage());
        BASE_ENERGY_PER_TICK = energyPerTick = recipeType.getEnergyUsage();
        upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);

        if (type.getFuelType() == MachineFuelType.CHANCE) {
            SideData data = configComponent.getOutputs(TransmissionType.ITEM).get(2);
            //Append the "extra" slot to the available slots
            data.availableSlots = Arrays.copyOf(data.availableSlots, data.availableSlots.length + 1);
            data.availableSlots[data.availableSlots.length - 1] = 4;
        }

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

    public void sortInventory() {
        if (sorting) {
            int[] inputSlots;
            if (tier == FactoryTier.BASIC) {
                inputSlots = new int[]{5, 6, 7};
            } else if (tier == FactoryTier.ADVANCED) {
                inputSlots = new int[]{5, 6, 7, 8, 9};
            } else if (tier == FactoryTier.ELITE) {
                inputSlots = new int[]{5, 6, 7, 8, 9, 10, 11};
            } else {
                //If something went wrong finding the tier don't sort it
                return;
            }
            for (int i = 0; i < inputSlots.length; i++) {
                int slotID = inputSlots[i];
                ItemStack stack = inventory.get(slotID);
                int count = stack.getCount();
                ItemStack output = inventory.get(tier.processes + slotID);
                for (int j = i + 1; j < inputSlots.length; j++) {
                    int checkSlotID = inputSlots[j];
                    ItemStack checkStack = inventory.get(checkSlotID);
                    if (Math.abs(count - checkStack.getCount()) < 2 || !InventoryUtils.areItemsStackable(stack, checkStack)) {
                        continue;
                    }
                    //TODO: This if statement used to update the cache. Is there any reason to still do so?
                    // Or can we just let that be handled by the operation.
                    // If we don't update the cache now then doing multiple levels of sorting may fail
                    //Output/Input will not match
                    // Only check if the input spot is empty otherwise assume it works
                    if (stack.isEmpty() && !inputProducesOutput(checkSlotID, checkStack, output, true) ||
                        checkStack.isEmpty() && !inputProducesOutput(slotID, stack, inventory.get(tier.processes + checkSlotID), true)) {
                        continue;
                    }

                    //Balance the two slots
                    int total = count + checkStack.getCount();
                    ItemStack newStack = stack.isEmpty() ? checkStack : stack;
                    inventory.set(slotID, StackUtils.size(newStack, (total + 1) / 2));
                    inventory.set(checkSlotID, StackUtils.size(newStack, total / 2));

                    markDirty();
                    return;
                }
            }
        }
    }

    /**
     * Checks if the cached recipe (or recipe for current factory if the cache is out of date) can produce a specific output.
     *
     * @param slotID        Slot ID to grab the cached recipe of.
     * @param fallbackInput Used if the cached recipe is null or to validate the cached recipe is not out of date.
     * @param output        The output we want.
     * @param updateCache   True to make the cached recipe get updated if it is out of date.
     *
     * @return True if the recipe produces the given output.
     */
    public boolean inputProducesOutput(int slotID, ItemStack fallbackInput, ItemStack output, boolean updateCache) {
        if (output.isEmpty()) {
            return true;
        }
        int process = getOperation(slotID);
        CachedRecipe cached = cachedRecipes[process];
        if (cached != null) {
            ItemStackIngredient recipeInput = null;
            boolean secondaryMatch = true;
            IMekanismRecipe cachedRecipe = cached.getRecipe();
            if (cachedRecipe instanceof ItemStackToItemStackRecipe) {
                recipeInput = ((ItemStackToItemStackRecipe) cachedRecipe).getInput();
            } else if (cachedRecipe instanceof ItemStackGasToItemStackRecipe) {
                ItemStackGasToItemStackRecipe recipe = (ItemStackGasToItemStackRecipe) cachedRecipe;
                recipeInput = recipe.getItemInput();
                secondaryMatch = gasTank.getGasType() == null || recipe.getGasInput().testType(gasTank.getGasType());
            } else if (cachedRecipe instanceof CombinerRecipe) {
                CombinerRecipe recipe = (CombinerRecipe) cachedRecipe;
                recipeInput = recipe.getMainInput();
                ItemStack extra = inventory.get(4);
                secondaryMatch = extra.isEmpty() || recipe.getExtraInput().testType(extra);
            } else if (cachedRecipe instanceof MetallurgicInfuserRecipe) {
                MetallurgicInfuserRecipe recipe = (MetallurgicInfuserRecipe) cachedRecipe;
                recipeInput = recipe.getItemInput();
                //Type cannot be null or the amount would be zero
                secondaryMatch = infuseStored.getAmount() == 0 || recipe.getInfusionInput().testType(new InfuseObject(infuseStored.getType(), infuseStored.getAmount()));
            } else if (cachedRecipe instanceof SawmillRecipe) {
                recipeInput = ((SawmillRecipe) cachedRecipe).getInput();
            }
            if (recipeInput != null && secondaryMatch && recipeInput.testType(fallbackInput)) {
                //Our input matches the recipe we have cached for this slot
                return true;
            }
            //If there is no cached item input or it doesn't match our fallback then it is an out of date cache,
            // so we ignore the fact that we have a cache and set our local copy of it to null to ensure it doesn't accidentally get used
            cached = null;
        }
        //TODO: Decide if recipe.getOutput *should* assume that it is given a valid input or not
        // Here we are using it as if it is not assuming it, but that is in part because it currently does not care about the value passed
        // and if something does have extra checking to check the input as long as it checks for invalid ones this should still work
        IMekanismRecipe foundRecipe = null;
        if (recipeType == RecipeType.SMELTING || recipeType == RecipeType.ENRICHING || recipeType == RecipeType.CRUSHING) {
            Recipe<ItemStackToItemStackRecipe> recipes = getRecipes();
            foundRecipe = recipes.findFirst(recipe -> recipe.getInput().testType(fallbackInput) && ItemHandlerHelper.canItemStacksStack(recipe.getOutput(fallbackInput), output));
        } else if (recipeType == RecipeType.COMPRESSING || recipeType == RecipeType.PURIFYING || recipeType == RecipeType.INJECTING) {
            Recipe<ItemStackGasToItemStackRecipe> recipes = getRecipes();
            GasStack gasStack = gasTank.getGas();
            Gas gas = gasStack == null || gasStack.amount == 0 ? null : gasStack.getGas();
            foundRecipe = recipes.findFirst(recipe -> {
                if (recipe.getItemInput().testType(fallbackInput)) {
                    //If we don't have a gas stored ignore checking for a match
                    if (gas == null || recipe.getGasInput().testType(gas)) {
                        //TODO: Give it something that is not null when we don't have a stored gas stack
                        return ItemHandlerHelper.canItemStacksStack(recipe.getOutput(fallbackInput, gasStack), output);
                    }
                }
                return false;
            });
        } else if (recipeType == RecipeType.COMBINING) {
            ItemStack extra = inventory.get(4);
            foundRecipe = Recipe.COMBINER.findFirst(recipe -> {
                if (recipe.getMainInput().testType(fallbackInput)) {
                    if (extra.isEmpty() || recipe.getExtraInput().testType(extra)) {
                        return ItemHandlerHelper.canItemStacksStack(recipe.getOutput(fallbackInput, extra), output);
                    }
                }
                return false;
            });
        } else if (recipeType == RecipeType.INFUSING) {
            int stored = infuseStored.getAmount();
            InfuseObject infuseObject = stored == 0 ? null : new InfuseObject(infuseStored.getType(), stored);
            foundRecipe = Recipe.METALLURGIC_INFUSER.findFirst(recipe -> {
                if (recipe.getItemInput().testType(fallbackInput)) {
                    if (stored == 0 || recipe.getInfusionInput().testType(infuseObject)) {
                        //TODO: Should this pass infuseObject instead of infuseStored?
                        return ItemHandlerHelper.canItemStacksStack(recipe.getOutput(infuseStored, fallbackInput), output);
                    }
                }
                return false;
            });
        } else if (recipeType == RecipeType.SAWING) {
            ItemStack extra = inventory.get(4);
            foundRecipe = Recipe.PRECISION_SAWMILL.findFirst(recipe -> {
                if (recipe.getInput().testType(fallbackInput)) {
                    ChanceOutput chanceOutput = recipe.getOutput(fallbackInput);
                    if (ItemHandlerHelper.canItemStacksStack(chanceOutput.getMainOutput(), output)) {
                        //If the input is good and the primary output matches, make sure that the secondary
                        // output of this recipe will stack with what is currently in the secondary slot
                        if (extra.isEmpty()) {
                            return true;
                        }
                        ItemStack secondaryOutput = chanceOutput.getMaxSecondaryOutput();
                        return secondaryOutput.isEmpty() || ItemHandlerHelper.canItemStacksStack(secondaryOutput, extra);
                    }
                }
                return false;
            });
        }
        if (foundRecipe == null) {
            //We could not find any valid recipe for the given item that matches the items in the current output slots
            return false;
        }
        if (updateCache) {
            //If we want to update the cache, then create a new cache with the recipe we found
            CachedRecipe newCachedRecipe = createNewCachedRecipe(foundRecipe, process);
            if (newCachedRecipe == null) {
                //If we want to update the cache but failed to create a new cache then return that the item is not valid for the slot as something goes wrong
                // I believe we can actually make createNewCachedRecipe Nonnull which will remove this if statement
                return false;
            }
            cachedRecipes[process] = newCachedRecipe;
        }
        return true;
    }

    public double getSecondaryEnergyPerTick(RecipeType type) {
        return MekanismUtils.getSecondaryEnergyPerTickMean(this, type.getSecondaryEnergyPerTick());
    }

    @Nullable
    public GasStack getItemGas(ItemStack itemStack) {
        if (recipeType.getFuelType() == MachineFuelType.ADVANCED) {
            return GasConversionHandler.getItemGas(itemStack, gasTank, recipeType::isValidGas);
        }
        return null;
    }

    public void handleSecondaryFuel() {
        ItemStack extra = inventory.get(4);
        if (!extra.isEmpty()) {
            if (recipeType.getFuelType() == MachineFuelType.ADVANCED && gasTank.getNeeded() > 0) {
                GasStack gasStack = getItemGas(extra);
                if (gasStack != null) {
                    Gas gas = gasStack.getGas();
                    if (gasTank.canReceive(gas) && gasTank.getNeeded() >= gasStack.amount) {
                        if (extra.getItem() instanceof IGasItem) {
                            IGasItem item = (IGasItem) extra.getItem();
                            gasTank.receive(item.removeGas(extra, gasStack.amount), true);
                        } else {
                            gasTank.receive(gasStack, true);
                            extra.shrink(1);
                        }
                    }
                }
            } else if (recipeType == RecipeType.INFUSING) {
                InfuseObject pendingInfusionInput = InfuseRegistry.getObject(extra);
                if (pendingInfusionInput != null) {
                    if (infuseStored.getType() == null || infuseStored.getType() == pendingInfusionInput.type) {
                        if (infuseStored.getAmount() + pendingInfusionInput.stored <= maxInfuse) {
                            infuseStored.increase(pendingInfusionInput);
                            extra.shrink(1);
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
        } else if (tier == FactoryTier.ELITE && slotID >= 12 && slotID <= 18) {
            return true;
        } else if (recipeType.getFuelType() == MachineFuelType.CHANCE && slotID == 4) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canInsertItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 1) {
            return ChargeUtils.canBeDischarged(itemstack);
        } else if (isInputSlot(slotID)) {
            return inputProducesOutput(slotID, itemstack, inventory.get(tier.processes + slotID), false);
        }
        //TODO: Only allow inserting into extra slot if it can go in
        return super.canInsertItem(slotID, itemstack, side);
    }

    private boolean isInputSlot(int slotID) {
        return slotID >= 5 && (tier == FactoryTier.BASIC ? slotID <= 7 : tier == FactoryTier.ADVANCED ? slotID <= 9 : tier == FactoryTier.ELITE && slotID <= 11);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return itemstack.getItem() == MekanismItems.SpeedUpgrade || itemstack.getItem() == MekanismItems.EnergyUpgrade;
        } else if (slotID == 1) {
            return ChargeUtils.canBeDischarged(itemstack);
        } else if (slotID == 4) {
            if (recipeType.getFuelType() == MachineFuelType.ADVANCED) {
                return getItemGas(itemstack) != null;
            } else if (recipeType.getFuelType() == MachineFuelType.DOUBLE) {
                return recipeType.hasRecipeForExtra(itemstack);
            } else if (recipeType == RecipeType.INFUSING) {
                return InfuseRegistry.getObject(itemstack) != null && (infuseStored.getType() == null || infuseStored.getType() == InfuseRegistry.getObject(itemstack).type);
            }
        } else if (isInputSlot(slotID)) {
            return recipeType.getAnyRecipe(itemstack, inventory.get(4), gasTank.getGasType(), infuseStored) != null;
        }
        return false;
    }


    public int getScaledProgress(int i, int process) {
        return progress[process] * i / ticksRequired;
    }

    public int getScaledInfuseLevel(int i) {
        return infuseStored.getAmount() * i / maxInfuse;
    }

    public int getScaledGasLevel(int i) {
        return gasTank.getStored() * i / gasTank.getMaxGas();
    }

    public int getScaledRecipeProgress(int i) {
        return recipeTicks * i / RECIPE_TICKS_REQUIRED;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int type = dataStream.readInt();
            if (type == 0) {
                sorting = !sorting;
            } else if (type == 1) {
                gasTank.setGas(null);
                infuseStored.setEmpty();
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
            int amount = dataStream.readInt();
            if (amount > 0) {
                infuseStored.setAmount(amount);
                infuseStored.setType(InfuseRegistry.get(PacketHandler.readString(dataStream)));
            } else {
                infuseStored.setEmpty();
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
        int amount = nbtTags.getInteger("infuseStored");
        if (amount != 0) {
            infuseStored.setAmount(amount);
            infuseStored.setType(InfuseRegistry.get(nbtTags.getString("type")));
        }
        for (int i = 0; i < tier.processes; i++) {
            progress[i] = nbtTags.getInteger("progress" + i);
        }
        gasTank.read(nbtTags.getCompoundTag("gasTank"));
        GasUtils.clearIfInvalid(gasTank, recipeType::isValidGas);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("recipeType", recipeType.ordinal());
        nbtTags.setInteger("recipeTicks", recipeTicks);
        nbtTags.setBoolean("sorting", sorting);
        if (infuseStored.getType() != null) {
            nbtTags.setString("type", infuseStored.getType().name);
            nbtTags.setInteger("infuseStored", infuseStored.getAmount());
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

        data.add(infuseStored.getAmount());
        if (infuseStored.getAmount() > 0) {
            data.add(infuseStored.getType().name);
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
        if (LangUtils.canLocalize("tile." + tier.getBaseTier().getName() + recipeType.getTranslationKey() + "Factory")) {
            return LangUtils.localize("tile." + tier.getBaseTier().getName() + recipeType.getTranslationKey() + "Factory");
        }
        return tier.getBaseTier().getLocalizedName() + " " + recipeType.getLocalizedName() + " " + super.getName();
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
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
                if ((Integer) arguments[0] < 0 || (Integer) arguments[0] > cachedRecipes.length) {
                    return new Object[]{"No such operation found."};
                }
                //TODO: potentially simplify this, or at least get a new cached recipe if it is null
                CachedRecipe cachedRecipe = cachedRecipes[(Integer) arguments[0]];
                return new Object[]{cachedRecipe != null && cachedRecipe.hasResourcesForTick() && cachedRecipe.hasRoomForOutput()};
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
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
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
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.CONFIG_CARD_CAPABILITY
               || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY || super.hasCapability(capability, side);
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
            //If the gas capability is not disabled, check if this machine even actually supports gas
            return !recipeType.supportsGas();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        switch (upgrade) {
            case ENERGY:
                energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK); // incorporate speed upgrades
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

    @Override
    public int getRedstoneLevel() {
        return Container.calcRedstoneFromInventory(this);
    }

    @Nonnull
    @Override
    public Recipe getRecipes() {
        return recipeType.getRecipe();
    }

    @Nullable
    @Override
    public IMekanismRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inventory.get(getInputSlot(cacheIndex));
        if (stack.isEmpty()) {
            return null;
        }
        if (recipeType == RecipeType.SMELTING || recipeType == RecipeType.ENRICHING || recipeType == RecipeType.CRUSHING) {
            Recipe<ItemStackToItemStackRecipe> recipes = getRecipes();
            return recipes.findFirst(recipe -> recipe.test(stack));
        } else if (recipeType == RecipeType.COMPRESSING || recipeType == RecipeType.PURIFYING || recipeType == RecipeType.INJECTING) {
            GasStack gasStack = gasTank.getGas();
            if (gasStack == null || gasStack.amount == 0) {
                return null;
            }
            Gas gas = gasStack.getGas();
            if (gas == null) {
                return null;
            }
            Recipe<ItemStackGasToItemStackRecipe> recipes = getRecipes();
            return recipes.findFirst(recipe -> recipe.test(stack, gas));
        } else if (recipeType == RecipeType.SAWING) {
            return Recipe.PRECISION_SAWMILL.findFirst(recipe -> recipe.test(stack));
        } else if (recipeType == RecipeType.COMBINING) {
            ItemStack extra = inventory.get(4);
            return extra.isEmpty() ? null : Recipe.COMBINER.findFirst(recipe -> recipe.test(stack, extra));
        } else if (recipeType == RecipeType.INFUSING) {
            return Recipe.METALLURGIC_INFUSER.findFirst(recipe -> recipe.test(infuseStored, stack));
        }
        return null;
    }

    @Override
    public CachedRecipe createNewCachedRecipe(@Nonnull IMekanismRecipe recipe, int cacheIndex) {
        int inputSlot = getInputSlot(cacheIndex);
        int outputSlot = getOutputSlot(cacheIndex);
        BooleanSupplier canFunction = () -> MekanismUtils.canFunction(this);
        DoubleSupplier perTickEnergy = () -> energyPerTick;
        IntSupplier requiredTicks = () -> ticksRequired;
        Consumer<Boolean> setActive = active -> activeStates[cacheIndex] = active;
        DoubleConsumer useEnergy = energy -> setEnergy(getEnergy() - energy);
        Supplier<@NonNull ItemStack> inputStack = () -> inventory.get(inputSlot);
        if (recipeType == RecipeType.SMELTING || recipeType == RecipeType.ENRICHING || recipeType == RecipeType.CRUSHING) {
            ItemStackToItemStackRecipe castedRecipe = (ItemStackToItemStackRecipe) recipe;
            return new ItemStackToItemStackCachedRecipe(castedRecipe, canFunction, perTickEnergy, this::getEnergy, requiredTicks, setActive, useEnergy, this::markDirty,
                  inputStack, OutputHelper.getAddToOutput(inventory, outputSlot));
        } else if (recipeType == RecipeType.COMPRESSING || recipeType == RecipeType.PURIFYING || recipeType == RecipeType.INJECTING) {
            ItemStackGasToItemStackRecipe castedRecipe = (ItemStackGasToItemStackRecipe) recipe;
            return new ItemStackGasToItemStackCachedRecipe(castedRecipe, canFunction, perTickEnergy, this::getEnergy, requiredTicks, setActive, useEnergy, this::markDirty,
                  inputStack, () -> gasTank, () -> secondaryEnergyThisTick, OutputHelper.getAddToOutput(inventory, outputSlot));
        } else if (recipeType == RecipeType.COMBINING) {
            CombinerRecipe castedRecipe = (CombinerRecipe) recipe;
            return new CombinerCachedRecipe(castedRecipe, canFunction, perTickEnergy, this::getEnergy, requiredTicks, setActive, useEnergy, this::markDirty,
                  inputStack, () -> inventory.get(4), OutputHelper.getAddToOutput(inventory, outputSlot));
        } else if (recipeType == RecipeType.INFUSING) {
            MetallurgicInfuserRecipe castedRecipe = (MetallurgicInfuserRecipe) recipe;
            return new MetallurgicInfuserCachedRecipe(castedRecipe, canFunction, perTickEnergy, this::getEnergy, requiredTicks, setActive, useEnergy, this::markDirty,
                  () -> infuseStored, inputStack, OutputHelper.getAddToOutput(inventory, outputSlot));
        } else if (recipeType == RecipeType.SAWING) {
            SawmillRecipe castedRecipe = (SawmillRecipe) recipe;
            return new SawmillCachedRecipe(castedRecipe, canFunction, perTickEnergy, this::getEnergy, requiredTicks, setActive, useEnergy, this::markDirty,
                  inputStack, OutputHelper.getAddToOutput(inventory, outputSlot, 4));
        }
        //TODO: Do we have to invalidate cache when recipe type changes/how to invalidate it
        return null;
    }
}