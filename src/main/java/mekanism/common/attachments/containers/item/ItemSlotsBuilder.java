package mekanism.common.attachments.containers.item;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.ContainsRecipe;
import mekanism.common.attachments.containers.ResourceContainersBuilder.BaseContainerBuilder;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
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
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

//TODO - 26.1: Do we want this to extend ResourceContainersBuilder
public class ItemSlotsBuilder {

    //Note: For a lot of slots with specific helper methods we can simply use a ComponentBackedInventorySlot as we don't have any overrides or desire to call those methods while on an itemstack
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> BASIC_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> BASIC_INPUT_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> OUTPUT_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue());

    //Copy of predicates from FuelInventorySlot
    //TODO - 26.1: this now needs world access. Does it really matter as it's only used on the Fuelwood heater's item inv, which we don't expose?
    /*private static final BiPredicate<ItemResource, AutomationType> FUEL_CAN_EXTRACT = (itemType, automationType) -> automationType.isManual() || itemType.toStack().getBurnTime(null) == 0;
    private static final BiPredicate<ItemResource, AutomationType> FUEL_CAN_INSERT = (itemType, automationType) -> itemType.toStack().getBurnTime(null) != 0;
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> FUEL_SLOT_CREATOR = (_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess,
          containerIndex, FUEL_CAN_EXTRACT, FUEL_CAN_INSERT, ConstantPredicates.alwaysTrue());*/

    //Security Inventory Slot
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> SECURITY_LOCK_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, SecurityInventorySlot.LOCK_EXTRACT_PREDICATE, SecurityInventorySlot.LOCK_INSERT_PREDICATE, SecurityInventorySlot.VALIDATOR);

    //FormulaInventorySlot
    //Note: We skip making the extra checks based on the formula and just allow all items
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> FORMULA_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), TileEntityFormulaicAssemblicator.FORMULA_SLOT_VALIDATOR);

    //QIO drive slot
    //Note: As we don't have to update the presence of a drive or remove it from the frequency we can make do with just using a basic slot
    //TODO - 1.20.4: Evaluate if copy the notExternal is correct or do we want this to have some other checks
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> QIO_DRIVE_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.notExternal(), QIODriveSlot.IS_QIO_ITEM);

    //QIO Dashboard Crafting WINDOW
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> QIO_DASHBOARD_INPUT_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    //Note: We don't allow external means to modify this slot as it truthfully only exists to make logic easier
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> QIO_DASHBOARD_OUTPUT_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.internalOnly(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue());

    //EnergyInventorySlot
    //Note: As energy is untyped we don't have to do extra checks about what is currently stored or not on the attached stack
    private static final BiPredicate<ItemResource, AutomationType> FILL_CONVERT_ENERGY_SLOT_CAN_EXTRACT = (itemType, automationType) ->
          //Allow extraction if something went horribly wrong, and we are not an energy container item or no longer have any energy left to give,
          // or we are no longer a valid conversion, this might happen after a reload for example
          automationType.isManual() || !EnergyInventorySlot.fillInsertCheck(itemType) && EnergyInventorySlot.getPotentialConversion(null, itemType) == null;
    private static final BiPredicate<ItemResource, AutomationType> FILL_CONVERT_ENERGY_SLOT_CAN_INSERT = (itemType, _) -> {
        if (EnergyInventorySlot.fillInsertCheck(itemType)) {
            return true;
        }
        //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
        // Unlike with the chemical conversions, we don't check if the type is "valid" as we only have one "type" of energy.
        return EnergyInventorySlot.getPotentialConversion(null, itemType) != null;
    };
    //Note: we mark all energy handler items as valid and have a more restrictive insert check so that we allow full containers when they are done being filled
    // We also allow energy conversion of items that can be converted
    private static final Predicate<ItemResource> FILL_CONVERT_ENERGY_SLOT_VALIDATOR = itemType -> EnergyCompatUtils.getStrictEnergyHandler(ItemAccessUtils.queryOnlyAccess(itemType)) != null || EnergyInventorySlot.getPotentialConversion(null, itemType) != null;
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> FILL_CONVERT_ENERGY_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, FILL_CONVERT_ENERGY_SLOT_CAN_EXTRACT, FILL_CONVERT_ENERGY_SLOT_CAN_INSERT, FILL_CONVERT_ENERGY_SLOT_VALIDATOR);

    private static final BiPredicate<ItemResource, AutomationType> DRAIN_ENERGY_SLOT_CAN_EXTRACT = (itemType, automationType) -> {
        if (automationType.isManual()) {
            return true;
        }
        //Inversion of the insert check
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(ItemAccessUtils.queryOnlyAccess(itemType));
        if (itemEnergyHandler == null) {
            return true;
        }
        try (Transaction simulation = Transaction.openRoot()) {//TODO - 26.1: Is there a concern we are already in a transactional context?
            return itemEnergyHandler.insert(Long.MAX_VALUE, simulation) == 0;
        }
    };
    private static final BiPredicate<ItemResource, AutomationType> DRAIN_ENERGY_SLOT_CAN_INSERT = (itemType, _) -> {
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(ItemAccessUtils.queryOnlyAccess(itemType));
        //if we can accept any energy that is currently stored in the container, then we allow inserting the item
        if (itemEnergyHandler == null) {
            return false;
        }
        try (Transaction simulation = Transaction.openRoot()) {//TODO - 26.1: Is there a concern we are already in a transactional context?
            return itemEnergyHandler.insert(Long.MAX_VALUE, simulation) > 0;
        }
    };
    private static final IBasicContainerCreator<ComponentBackedInventorySlot> DRAIN_ENERGY_SLOT_CREATOR = (_, attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, DRAIN_ENERGY_SLOT_CAN_EXTRACT, DRAIN_ENERGY_SLOT_CAN_INSERT, EnergyInventorySlot.HAS_ENERGY_HANDLER);

    public static ItemSlotsBuilder builder() {
        return new ItemSlotsBuilder();
    }

    private final List<IBasicContainerCreator<? extends ComponentBackedInventorySlot>> slotCreators = new ArrayList<>();

    private ItemSlotsBuilder() {
    }

    public BaseContainerCreator<AttachedResources<ItemResource>, ComponentBackedInventorySlot> build() {
        return new BaseContainerBuilder<>(slotCreators, LargeResourceStack.ITEM_HELPER);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemResource> recipeInputPredicate) {
        return addBasicFactorySlots(process, recipeInputPredicate, false);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemResource> recipeInputPredicate, boolean secondaryOutput) {
        IBasicContainerCreator<ComponentBackedInventorySlot> inputSlotCreator = (_, attachedAccess, containerIndex) ->
              new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), recipeInputPredicate);
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
        return addSlots(count, (_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
              //Allow extraction if it is manual or for internal usage, or if it is not a replace stack
              //Note: We don't currently use internal for extraction anywhere here as we just shrink replace stacks directly
              (itemType, automationType) -> !automationType.isExternal() || !TileEntityDigitalMiner.isSavedReplaceTarget(attachedAccess, itemType),
              (itemType, automationType) -> !automationType.isExternal() || TileEntityDigitalMiner.isSavedReplaceTarget(attachedAccess, itemType),
              ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addFormulaSlot() {
        return addSlot(FORMULA_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addFormulaCraftingSlot(int count) {
        return addSlots(count, (_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(),
              (_, automationType) -> automationType.isInternal() || !attachedAccess.getResource().getOrDefault(MekanismDataComponents.AUTO, false), ConstantPredicates.alwaysFalse()));
    }

    public ItemSlotsBuilder addLockSlot() {
        return addSlot(SECURITY_LOCK_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addUnlockSlot() {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess,
              containerIndex, SecurityInventorySlot.UNLOCK_EXTRACT_PREDICATE, (itemType, automationType) ->
              SecurityInventorySlot.canInsertUnlock(itemType, automationType, () -> IItemSecurityUtils.INSTANCE.getOwnerUUID(attachedAccess)),
              SecurityInventorySlot.VALIDATOR));
    }

    public ItemSlotsBuilder addSlot(IBasicContainerCreator<? extends ComponentBackedInventorySlot> slot) {
        slotCreators.add(slot);
        return this;
    }

    /*public ItemSlotsBuilder addFuelSlot() {
        return addSlot(FUEL_SLOT_CREATOR);
    }*/

    public ItemSlotsBuilder addOredictionificatorInput() {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(),
              itemType -> TileEntityOredictionificator.hasResult(attachedAccess.getResource().getOrDefault(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY).getEnabled(OredictionificatorItemFilter.class), itemType)));
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

    public ItemSlotsBuilder addInput(Predicate<ItemResource> isItemValid) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), isItemValid));
    }

    public <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache> ItemSlotsBuilder addInput(
          IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> recipeType, ContainsRecipe<INPUT_CACHE, ItemResource> containsRecipe) {
        return addInput(itemType -> containsRecipe.check(recipeType.getInputCache(), null, itemType));
    }

    public ItemSlotsBuilder addEnergy() {
        return addSlot(FILL_CONVERT_ENERGY_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addDrainEnergy() {
        return addSlot(DRAIN_ENERGY_SLOT_CREATOR);
    }

    private boolean canFluidFill(ItemAccess attachedAccess, int tankIndex, ItemResource itemType) {
        IFluidTank fluidTank = ContainerType.FLUID.createContainer(attachedAccess, tankIndex);
        //TODO - 26.1: Figure out item access
        return FluidInventorySlot.canFill(fluidTank, ItemAccessUtils.queryOnlyAccess(itemType));
    }

    public ItemSlotsBuilder addFluidFillSlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.manualOnly(),
              (itemType, _) -> canFluidFill(attachedAccess, tankIndex, itemType), ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addFluidDrainSlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.manualOnly(), (itemType, _) -> {
            //Copy of FluidInventorySlot's drain insert predicate
            //TODO - 26.1: Figure out fluid handlers, this used to be a one by one
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
            if (fluidHandler != null) {
                //Note: We don't need to create a fake tank using the container type, as we only care about the stored type
                AttachedResources<FluidResource> attachedFluids = ContainerType.FLUID.getOrEmpty(attachedAccess);
                LargeResourceStack<FluidResource> fluidInTank = attachedFluids.getOrNull(tankIndex);
                //True if the tanks contents are valid, and we can fill the item with any of the contents
                if (fluidInTank == null || fluidInTank.isEmpty()) {
                    return FluidInventorySlot.isNonFullFluidContainer(fluidHandler);
                }
                //TODO - 26.1: Are our insert predicates and stuff ever ran from within a transactional context?
                // If so we might need to pass Transaction#getCurrentOpenedTransaction to it
                try (Transaction simulation = Transaction.openRoot()) {
                    //TODO - 26.1: Do we need to do similar to the canInput that checks for bucket volume?
                    return fluidHandler.insert(fluidInTank.resource(), Ints.saturatedCast(fluidInTank.amount()), simulation) > 0;
                }
            }
            return false;
        }, ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addFluidInputSlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.manualOnly(), (itemType, _) -> {
            //Copy of FluidInventorySlot#getInputPredicate
            //TODO - 26.1: Figure out fluid handlers, this used to be a one by one
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
            if (fluidHandler != null) {
                IFluidTank fluidTank = ContainerType.FLUID.createContainer(attachedAccess, tankIndex);
                return FluidInventorySlot.canInput(fluidHandler, fluidTank);
            }
            return false;
        }, ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addFluidRotarySlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.manualOnly(), (itemType, _) -> {
            //Copy of FluidInventorySlot's rotary insert predicate
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
            if (fluidHandler != null) {
                boolean mode = attachedAccess.getResource().getOrDefault(MekanismDataComponents.ROTARY_MODE, false);
                //Mode == true if fluid to chemical
                boolean allEmpty = true;
                IFluidTank fluidTank = null;
                for (int tank = 0, tanks = fluidHandler.size(); tank < tanks; tank++) {
                    FluidResource fluidInTank = fluidHandler.getResource(tank);
                    if (!fluidInTank.isEmpty()) {
                        if (fluidTank == null) {
                            //Lazily initialize the tank
                            fluidTank = ContainerType.FLUID.createContainer(attachedAccess, tankIndex);
                        }
                        //TODO - 26.1: Are call sites ever in a transactional context?
                        try (Transaction simulation = Transaction.openRoot()) {
                            if (fluidTank.insert(fluidInTank, fluidHandler.getAmountAsInt(tank), simulation, AutomationType.INTERNAL) > 0) {
                                //True if we are the input tank and the items contents are valid and can fill the tank with any of our contents
                                return mode;
                            }
                        }
                        allEmpty = false;
                    }
                }
                //We want to try and drain the tank AND we are not the input tank
                return allEmpty && !mode;
            }
            return false;
        }, ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addFluidFuelSlot(int tankIndex, Predicate<ItemResource> hasFuelValue) {
        //Copy of FluidFuelInventorySlot's forFuel insert and extract predicates
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, _) -> {
            ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
            if (handler != null) {
                int tanks = handler.size();
                if (tanks > 0) {
                    IFluidTank fluidTank = ContainerType.FLUID.createContainer(attachedAccess, tankIndex);
                    for (int tank = 0; tank < tanks; tank++) {
                        FluidResource fluidType = handler.getResource(tank);
                        if (!fluidType.isEmpty() && fluidTank.isValid(fluidType)) {
                            //False if the items contents are still valid
                            return false;
                        }
                    }
                }
                //Only allow extraction if our item is out of fluid, but also verify there is no conversion for it
            }
            //Always allow extraction if something went horribly wrong, and we are not a fluid item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            return !hasFuelValue.test(itemType);
        }, (itemType, _) -> hasFuelValue.test(itemType) || canFluidFill(attachedAccess, tankIndex, itemType), ConstantPredicates.alwaysTrue()));
    }

    private boolean canChemicalDrainInsert(ItemAccess attachedAccess, int tankIndex, ItemResource itemType) {
        //Copy of logic from ChemicalInventorySlot#getDrainInsertPredicate
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (handler != null) {
            IChemicalTank tank = ContainerType.CHEMICAL.createContainer(attachedAccess, tankIndex);
            return ChemicalInventorySlot.canDrainInsert(tank, handler);
        }
        return false;
    }

    private boolean canChemicalFillExtract(ItemAccess attachedAccess, int tankIndex, ItemResource itemType) {
        //Copy of logic from ChemicalInventorySlot#getFillExtractPredicate
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (handler == null) {
            return true;
        }
        IChemicalTank tank = ContainerType.CHEMICAL.createContainer(attachedAccess, tankIndex);
        return ChemicalInventorySlot.fillExtractCheck(tank, handler);
    }

    private boolean canChemicalFillInsert(ItemAccess attachedAccess, int tankIndex, ItemResource itemType) {
        //Copy of logic from ChemicalInventorySlot#fillInsertCheck
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (handler != null) {
            IChemicalTank chemicalTank = ContainerType.CHEMICAL.createContainer(attachedAccess, tankIndex);
            return ChemicalInventorySlot.fillInsertCheck(chemicalTank, handler);
        }
        return false;
    }

    private boolean canChemicalFillOrConvertExtract(ItemAccess attachedAccess, int tankIndex, ItemResource itemType) {
        //Copy of logic from ChemicalInventorySlot#getFillOrConvertExtractPredicate
        //Note: We eagerly resolve the chemical tank as it makes things easier, as the only case where we would not need it is:
        // no handler on the item, AND no conversion recipe
        //TODO: If it turns out to be an issue, we can make the method we call lazily initialize the chemical tank
        IChemicalTank chemicalTank = ContainerType.CHEMICAL.createContainer(attachedAccess, tankIndex);
        return ChemicalInventorySlot.fillOrConvertExtractCheck(chemicalTank, () -> null, itemType);
    }

    private boolean canChemicalFillOrConvertInsert(ItemAccess attachedAccess, int tankIndex, ItemResource itemType) {
        //Copy of logic from ChemicalInventorySlot#getFillOrConvertInsertPredicate
        //Note: We eagerly resolve the chemical tank as it makes things easier, as the only case where we would not need it is:
        // no handler on the item, AND no conversion recipe
        //TODO: If it turns out to be an issue, we can make the method we call lazily initialize the chemical tank
        IChemicalTank chemicalTank = ContainerType.CHEMICAL.createContainer(attachedAccess, tankIndex);
        return ChemicalInventorySlot.fillOrConvertInsertCheck(chemicalTank, () -> null, itemType);
    }

    public ItemSlotsBuilder addChemicalFillSlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
              (itemType, automationType) -> automationType.isManual() || canChemicalFillExtract(attachedAccess, tankIndex, itemType),
              (itemType, _) -> canChemicalFillInsert(attachedAccess, tankIndex, itemType), ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addChemicalFillOrConvertSlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
              (itemType, automationType) -> automationType.isManual() || canChemicalFillOrConvertExtract(attachedAccess, tankIndex, itemType),
              (itemType, _) -> canChemicalFillOrConvertInsert(attachedAccess, tankIndex, itemType), ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addChemicalDrainSlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
              (itemType, automationType) -> automationType.isManual() || !canChemicalDrainInsert(attachedAccess, tankIndex, itemType),
              (itemType, _) -> canChemicalDrainInsert(attachedAccess, tankIndex, itemType), ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addChemicalRotaryDrainSlot(int tankIndex) {
        //Copy of logic from ChemicalInventorySlot#rotaryDrain
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
              (itemType, automationType) -> {
                  if (automationType.isManual()) {
                      return true;
                  }
                  //Copy of the insert check but inverted
                  return !attachedAccess.getResource().getOrDefault(MekanismDataComponents.ROTARY_MODE, false) || !canChemicalDrainInsert(attachedAccess, tankIndex, itemType);
              },
              (itemType, _) -> attachedAccess.getResource().getOrDefault(MekanismDataComponents.ROTARY_MODE, false) &&
                               canChemicalDrainInsert(attachedAccess, tankIndex, itemType),
              ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addChemicalRotaryFillSlot(int tankIndex) {
        //Copy of logic from ChemicalInventorySlot#rotaryFill
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
              (itemType, automationType) -> automationType.isManual() || canChemicalFillExtract(attachedAccess, tankIndex, itemType),
              (itemType, _) -> !attachedAccess.getResource().getOrDefault(MekanismDataComponents.ROTARY_MODE, false) &&
                               canChemicalFillInsert(attachedAccess, tankIndex, itemType),
              ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addInfusionFillOrConvertSlot(int tankIndex) {
        return addSlot((_, attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
              (itemType, automationType) -> automationType.isManual() || canChemicalFillOrConvertExtract(attachedAccess, tankIndex, itemType),
              (itemType, _) -> canChemicalFillOrConvertInsert(attachedAccess, tankIndex, itemType), ConstantPredicates.alwaysTrue()));
    }
}