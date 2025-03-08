package mekanism.common.attachments.containers.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.ContainsRecipe;
import mekanism.common.attachments.containers.chemical.AttachedChemicals;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.attachments.containers.fluid.AttachedFluids;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class ItemSlotsBuilder {

    //Note: For a lot of slots with specific helper methods we can simply use a ComponentBackedInventorySlot as we don't have any overrides or desire to call those methods while on an itemstack
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> BASIC_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> BASIC_INPUT_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> OUTPUT_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue());

    //Copy of predicates from FuelInventorySlot
    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> FUEL_CAN_EXTRACT = (stack, automationType) -> automationType == AutomationType.MANUAL || stack.getBurnTime(null) == 0;
    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> FUEL_CAN_INSERT = (stack, automationType) -> stack.getBurnTime(null) != 0;
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> FUEL_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, FUEL_CAN_EXTRACT, FUEL_CAN_INSERT, ConstantPredicates.alwaysTrue());

    //Security Inventory Slot
    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> SECURITY_LOCK_CAN_EXTRACT = (stack, automationType) -> automationType == AutomationType.MANUAL || SecurityInventorySlot.LOCK_EXTRACT_PREDICATE.test(stack);
    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> SECURITY_LOCK_CAN_INSERT = (stack, automationType) -> SecurityInventorySlot.LOCK_INSERT_PREDICATE.test(stack);
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> SECURITY_LOCK_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, SECURITY_LOCK_CAN_EXTRACT, SECURITY_LOCK_CAN_INSERT, SecurityInventorySlot.VALIDATOR);

    //FormulaInventorySlot
    //Note: We skip making the extra checks based on the formula and just allow all items
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> FORMULA_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), TileEntityFormulaicAssemblicator.FORMULA_SLOT_VALIDATOR);

    //QIO drive slot
    //Note: As we don't have to update the presence of a drive or remove it from the frequency we can make do with just using a basic slot
    //TODO - 1.20.4: Evaluate if copy the notExternal is correct or do we want this to have some other checks
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> QIO_DRIVE_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.notExternal(), QIODriveSlot.IS_QIO_ITEM);

    //QIO Dashboard Crafting WINDOW
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> QIO_DASHBOARD_INPUT_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    //Note: We don't allow external means to modify this slot as it truthfully only exists to make logic easier
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> QIO_DASHBOARD_OUTPUT_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, ConstantPredicates.internalOnly(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue());

    //EnergyInventorySlot
    //Note: As energy is untyped we don't have to do extra checks about what is currently stored or not on the attached stack
    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> FILL_CONVERT_ENERGY_SLOT_CAN_EXTRACT = (stack, automationType) ->
          //Allow extraction if something went horribly wrong, and we are not an energy container item or no longer have any energy left to give,
          // or we are no longer a valid conversion, this might happen after a reload for example
          automationType == AutomationType.MANUAL || !EnergyInventorySlot.fillInsertCheck(stack) && EnergyInventorySlot.getPotentialConversion(null, stack) == 0L;
    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> FILL_CONVERT_ENERGY_SLOT_CAN_INSERT = (stack, automationType) -> {
        if (EnergyInventorySlot.fillInsertCheck(stack)) {
            return true;
        }
        //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
        // Unlike with the chemical conversions, we don't check if the type is "valid" as we only have one "type" of energy.
        return EnergyInventorySlot.getPotentialConversion(null, stack) > 0L;
    };
    //Note: we mark all energy handler items as valid and have a more restrictive insert check so that we allow full containers when they are done being filled
    // We also allow energy conversion of items that can be converted
    private static final Predicate<ItemStack> FILL_CONVERT_ENERGY_SLOT_VALIDATOR = stack -> EnergyCompatUtils.hasStrictEnergyHandler(stack) || EnergyInventorySlot.getPotentialConversion(null, stack) > 0L;
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> FILL_CONVERT_ENERGY_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, FILL_CONVERT_ENERGY_SLOT_CAN_EXTRACT, FILL_CONVERT_ENERGY_SLOT_CAN_INSERT, FILL_CONVERT_ENERGY_SLOT_VALIDATOR);

    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> DRAIN_ENERGY_SLOT_CAN_EXTRACT = (stack, automationType) -> {
        if (automationType == AutomationType.MANUAL) {
            return true;
        }
        //Inversion of the insert check
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
        return itemEnergyHandler == null || itemEnergyHandler.insertEnergy(Long.MAX_VALUE, Action.SIMULATE) == Long.MAX_VALUE;
    };
    private static final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> DRAIN_ENERGY_SLOT_CAN_INSERT = (stack, automationType) -> {
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
        //if we can accept any energy that is currently stored in the container, then we allow inserting the item
        return itemEnergyHandler != null && itemEnergyHandler.insertEnergy(Long.MAX_VALUE, Action.SIMULATE) < Long.MAX_VALUE;
    };
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> DRAIN_ENERGY_SLOT_CREATOR = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo,
          containerIndex, DRAIN_ENERGY_SLOT_CAN_EXTRACT, DRAIN_ENERGY_SLOT_CAN_INSERT, EnergyInventorySlot.DRAIN_VALIDATOR);

    public static ItemSlotsBuilder builder() {
        return new ItemSlotsBuilder();
    }

    private final List<IBasicContainerCreator<? extends ComponentBackedInventorySlot>> slotCreators = new ArrayList<>();

    private ItemSlotsBuilder() {
    }

    public BaseContainerCreator<AttachedItems, ComponentBackedInventorySlot> build() {
        return new BaseInventorySlotCreator(slotCreators);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemStack> recipeInputPredicate) {
        return addBasicFactorySlots(process, recipeInputPredicate, false);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemStack> recipeInputPredicate, boolean secondaryOutput) {
        IBasicContainerCreator<ComponentBackedInventorySlot> inputSlotCreator = (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), recipeInputPredicate);
        for (int i = 0; i < process; i++) {
            //Note: We can just get away with using a simple input instead of a factory input slot and skip checking insert based on producing output
            addSlot(inputSlotCreator)
                  .addOutput();
            if (secondaryOutput) {
                addOutput();
            }
        }
        return this;
    }

    public ItemSlotsBuilder addSlots(int count, IBasicContainerCreator<? extends ComponentBackedInventorySlot> creator) {
        for (int i = 0; i < count; i++) {
            addSlot(creator);
        }
        return this;
    }

    public ItemSlotsBuilder addQIODriveSlots(int count) {
        return addSlots(count, QIO_DRIVE_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addQIODashboardSlots() {
        for (byte window = 0; window < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS; window++) {
            addSlots(9, QIO_DASHBOARD_INPUT_SLOT_CREATOR);
            addSlot(QIO_DASHBOARD_OUTPUT_SLOT_CREATOR);
        }
        return this;
    }

    public ItemSlotsBuilder addMinerSlots(int count) {
        return addSlots(count, (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              //Allow extraction if it is manual or for internal usage, or if it is not a replace stack
              //Note: We don't currently use internal for extraction anywhere here as we just shrink replace stacks directly
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || !TileEntityDigitalMiner.isSavedReplaceTarget(attachedTo, stack.getItem()),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || TileEntityDigitalMiner.isSavedReplaceTarget(attachedTo, stack.getItem()),
              ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addFormulaSlot() {
        return addSlot(FORMULA_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addFormulaCraftingSlot(int count) {
        return addSlots(count, (type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, ConstantPredicates.alwaysTrueBi(),
              (stack, automationType) -> automationType == AutomationType.INTERNAL || !attachedTo.getOrDefault(MekanismDataComponents.AUTO, false), ConstantPredicates.alwaysFalse()));
    }

    public ItemSlotsBuilder addLockSlot() {
        return addSlot(SECURITY_LOCK_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addUnlockSlot() {
        return addSlot((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, SECURITY_LOCK_CAN_INSERT, (stack, automationType) -> {
            UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
            return ownerUUID != null && ownerUUID.equals(IItemSecurityUtils.INSTANCE.getOwnerUUID(attachedTo));
        }, SecurityInventorySlot.VALIDATOR));
    }

    public ItemSlotsBuilder addSlot(IBasicContainerCreator<? extends ComponentBackedInventorySlot> slot) {
        slotCreators.add(slot);
        return this;
    }

    public ItemSlotsBuilder addFuelSlot() {
        return addSlot(FUEL_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addOredictionificatorInput() {
        return addSlot((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(),
              stack -> TileEntityOredictionificator.hasResult(attachedTo.getOrDefault(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY).getEnabled(OredictionificatorItemFilter.class), stack)));
    }

    public ItemSlotsBuilder addOutput() {
        return addSlot(OUTPUT_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addOutput(int count) {
        return addSlots(count, OUTPUT_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addBasic(int count) {
        return addSlots(count, BASIC_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addInput(int count) {
        return addSlots(count, BASIC_INPUT_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addInput(Predicate<@NotNull ItemStack> isItemValid) {
        return addSlot((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), isItemValid));
    }

    public <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> ItemSlotsBuilder addInput(
          IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> recipeType, ContainsRecipe<INPUT_CACHE, ItemStack> containsRecipe) {
        return addInput(stack -> containsRecipe.check(recipeType.getInputCache(), null, stack));
    }

    public ItemSlotsBuilder addEnergy() {
        return addSlot(FILL_CONVERT_ENERGY_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addDrainEnergy() {
        return addSlot(DRAIN_ENERGY_SLOT_CREATOR);
    }

    private boolean canFluidFill(ItemStack attachedTo, int tankIndex, ItemStack stack) {
        //Copy of FluidInventorySlot#getFillPredicate
        IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
        if (fluidHandlerItem != null) {
            IExtendedFluidTank fluidTank = ContainerType.FLUID.createContainer(attachedTo, tankIndex);
            for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                if (!fluidInTank.isEmpty() && fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                    //True if we can fill the tank with any of our contents
                    // Note: We need to recheck the fact the fluid is not empty and that it is valid,
                    // in case the item has multiple tanks and only some of the fluids are valid
                    return true;
                }
            }
        }
        return false;
    }

    public ItemSlotsBuilder addFluidFillSlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, ConstantPredicates.manualOnly(),
              (stack, automationType) -> canFluidFill(attachedTo, tankIndex, stack), ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addFluidDrainSlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, ConstantPredicates.manualOnly(), (stack, automationType) -> {
            //Copy of FluidInventorySlot's drain insert predicate
            IFluidHandlerItem itemFluidHandler = FluidInventorySlot.tryGetFluidHandlerUnstacked(stack);
            if (itemFluidHandler != null) {
                //Note: We don't need to create a fake tank using the container type, as we only care about the stored type
                AttachedFluids attachedFluids = attachedTo.getOrDefault(MekanismDataComponents.ATTACHED_FLUIDS, AttachedFluids.EMPTY);
                FluidStack fluidInTank = attachedFluids.getOrDefault(tankIndex);
                //True if the tanks contents are valid, and we can fill the item with any of the contents
                if (fluidInTank.isEmpty()) {
                    return FluidInventorySlot.isNonFullFluidContainer(itemFluidHandler);
                }
                return itemFluidHandler.fill(fluidInTank.copy(), FluidAction.SIMULATE) > 0;
            }
            return false;
        }, ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addFluidInputSlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, ConstantPredicates.manualOnly(), (stack, automationType) -> {
            //Copy of FluidInventorySlot#getInputPredicate
            IFluidHandlerItem fluidHandlerItem = FluidInventorySlot.tryGetFluidHandlerUnstacked(stack);
            if (fluidHandlerItem != null) {
                IExtendedFluidTank fluidTank = ContainerType.FLUID.createContainer(attachedTo, tankIndex);
                boolean hasEmpty = false;
                for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (fluidInTank.isEmpty()) {
                        hasEmpty = true;
                    } else if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                        //True if the items contents are valid, and we can fill the tank with any of our contents
                        return true;
                    }
                }
                //If we have no valid fluids/can't fill the tank with it
                if (fluidTank.isEmpty()) {
                    //we return if there is at least one empty tank in the item so that we can then drain into it
                    return hasEmpty;
                }
                FluidStack fluid = fluidTank.getFluid();
                if (fluid.getAmount() < FluidType.BUCKET_VOLUME) {
                    //Workaround for buckets not being able to be filled until we have enough of our volume
                    fluid = fluid.copyWithAmount(FluidType.BUCKET_VOLUME);
                } else {
                    fluid = fluid.copy();//avoid handler modifying
                }
                return fluidHandlerItem.fill(fluid, FluidAction.SIMULATE) > 0;
            }
            return false;
        }, ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addFluidRotarySlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, ConstantPredicates.manualOnly(), (stack, automationType) -> {
            //Copy of FluidInventorySlot's rotary insert predicate
            IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
            if (fluidHandlerItem != null) {
                boolean mode = attachedTo.getOrDefault(MekanismDataComponents.ROTARY_MODE, false);
                //Mode == true if fluid to chemical
                boolean allEmpty = true;
                IExtendedFluidTank fluidTank = null;
                for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (!fluidInTank.isEmpty()) {
                        if (fluidTank == null) {
                            //Lazily initialize the tank
                            fluidTank = ContainerType.FLUID.createContainer(attachedTo, tankIndex);
                        }
                        if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                            //True if we are the input tank and the items contents are valid and can fill the tank with any of our contents
                            return mode;
                        }
                        allEmpty = false;
                    }
                }
                //We want to try and drain the tank AND we are not the input tank
                return allEmpty && !mode;
            }
            return false;
        }, ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addFluidFuelSlot(int tankIndex, Predicate<@NotNull ItemStack> hasFuelValue) {
        //Copy of FluidFuelInventorySlot's forFuel insert and extract predicates
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex, (stack, automationType) -> {
            IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
            if (fluidHandlerItem != null) {
                int tanks = fluidHandlerItem.getTanks();
                if (tanks > 0) {
                    IExtendedFluidTank fluidTank = ContainerType.FLUID.createContainer(attachedTo, tankIndex);
                    for (int tank = 0; tank < tanks; tank++) {
                        if (fluidTank.isFluidValid(fluidHandlerItem.getFluidInTank(tank))) {
                            //False if the items contents are still valid
                            return false;
                        }
                    }
                }
                //Only allow extraction if our item is out of fluid, but also verify there is no conversion for it
            }
            //Always allow extraction if something went horribly wrong, and we are not a fluid item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            return !hasFuelValue.test(stack);
        }, (stack, automationType) -> hasFuelValue.test(stack) || canFluidFill(attachedTo, tankIndex, stack), ConstantPredicates.alwaysTrue())));
    }

    private boolean canChemicalDrainInsert(ItemStack attachedTo, int tankIndex, ItemStack stack) {
        //Copy of logic from ChemicalInventorySlot#getDrainInsertPredicate
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        if (handler != null) {
            //Note: We don't need to create a fake tank using the container type, as we only care about the stored type
            AttachedChemicals containers = ContainerType.CHEMICAL.getOrEmpty(attachedTo);
            ChemicalStack chemicalInTank = containers.getOrDefault(tankIndex);
            if (chemicalInTank.isEmpty()) {
                //If the chemical tank is empty, accept the chemical item as long as it is not full
                for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                    if (handler.getChemicalInTank(tank).getAmount() < handler.getChemicalTankCapacity(tank)) {
                        //True if we have any space in this tank
                        return true;
                    }
                }
                return false;
            }
            //Otherwise, if we can accept any of the chemical that is currently stored in the tank, then we allow inserting the item
            return handler.insertChemical(chemicalInTank, Action.SIMULATE).getAmount() < chemicalInTank.getAmount();
        }
        return false;
    }

    private boolean canChemicalFillExtract(ItemStack attachedTo, int tankIndex, ItemStack stack) {
        //Copy of logic from ChemicalInventorySlot#getFillExtractPredicate
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        if (handler != null) {
            IChemicalTank chemicalTank = null;
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                ChemicalStack storedChemical = handler.getChemicalInTank(tank);
                if (!storedChemical.isEmpty()) {
                    if (chemicalTank == null) {
                        chemicalTank = ContainerType.CHEMICAL.createContainer(attachedTo, tankIndex);
                    }
                    if (chemicalTank.isValid(storedChemical)) {
                        //False if the item isn't empty and the contents are still valid
                        return false;
                    }
                }
            }
            //If we have no contents that are still valid, allow extraction
        }
        //Always allow it if we are not a chemical item (For example this may be true for hybrid inventory slots)
        return true;
    }

    private boolean canChemicalFillInsert(ItemStack attachedTo, int tankIndex, ItemStack stack) {
        //Copy of logic from ChemicalInventorySlot#fillInsertCheck
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        if (handler != null) {
            IChemicalTank chemicalTank = null;
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                ChemicalStack chemicalInTank = handler.getChemicalInTank(tank);
                if (!chemicalInTank.isEmpty()) {
                    if (chemicalTank == null) {
                        chemicalTank = ContainerType.CHEMICAL.createContainer(attachedTo, tankIndex);
                    }
                    if (chemicalTank.insert(chemicalInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < chemicalInTank.getAmount()) {
                        //True if we can fill the tank with any of our contents
                        // Note: We need to recheck the fact the chemical is not empty in case the item has multiple tanks and only some of the chemicals are valid
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canChemicalFillOrConvertExtract(ItemStack attachedTo, int tankIndex, ItemStack stack) {
        //Copy of logic from ChemicalInventorySlot#getFillOrConvertExtractPredicate
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        IChemicalTank chemicalTank = null;
        if (handler != null) {
            int tanks = handler.getChemicalTanks();
            if (tanks > 0) {
                chemicalTank = ContainerType.CHEMICAL.createContainer(attachedTo, tankIndex);
                for (int tank = 0; tank < tanks; tank++) {
                    if (chemicalTank.isValid(handler.getChemicalInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
            }
            //Only allow extraction if our item is out of chemical, and doesn't have a valid conversion for it
        }
        //Always allow extraction if something went horribly wrong, and we are not a chemical item AND we can't provide a valid type of chemical
        // This might happen after a reload for example
        ChemicalStack conversion = ChemicalInventorySlot.getPotentialConversion(null, stack);
        if (conversion.isEmpty()) {
            return true;
        } else if (chemicalTank == null) {
            //If we haven't resolved the tank yet, we need to do it now
            chemicalTank = ContainerType.CHEMICAL.createContainer(attachedTo, tankIndex);
        }
        return !chemicalTank.isValid(conversion);
    }

    private boolean canChemicalFillOrConvertInsert(ItemStack attachedTo, int tankIndex, ItemStack stack) {
        //Copy of logic from ChemicalInventorySlot#getFillOrConvertInsertPredicate
        IChemicalTank chemicalTank = null;
        {//Fill insert check logic, we want to avoid resolving the tank as long as possible
            IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
            if (handler != null) {
                for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                    ChemicalStack chemicalInTank = handler.getChemicalInTank(tank);
                    if (!chemicalInTank.isEmpty()) {
                        if (chemicalTank == null) {
                            chemicalTank = ContainerType.CHEMICAL.createContainer(attachedTo, tankIndex);
                        }
                        if (chemicalTank.insert(chemicalInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < chemicalInTank.getAmount()) {
                            //True if we can fill the tank with any of our contents
                            // Note: We need to recheck the fact the chemical is not empty in case the item has multiple tanks and only some of the chemicals are valid
                            return true;
                        }
                    }
                }
            }
        }
        ChemicalStack conversion = ChemicalInventorySlot.getPotentialConversion(null, stack);
        //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
        if (conversion.isEmpty()) {
            return false;
        } else if (chemicalTank == null) {
            //If we haven't resolved the tank yet, we need to do it now
            chemicalTank = ContainerType.CHEMICAL.createContainer(attachedTo, tankIndex);
        }
        if (chemicalTank.insert(conversion, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < conversion.getAmount()) {
            //If we can insert the converted substance into the tank allow insertion
            return true;
        }
        //If we can't because the tank is full, we do a slightly less accurate check and validate that the type matches the stored type
        // and that it is still actually valid for the tank, as a reload could theoretically make it no longer be valid while there is still some stored
        return chemicalTank.getNeeded() == 0 && chemicalTank.isTypeEqual(conversion) && chemicalTank.isValid(conversion);
    }

    public ItemSlotsBuilder addChemicalFillSlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              (stack, automationType) -> automationType == AutomationType.MANUAL || canChemicalFillExtract(attachedTo, tankIndex, stack),
              (stack, automationType) -> canChemicalFillInsert(attachedTo, tankIndex, stack), ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addChemicalFillOrConvertSlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              (stack, automationType) -> automationType == AutomationType.MANUAL || canChemicalFillOrConvertExtract(attachedTo, tankIndex, stack),
              (stack, automationType) -> canChemicalFillOrConvertInsert(attachedTo, tankIndex, stack), ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addChemicalDrainSlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              (stack, automationType) -> automationType == AutomationType.MANUAL || !canChemicalDrainInsert(attachedTo, tankIndex, stack),
              (stack, automationType) -> canChemicalDrainInsert(attachedTo, tankIndex, stack), ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addChemicalRotaryDrainSlot(int tankIndex) {
        //Copy of logic from ChemicalInventorySlot#rotaryDrain
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              (stack, automationType) -> {
                  if (automationType == AutomationType.MANUAL) {
                      return true;
                  }
                  //Copy of the insert check but inverted
                  return !attachedTo.getOrDefault(MekanismDataComponents.ROTARY_MODE, false) ||
                         !canChemicalDrainInsert(attachedTo, tankIndex, stack);
              },
              (stack, automationType) -> attachedTo.getOrDefault(MekanismDataComponents.ROTARY_MODE, false) &&
                                         canChemicalDrainInsert(attachedTo, tankIndex, stack),
              ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addChemicalRotaryFillSlot(int tankIndex) {
        //Copy of logic from ChemicalInventorySlot#rotaryFill
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              (stack, automationType) -> automationType == AutomationType.MANUAL || canChemicalFillExtract(attachedTo, tankIndex, stack),
              (stack, automationType) -> !attachedTo.getOrDefault(MekanismDataComponents.ROTARY_MODE, false) &&
                                         canChemicalFillInsert(attachedTo, tankIndex, stack),
              ConstantPredicates.alwaysTrue())));
    }

    public ItemSlotsBuilder addInfusionFillOrConvertSlot(int tankIndex) {
        return addSlot(((type, attachedTo, containerIndex) -> new ComponentBackedInventorySlot(attachedTo, containerIndex,
              (stack, automationType) -> automationType == AutomationType.MANUAL || canChemicalFillOrConvertExtract(attachedTo, tankIndex, stack),
              (stack, automationType) -> canChemicalFillOrConvertInsert(attachedTo, tankIndex, stack), ConstantPredicates.alwaysTrue())));
    }

    private static class BaseInventorySlotCreator extends BaseContainerCreator<AttachedItems, ComponentBackedInventorySlot> {

        public BaseInventorySlotCreator(List<IBasicContainerCreator<? extends ComponentBackedInventorySlot>> creators) {
            super(creators);
        }

        @Override
        public AttachedItems initStorage(int containers) {
            return AttachedItems.create(containers);
        }
    }
}