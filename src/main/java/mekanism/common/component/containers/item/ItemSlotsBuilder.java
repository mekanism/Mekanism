package mekanism.common.component.containers.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.component.FilterAware;
import mekanism.common.component.containers.ContainsRecipe;
import mekanism.common.component.containers.creator.BaseContainerCreator;
import mekanism.common.component.containers.creator.IBasicContainerCreator;
import mekanism.common.component.containers.resource.AttachedResources;
import mekanism.common.component.containers.resource.ResourceContainersBuilder.BaseContainerBuilder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.ResourceContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.ChemicalInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.inventory.slot.ResourceHandlerSlot;
import mekanism.common.inventory.slot.SecurityInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

//TODO - 26.2: Do we want this to extend ResourceContainersBuilder
public class ItemSlotsBuilder {

    //Note: For a lot of slots with specific helper methods we can simply use a ComponentBackedInventorySlot as we don't have any overrides or desire to call those methods while on an itemstack
    private static final IBasicContainerCreator<IInventorySlot> BASIC_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    private static final IBasicContainerCreator<IInventorySlot> BASIC_INPUT_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    private static final IBasicContainerCreator<IInventorySlot> OUTPUT_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue());

    //Copy of predicates from FuelInventorySlot
    //TODO - 26.2: this now needs world access. Does it really matter as it's only used on the Fuelwood heater's item inv, which we don't expose?
    /*private static final BiPredicate<ItemResource, AutomationType> FUEL_CAN_EXTRACT = (itemType, automationType) -> !automationType.isExternal() || itemType.toStack().getBurnTime(null) == 0;
    private static final BiPredicate<ItemResource, AutomationType> FUEL_CAN_INSERT = (itemType, automationType) -> automationType.isInternal() || itemType.toStack().getBurnTime(null) != 0;
    private static final IBasicContainerCreator<IInventorySlot> FUEL_SLOT_CREATOR = (attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess,
          containerIndex, FUEL_CAN_EXTRACT, FUEL_CAN_INSERT, ConstantPredicates.alwaysTrue());*/

    //Security Inventory Slot
    private static final IBasicContainerCreator<IInventorySlot> SECURITY_LOCK_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, SecurityInventorySlot.LOCK_EXTRACT_PREDICATE, SecurityInventorySlot.LOCK_INSERT_PREDICATE, SecurityInventorySlot.VALIDATOR);

    //FormulaInventorySlot
    //Note: We skip making the extra checks based on the formula and just allow all items
    private static final IBasicContainerCreator<IInventorySlot> FORMULA_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), TileEntityFormulaicAssemblicator.FORMULA_SLOT_VALIDATOR);

    //QIO drive slot
    //Note: As we don't have to update the presence of a drive or remove it from the frequency we can make do with just using a basic slot
    //TODO - 1.20.4: Evaluate if copy the notExternal is correct or do we want this to have some other checks
    private static final IBasicContainerCreator<IInventorySlot> QIO_DRIVE_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.notExternal(), QIODriveSlot.IS_QIO_ITEM);

    //QIO Dashboard Crafting WINDOW
    private static final IBasicContainerCreator<IInventorySlot> QIO_DASHBOARD_INPUT_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue());
    //Note: We don't allow external means to modify this slot as it truthfully only exists to make logic easier
    private static final IBasicContainerCreator<IInventorySlot> QIO_DASHBOARD_OUTPUT_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.internalOnly(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue());

    //EnergyInventorySlot
    private static final IBasicContainerCreator<IInventorySlot> FILL_CONVERT_ENERGY_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
              if (!automationType.isExternal()) {
                  return true;
              }
              IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(ContainerType.ENERGY.getCapOrUnexposed(attachedAccess));
              return !EnergyInventorySlot.canFillOrConvert(energyContainer, BasicInventorySlot.NO_LEVEL, itemType);
          }, (itemType, automationType) -> {
              if (automationType.isInternal()) {
                  return true;
              }
              IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(ContainerType.ENERGY.getCapOrUnexposed(attachedAccess));
              return EnergyInventorySlot.canFillOrConvert(energyContainer, BasicInventorySlot.NO_LEVEL, itemType);
          }, ConstantPredicates.alwaysTrue());
    private static final IBasicContainerCreator<IInventorySlot> DRAIN_ENERGY_SLOT_CREATOR = (attachedAccess, containerIndex) ->
          new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
              if (!automationType.isExternal()) {
                  return true;
              }
              //Inversion of the insert check
              EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.sideEffectFreeAccess(itemType));
              if (energyHandler == null) {
                  return true;
              }
              EnergyHandler energyContainer = ContainerType.ENERGY.getCapOrUnexposed(attachedAccess);
              return energyContainer != null && !EnergyInventorySlot.canDrain(energyContainer, energyHandler);
          }, (itemType, automationType) -> {
              if (automationType.isInternal()) {
                  return true;
              }
              EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.sideEffectFreeAccess(itemType));
              if (energyHandler == null) {
                  return false;
              }
              EnergyHandler energyContainer = ContainerType.ENERGY.getCapOrUnexposed(attachedAccess);
              return energyContainer != null && EnergyInventorySlot.canDrain(energyContainer, energyHandler);
          }, ConstantPredicates.alwaysTrue());

    public static ItemSlotsBuilder builder() {
        return new ItemSlotsBuilder();
    }

    private final List<IBasicContainerCreator<IInventorySlot>> slotCreators = new ArrayList<>();

    private ItemSlotsBuilder() {
    }

    public BaseContainerCreator<AttachedResources<ItemResource>, IInventorySlot> build() {
        return new BaseContainerBuilder<>(slotCreators, LargeResourceStack.ITEM_HELPER);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemResource> recipeInputPredicate) {
        return addBasicFactorySlots(process, recipeInputPredicate, false);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemResource> recipeInputPredicate, boolean secondaryOutput) {
        IBasicContainerCreator<IInventorySlot> inputSlotCreator = (attachedAccess, containerIndex) ->
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

    public ItemSlotsBuilder addSlots(int count, IBasicContainerCreator<IInventorySlot> creator) {
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
        return addSlots(count, (attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex,
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
        return addSlots(count, (attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(),
              (_, automationType) -> automationType.isInternal() || !attachedAccess.getResource().getOrDefault(MekanismDataComponents.AUTO, false), ConstantPredicates.alwaysFalse()));
    }

    public ItemSlotsBuilder addLockSlot() {
        return addSlot(SECURITY_LOCK_SLOT_CREATOR);
    }

    public ItemSlotsBuilder addUnlockSlot() {
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess,
              containerIndex, SecurityInventorySlot.UNLOCK_EXTRACT_PREDICATE, (itemType, automationType) ->
              SecurityInventorySlot.canInsertUnlock(itemType, automationType, () -> IItemSecurityUtils.INSTANCE.getOwnerUUID(attachedAccess)),
              SecurityInventorySlot.VALIDATOR));
    }

    public ItemSlotsBuilder addSlot(IBasicContainerCreator<IInventorySlot> slot) {
        slotCreators.add(slot);
        return this;
    }

    /*public ItemSlotsBuilder addFuelSlot() {
        return addSlot(FUEL_SLOT_CREATOR);
    }*/

    public ItemSlotsBuilder addOredictionificatorInput() {
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(),
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
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), isItemValid));
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

    private <RESOURCE extends Resource> ItemSlotsBuilder addResourceFillSlot(ResourceContainerType<RESOURCE, ?> containerType, int tankIndex) {
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            return !ResourceHandlerSlot.canFill(attachedAccess, containerType, tankIndex, itemType);
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            return ResourceHandlerSlot.canFill(attachedAccess, containerType, tankIndex, itemType);
        }, ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addChemicalFillSlot(int tankIndex) {
        return addResourceFillSlot(ContainerType.CHEMICAL, tankIndex);
    }

    public ItemSlotsBuilder addFluidFillSlot(int tankIndex) {
        return addResourceFillSlot(ContainerType.FLUID, tankIndex);
    }

    private <RESOURCE extends Resource> ItemSlotsBuilder addResourceDrainSlot(ResourceContainerType<RESOURCE, ?> containerType, int tankIndex) {
        //Copy of logic from FluidInventorySlot#drain
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            return !ResourceHandlerSlot.canDrain(attachedAccess, containerType, tankIndex, itemType);
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            return ResourceHandlerSlot.canDrain(attachedAccess, containerType, tankIndex, itemType);
        }, ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addChemicalDrainSlot(int tankIndex) {
        return addResourceDrainSlot(ContainerType.CHEMICAL, tankIndex);
    }

    public ItemSlotsBuilder addFluidDrainSlot(int tankIndex) {
        return addResourceDrainSlot(ContainerType.FLUID, tankIndex);
    }

    public ItemSlotsBuilder addFluidInputSlot(int tankIndex) {
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            return !ResourceHandlerSlot.canInput(attachedAccess, ContainerType.FLUID, tankIndex, itemType);
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            return ResourceHandlerSlot.canInput(attachedAccess, ContainerType.FLUID, tankIndex, itemType);
        }, ConstantPredicates.alwaysTrue()));
    }

    private static boolean getRotaryMode(ItemAccess attachedAccess) {
        return attachedAccess.getResource().getOrDefault(MekanismDataComponents.ROTARY_MODE, false);
    }

    private <RESOURCE extends Resource> ItemSlotsBuilder addRotarySlot(ResourceContainerType<RESOURCE, ?> containerType, int tankIndex, boolean rotaryMode) {
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            return !ResourceHandlerSlot.canRotaryInsert(attachedAccess, containerType, tankIndex, itemType, rotaryMode == getRotaryMode(attachedAccess));
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            return ResourceHandlerSlot.canRotaryInsert(attachedAccess, containerType, tankIndex, itemType, rotaryMode == getRotaryMode(attachedAccess));
        }, ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addFluidRotarySlot(int tankIndex) {
        return addRotarySlot(ContainerType.FLUID, tankIndex, true);
    }

    public ItemSlotsBuilder addChemicalRotarySlot(int tankIndex) {
        return addRotarySlot(ContainerType.CHEMICAL, tankIndex, false);
    }

    public ItemSlotsBuilder addFluidFuelSlot(int tankIndex, Predicate<ItemResource> hasFuelValue) {
        //Copy of FluidFuelInventorySlot's forFuel insert and extract predicates
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            //Always allow extraction if something went horribly wrong, and we are not a fluid item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            return !hasFuelValue.test(itemType) && !ResourceHandlerSlot.canFill(attachedAccess, ContainerType.FLUID, tankIndex, itemType);
        }, (itemType, automationType) -> {
            if (automationType.isInternal() || hasFuelValue.test(itemType)) {
                return true;
            }
            return ResourceHandlerSlot.canFill(attachedAccess, ContainerType.FLUID, tankIndex, itemType);
        }, ConstantPredicates.alwaysTrue()));
    }

    public ItemSlotsBuilder addChemicalFillOrConvertSlot(int tankIndex) {
        return addSlot((attachedAccess, containerIndex) -> new ComponentBackedInventorySlot(attachedAccess, containerIndex, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                return true;
            }
            //Copy of logic from ChemicalInventorySlot#getFillOrConvertExtractPredicate
            //Note: We eagerly resolve the chemical tank as it makes things easier, as the only case where we would not need it is:
            // no handler on the item, AND no conversion recipe
            return !ChemicalInventorySlot.canFillOrConvert(ContainerType.CHEMICAL.createContainer(attachedAccess, tankIndex), BasicInventorySlot.NO_LEVEL, itemType);
        }, (itemType, automationType) -> {
            if (automationType.isInternal()) {
                return true;
            }
            //Copy of logic from ChemicalInventorySlot#getFillOrConvertInsertPredicate
            //Note: We eagerly resolve the chemical tank as it makes things easier, as the only case where we would not need it is:
            // no handler on the item, AND no conversion recipe
            return ChemicalInventorySlot.canFillOrConvert(ContainerType.CHEMICAL.createContainer(attachedAccess, tankIndex), BasicInventorySlot.NO_LEVEL, itemType);
        }, ConstantPredicates.alwaysTrue()));
    }
}