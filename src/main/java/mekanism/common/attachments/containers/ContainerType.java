package mekanism.common.attachments.containers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.DataHandlerUtils;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.chemical.AttachedChemicals;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalHandler;
import mekanism.common.attachments.containers.creator.IContainerCreator;
import mekanism.common.attachments.containers.energy.AttachedEnergy;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyHandler;
import mekanism.common.attachments.containers.fluid.AttachedFluids;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidHandler;
import mekanism.common.attachments.containers.heat.AttachedHeat;
import mekanism.common.attachments.containers.heat.ComponentBackedHeatHandler;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.attachments.containers.item.ComponentBackedItemHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IMultiTypeCapability;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 26.1: move these to resource handlers?
public class ContainerType<CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<?, ATTACHED>,
      HANDLER extends ComponentBackedHandler<?, CONTAINER, ATTACHED>> {

    private static final List<ContainerType<?, ?, ?>> TYPES_INTERNAL = new ArrayList<>();
    public static final List<ContainerType<?, ?, ?>> TYPES = Collections.unmodifiableList(TYPES_INTERNAL);

    //TODO - 26.1 - these collect/apply methods should be static, they don't actually use the tile. QIO has an item override, but other than that the inheritance is irrelevant
    //possibly change to use copy?
    public static final ContainerType<IEnergyContainer, AttachedEnergy, ComponentBackedEnergyHandler> ENERGY = new ContainerType<>(MekanismDataComponents.ATTACHED_ENERGY,
          SerializationConstants.ENERGY_CONTAINERS, SerializationConstants.CONTAINER, ComponentBackedEnergyHandler::new, Capabilities.STRICT_ENERGY, AttachedEnergy.EMPTY,
          TileEntityMekanism::getEnergyContainers, TileEntityMekanism::collectEnergyContainers, TileEntityMekanism::applyEnergyContainers, TileEntityMekanism::canHandleEnergy, (from, to) -> to.setEnergy(from.getEnergy())) {
        @Override
        @SuppressWarnings("unchecked")
        public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, boolean exposeWhenStacked, IMekanismConfig... requiredConfigs) {
            EnergyCompatUtils.registerItemCapabilities(event, item, (ICapabilityProvider<ItemStack, ItemAccess, IStrictEnergyHandler>) getCapabilityProvider(exposeWhenStacked, requiredConfigs));
        }
    };
    public static final ContainerType<IInventorySlot, AttachedItems, ComponentBackedItemHandler> ITEM = new ContainerType<>(MekanismDataComponents.ATTACHED_ITEMS,
          SerializationConstants.ITEMS, SerializationConstants.SLOT, ComponentBackedItemHandler::new, Capabilities.ITEM, AttachedItems.EMPTY,
          TileEntityMekanism::getInventorySlots, TileEntityMekanism::collectInventorySlots, TileEntityMekanism::applyInventorySlots, TileEntityMekanism::hasInventory, (from, to) -> {
        if (to instanceof BasicInventorySlot basicInventorySlot) {
            basicInventorySlot.setStackUnchecked(from.getResource(), from.amount());
        } else {
            to.setContents(from.getResource(), from.amount());
        }
    });
    public static final ContainerType<IExtendedFluidTank, AttachedFluids, ComponentBackedFluidHandler> FLUID = new ContainerType<>(MekanismDataComponents.ATTACHED_FLUIDS,
          SerializationConstants.FLUID_TANKS, SerializationConstants.TANK, ComponentBackedFluidHandler::new, Capabilities.FLUID_LEGACY, AttachedFluids.EMPTY,
          TileEntityMekanism::getFluidTanks, TileEntityMekanism::collectFluidTanks, TileEntityMekanism::applyFluidTanks, TileEntityMekanism::canHandleFluid, (from, to) -> to.setStackUnchecked(from.getFluid().copy()));

    public static final ContainerType<IChemicalTank, AttachedChemicals, ComponentBackedChemicalHandler> CHEMICAL = new ContainerType<>(MekanismDataComponents.ATTACHED_CHEMICALS,
          SerializationConstants.CHEMICAL_TANKS, SerializationConstants.TANK, ComponentBackedChemicalHandler::new, Capabilities.CHEMICAL, AttachedChemicals.EMPTY,
          TileEntityMekanism::getChemicalTanks, TileEntityMekanism::collectChemicalTanks, TileEntityMekanism::applyChemicalTanks, TileEntityMekanism::canHandleChemicals, (from, to) -> to.setStackUnchecked(from.getStack().copy()));

    public static final ContainerType<IHeatCapacitor, AttachedHeat, ComponentBackedHeatHandler> HEAT = new ContainerType<>(MekanismDataComponents.ATTACHED_HEAT,
          SerializationConstants.HEAT_CAPACITORS, SerializationConstants.CONTAINER, ComponentBackedHeatHandler::new, null, AttachedHeat.EMPTY,
          TileEntityMekanism::getHeatCapacitors, TileEntityMekanism::collectHeatCapacitors, TileEntityMekanism::applyHeatCapacitors, TileEntityMekanism::canHandleHeat, (from, to) -> {
        to.setHeat(from.getHeat());
        if (to instanceof BasicHeatCapacitor heatCapacitor) {
            heatCapacitor.setHeatCapacity(from.getHeatCapacity(), false);
        }
    });

    //TODO - 1.20.5: Re-evaluate this codec implementation
    //TODO - 26.1: seems unused?
    public static final Codec<ContainerType<?, ?, ?>> CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap(componentType -> {
        for (ContainerType<?, ?, ?> type : TYPES) {
            if (type.component.value() == componentType) {
                return DataResult.success(type);
            }
        }
        return DataResult.error(() -> "Data Component type " + Util.getRegisteredName(BuiltInRegistries.DATA_COMPONENT_TYPE, componentType) +
                                      " does not have a corresponding container type");
    }, containerType -> containerType.component.get());

    private final Map<Item, Lazy<? extends IContainerCreator<? extends CONTAINER, ATTACHED>>> knownDefaultCreators = new Reference2ObjectOpenHashMap<>();
    private final HandlerConstructor<HANDLER> handlerConstructor;
    private final BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile;
    private final CopyFromTile<CONTAINER, ATTACHED> copyFromTile;
    private final CopyToTile<CONTAINER, ATTACHED> copyToTile;
    private final CopyHandler<CONTAINER> copyHandler;
    private final DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component;
    @Nullable
    private final IMultiTypeCapability<? super HANDLER, ?> capability;
    private final Predicate<TileEntityMekanism> canHandle;
    private final ATTACHED emptyAttachment;
    private final String containerTag;
    protected final String containerKey;

    private ContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component, String containerTag, String containerKey,
          HandlerConstructor<HANDLER> handlerConstructor, @Nullable IMultiTypeCapability<? super HANDLER, ?> capability, ATTACHED emptyAttachment,
          BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile, CopyFromTile<CONTAINER, ATTACHED> copyFromTile,
          CopyToTile<CONTAINER, ATTACHED> copyToTile, Predicate<TileEntityMekanism> canHandle, CopyHandler<CONTAINER> copyHandler) {
        TYPES_INTERNAL.add(this);
        this.component = component;
        this.containerTag = containerTag;
        this.containerKey = containerKey;
        this.emptyAttachment = emptyAttachment;
        this.handlerConstructor = handlerConstructor;
        this.containersFromTile = containersFromTile;
        this.copyFromTile = copyFromTile;
        this.copyToTile = copyToTile;
        this.capability = capability;
        this.canHandle = canHandle;
        this.copyHandler = copyHandler;
    }

    public DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> getComponentType() {
        return component;
    }

    public Identifier getComponentName() {
        return component.getId();
    }

    public String getTag() {
        return containerTag;
    }

    /**
     * Adds some containers as default and exposes it as a capability that requires the given configs if the specified bus is present.
     */
    public void addDefaultCreators(@Nullable IEventBus eventBus, Item item, Supplier<? extends IContainerCreator<? extends CONTAINER, ATTACHED>> defaultCreator,
          IMekanismConfig... requiredConfigs) {
        knownDefaultCreators.put(item, Lazy.of(defaultCreator));
        if (eventBus != null && capability != null) {
            eventBus.addListener(RegisterCapabilitiesEvent.class, event -> registerItemCapabilities(event, item, false, requiredConfigs));
        }
    }

    public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, boolean exposeWhenStacked, IMekanismConfig... requiredConfigs) {
        if (capability != null) {
            event.registerItem((ItemCapability) capability.item(), getCapabilityProvider(exposeWhenStacked, requiredConfigs), item);
        }
    }

    //TODO - 1.21: Do we want to have create in the name instead of get
    public List<CONTAINER> getAttachmentContainersIfPresent(ItemStack stack) {
        HANDLER handler = createHandlerIfData(stack);
        return handler == null ? Collections.emptyList() : handler.getContainers();
    }

    public int getContainerCount(ItemInstance stack) {
        ATTACHED attached = getOrEmpty(stack);
        if (attached.isEmpty()) {
            return getContainerCount(stack.typeHolder().value());
        }
        //TODO - 1.21: Do we need to look it up in case the max size changed since we were last saved?
        return attached.size();
    }

    //for datagen
    public int getContainerCount(Item item) {
        Lazy<? extends IContainerCreator<? extends CONTAINER, ATTACHED>> containerCreator = knownDefaultCreators.get(item);
        return containerCreator == null ? 0 : containerCreator.get().totalContainers();
    }

    @Nullable//TODO - 26.1: remove me, just use caps
    public HANDLER createHandlerIfData(ItemStack stack) {
        ATTACHED attached = getOrEmpty(stack);
        //TODO - 1.21: Do we need to look it up in case the max size changed since we were last saved?
        return attached.isEmpty() ? null : handlerConstructor.create(stack, attached.size());
    }

    @Nullable
    public HANDLER createHandler(ItemStack stack) {
        //TODO - 1.21: Do we want local callers to just directly access the handler constructor as we wouldn't be exposing the cap
        // if we didn't have any creators?
        int count = getContainerCount(stack);
        if (count == 0) {
            return null;
        }
        return handlerConstructor.create(stack, count);
    }

    public ATTACHED createNewAttachment(ItemStack stack) {
        //TODO - 1.1: Do we want local callers to just directly access the handler constructor as we wouldn't be exposing the cap
        // if we didn't have any creators?
        Lazy<? extends IContainerCreator<? extends CONTAINER, ATTACHED>> lazy = knownDefaultCreators.get(stack.getItem());
        if (lazy == null) {
            return emptyAttachment;
        }
        IContainerCreator<? extends CONTAINER, ATTACHED> containerCreator = lazy.get();
        int count = containerCreator.totalContainers();
        if (count == 0) {
            return emptyAttachment;
        }
        return containerCreator.initStorage(count);
    }

    public ATTACHED getOrEmpty(DataComponentGetter stack) {
        return stack.getOrDefault(component, emptyAttachment);
    }

    //TODO - 1.21: Re-evaluate usages and see if they should be going via capability instead?
    public CONTAINER createContainer(ItemStack attachedTo, int containerIndex) {
        Lazy<? extends IContainerCreator<? extends CONTAINER, ATTACHED>> creator = knownDefaultCreators.get(attachedTo.getItem());
        if (creator != null) {
            return creator.get().create(this, attachedTo, containerIndex);
        }
        throw new IllegalArgumentException("No known containers for item " + attachedTo.getItem());
    }

    protected ICapabilityProvider<ItemStack, ItemAccess, ? super HANDLER> getCapabilityProvider(boolean exposeWhenStacked, IMekanismConfig... requiredConfigs) {
        if (exposeWhenStacked) {
            return getCapabilityProvider(requiredConfigs);
        } else if (requiredConfigs.length == 0) {
            return (stack, context) -> stack.count() == 1 ? createHandler(stack) : null;
        }
        //Only expose the capabilities if the required configs are loaded
        return (stack, context) -> stack.count() == 1 && hasRequiredConfigs(requiredConfigs) ? createHandler(stack) : null;
    }

    protected ICapabilityProvider<ItemStack, ItemAccess, ? super HANDLER> getCapabilityProvider(IMekanismConfig... requiredConfigs) {
        if (requiredConfigs.length == 0) {
            return (stack, context) -> createHandler(stack);
        }
        //Only expose the capabilities if the required configs are loaded
        return (stack, context) -> hasRequiredConfigs(requiredConfigs) ? createHandler(stack) : null;
    }

    private static boolean hasRequiredConfigs(IMekanismConfig... requiredConfigs) {
        for (IMekanismConfig requiredConfig : requiredConfigs) {
            if (!requiredConfig.isLoaded()) {
                return false;
            }
        }
        return true;
    }

    public boolean supports(ItemStack stack) {
        return stack.has(component) || knownDefaultCreators.containsKey(stack.getItem());
    }

    public void addDefault(Item item, DataComponentMap.Builder components) {
        Lazy<? extends IContainerCreator<? extends CONTAINER, ATTACHED>> lazy = knownDefaultCreators.get(item);
        if (lazy != null) {
            //Supports the type
            IContainerCreator<? extends CONTAINER, ATTACHED> containerCreator = lazy.get();
            int count = containerCreator.totalContainers();
            if (count > 0) {
                components.set(component.get(), containerCreator.initStorage(count));
            }
        }
    }

    public static boolean anySupports(Holder<Item> item) {
        for (ContainerType<?, ?, ?> type : TYPES) {
            if (type.knownDefaultCreators.containsKey(item.value())) {
                return true;
            }
        }
        return false;
    }

    public void saveTo(ValueOutput output, TileEntityMekanism tile) {
        saveTo(output, getContainers(tile));
    }

    public void saveTo(ValueOutput output, List<CONTAINER> containers) {
        ValueOutputList storedContainers = output.childrenList(containerTag);
        DataHandlerUtils.writeContents(storedContainers, containerKey, containers);
        if (storedContainers.isEmpty()) {
            output.discard(containerTag);
        }
    }

    public void readFrom(ValueInput input, TileEntityMekanism tile) {
        readFrom(input, getContainers(tile));
    }

    public void readFrom(ValueInput input, List<CONTAINER> containers) {
        //TODO - 26.1: Should this not be orEmpty?
        DataHandlerUtils.readContents(input.childrenListOrEmpty(containerTag), containerKey, containers);
    }

    public void copyToTile(TileEntityMekanism tile, DataComponentGetter input) {
        ATTACHED attachedData = input.get(component);
        if (attachedData != null) {
            copyToTile.copy(tile, input, getContainers(tile), attachedData);
        }
    }

    public void copyFromTile(TileEntityMekanism tile, DataComponentMap.Builder builder) {
        List<CONTAINER> containers = getContainers(tile);
        if (!containers.isEmpty()) {
            ATTACHED attachedData = copyFromTile.copy(tile, builder, containers);
            if (attachedData != null) {
                builder.set(component, attachedData);
            }
        }
    }

    public void copy(CONTAINER from, CONTAINER to) {
        copyHandler.copy(from, to);
    }

    public boolean canHandle(TileEntityMekanism tile) {
        return canHandle.test(tile);
    }

    public List<CONTAINER> getContainers(TileEntityMekanism tile) {
        return containersFromTile.apply(tile, null);
    }

    @FunctionalInterface
    private interface HandlerConstructor<HANDLER extends ComponentBackedHandler<?, ?, ?>> {

        HANDLER create(ItemStack attachedTo, int totalContainers);
    }

    @FunctionalInterface
    public interface CopyToTile<CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<?, ATTACHED>> {

        void copy(TileEntityMekanism tile, DataComponentGetter input, List<CONTAINER> containers, ATTACHED attachedData);
    }

    @FunctionalInterface
    public interface CopyFromTile<CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<?, ATTACHED>> {

        @Nullable
        ATTACHED copy(TileEntityMekanism tile, DataComponentMap.Builder builder, List<CONTAINER> containers);
    }

    @FunctionalInterface
    public interface CopyHandler<CONTAINER extends ValueIOSerializable> {

        void copy(CONTAINER from, CONTAINER to);
    }
}