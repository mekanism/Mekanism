package mekanism.common.inventory.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.attachments.containers.AttachedContainers;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemSlotsBuilder {

    //Note: We don't render these in the GUI, so they don't need to be the actual values, and attachments are automatically saved so we don't need a listener
    @Nullable
    private static final IContentsListener LISTENER = null;
    private static final int X = 0;
    private static final int Y = 0;

    public static ItemSlotsBuilder builder(ItemStack backingStack) {
        return new ItemSlotsBuilder(backingStack);
    }

    private final List<IInventorySlot> slots = new ArrayList<>();
    private final ItemStack backingStack;

    private ItemSlotsBuilder(ItemStack backingStack) {
        this.backingStack = backingStack;
    }

    public List<IInventorySlot> build() {
        return List.copyOf(slots);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemStack> recipeInputPredicate) {
        return addBasicFactorySlots(process, recipeInputPredicate, false);
    }

    public ItemSlotsBuilder addBasicFactorySlots(int process, Predicate<ItemStack> recipeInputPredicate, boolean secondaryOutput) {
        for (int i = 0; i < process; i++) {
            //Note: We can just get away with using a simple input instead of a factory input slot and skip checking insert based on producing output
            addInput(recipeInputPredicate)
                  .addOutput();
            if (secondaryOutput) {
                addOutput();
            }
        }
        return this;
    }

    public ItemSlotsBuilder addSlots(int count, SlotCreator creator) {
        for (int i = 0; i < count; i++) {
            addSlot(creator);
        }
        return this;
    }

    public ItemSlotsBuilder addSlot(SlotCreator creator) {
        return addSlot(creator.create(LISTENER, X, Y));
    }

    public ItemSlotsBuilder addSlot(IInventorySlot slot) {
        slots.add(slot);
        return this;
    }

    public ItemSlotsBuilder addFuelSlot() {
        return addFuelSlot(stack -> stack.getBurnTime(null));
    }

    public ItemSlotsBuilder addFuelSlot(ToIntFunction<@NotNull ItemStack> fuelValue) {
        return addSlot(FuelInventorySlot.forFuel(fuelValue, LISTENER, X, Y));
    }

    public ItemSlotsBuilder addOutput() {
        return addSlot(OutputInventorySlot.at(LISTENER, X, Y));
    }

    public ItemSlotsBuilder addInput(Predicate<@NotNull ItemStack> isItemValid) {
        return addSlot(InputInventorySlot.at(isItemValid, LISTENER, X, Y));
    }

    public <RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> ItemSlotsBuilder addInput(IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> recipeType,
          ContainsRecipe<INPUT_CACHE> containsRecipe) {
        return addInput(stack -> containsRecipe.check(recipeType.getInputCache(), null, stack));
    }

    public ItemSlotsBuilder addEnergy() {
        return addEnergy(0);
    }

    public ItemSlotsBuilder addEnergy(int containerIndex) {
        return addEnergySlot(containerIndex, (container, listener, x, y) -> EnergyInventorySlot.fillOrConvert(container, () -> null, listener, x, y));
    }

    public ItemSlotsBuilder addEnergySlot(int containerIndex, ContainerBasedSlotCreator<IEnergyContainer> slotCreator) {
        return addContainerInput(ContainerType.ENERGY, containerIndex, slotCreator);
    }

    public ItemSlotsBuilder addFluidSlot(int tankIndex, ContainerBasedSlotCreator<IExtendedFluidTank> slotCreator) {
        return addContainerInput(ContainerType.FLUID, tankIndex, slotCreator);
    }

    public ItemSlotsBuilder addGasSlot(int tankIndex, ContainerBasedSlotCreator<IGasTank> slotCreator) {
        return addContainerInput(ContainerType.GAS, tankIndex, slotCreator);
    }

    public ItemSlotsBuilder addGasSlotWithConversion(int tankIndex) {
        return addGasSlot(tankIndex, (tank, listener, x, y) -> GasInventorySlot.fillOrConvert(tank, () -> null, listener, x, y));
    }

    public ItemSlotsBuilder addInfusionSlot(int tankIndex, ContainerBasedSlotCreator<IInfusionTank> slotCreator) {
        return addContainerInput(ContainerType.INFUSION, tankIndex, slotCreator);
    }

    public ItemSlotsBuilder addInfusionSlotWithConversion(int tankIndex) {
        return addInfusionSlot(tankIndex, (tank, listener, x, y) -> InfusionInventorySlot.fillOrConvert(tank, () -> null, listener, x, y));
    }

    public ItemSlotsBuilder addPigmentSlot(int tankIndex, ContainerBasedSlotCreator<IPigmentTank> slotCreator) {
        return addContainerInput(ContainerType.PIGMENT, tankIndex, slotCreator);
    }

    public ItemSlotsBuilder addSlurrySlot(int tankIndex, ContainerBasedSlotCreator<ISlurryTank> slotCreator) {
        return addContainerInput(ContainerType.SLURRY, tankIndex, slotCreator);
    }

    private <CONTAINER extends INBTSerializable<CompoundTag>> ItemSlotsBuilder addContainerInput(ContainerType<CONTAINER, ? extends AttachedContainers<CONTAINER>, ?> containerType,
          int containerIndex, ContainerBasedSlotCreator<CONTAINER> slotCreator) {
        //Note: In theory this shouldn't cause problems as we don't allow interacting with the actual slots, and we don't expose an ItemHandler
        // cap (currently ever), for stacked stacks
        AttachedContainers<CONTAINER> attachment = containerType.getAttachment(backingStack);
        if (attachment == null) {
            throw new IllegalStateException("Expected stack to have attached " + containerType.getAttachmentName() + " containers.");
        }
        List<CONTAINER> containers = attachment.getContainers();
        if (containerIndex >= containers.size()) {
            throw new IllegalStateException("Expected stack to have an attached " + containerType.getAttachmentName() + " container with index " + containerIndex);
        }
        return addContainerSlot(containers.get(containerIndex), slotCreator);
    }

    public <CONTAINER> ItemSlotsBuilder addContainerSlot(CONTAINER container, ContainerBasedSlotCreator<CONTAINER> slotCreator) {
        return addSlot(slotCreator.create(container, LISTENER, X, Y));
    }

    @FunctionalInterface
    public interface ContainsRecipe<INPUT_CACHE extends IInputRecipeCache> {

        boolean check(INPUT_CACHE cache, @Nullable Level level, ItemStack stack);
    }

    @FunctionalInterface
    public interface SlotCreator {

        IInventorySlot create(@Nullable IContentsListener listener, int x, int y);
    }

    @FunctionalInterface
    public interface ContainerBasedSlotCreator<CONTAINER> {

        IInventorySlot create(CONTAINER container, @Nullable IContentsListener listener, int x, int y);
    }
}