package mekanism.common.attachments.containers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedGasTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedInfusionTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedPigmentTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedSlurryTanks;
import mekanism.common.attachments.containers.AttachedFluidTanks.AttachedItemFluidTanks;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IMultiTypeCapability;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ContainerType<CONTAINER extends INBTSerializable<CompoundTag>, ATTACHMENT extends AttachedContainers<CONTAINER>, HANDLER> {

    private static final List<ContainerType<?, ?, ?>> TYPES_INTERNAL = new ArrayList<>();
    public static final List<ContainerType<?, ?, ?>> TYPES = Collections.unmodifiableList(TYPES_INTERNAL);

    public static final ContainerType<IEnergyContainer, AttachedEnergyContainers, IStrictEnergyHandler> ENERGY =
          new ContainerType<>(MekanismDataComponents.ENERGY_CONTAINERS, NBTConstants.ENERGY_CONTAINERS, NBTConstants.CONTAINER, AttachedEnergyContainers::new, Capabilities.STRICT_ENERGY, TileEntityMekanism::getEnergyContainers,
                TileEntityMekanism::canHandleEnergy) {
              @Override
              public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, boolean exposeWhenStacked, IMekanismConfig... requiredConfigs) {
                  EnergyCompatUtils.registerItemCapabilities(event, item, getCapabilityProvider(exposeWhenStacked, requiredConfigs));
              }
          };
    public static final ContainerType<IInventorySlot, AttachedInventorySlots, IItemHandler> ITEM = new ContainerType<>(MekanismDataComponents.INVENTORY_SLOTS, NBTConstants.ITEMS, NBTConstants.SLOT, AttachedInventorySlots::new, Capabilities.ITEM, TileEntityMekanism::getInventorySlots, TileEntityMekanism::hasInventory);
    public static final ContainerType<IExtendedFluidTank, AttachedFluidTanks, IFluidHandler> FLUID = new ContainerType<>(MekanismDataComponents.FLUID_TANKS, NBTConstants.FLUID_TANKS, NBTConstants.TANK, AttachedFluidTanks::new, AttachedItemFluidTanks::new, Capabilities.FLUID, TileEntityMekanism::getFluidTanks, TileEntityMekanism::canHandleFluid);
    public static final ContainerType<IGasTank, AttachedGasTanks, IGasHandler> GAS = new ContainerType<>(MekanismDataComponents.GAS_TANKS, NBTConstants.GAS_TANKS, NBTConstants.TANK, AttachedGasTanks::new, Capabilities.GAS, TileEntityMekanism::getGasTanks, TileEntityMekanism::canHandleGas);
    public static final ContainerType<IInfusionTank, AttachedInfusionTanks, IInfusionHandler> INFUSION = new ContainerType<>(MekanismDataComponents.INFUSION_TANKS, NBTConstants.INFUSION_TANKS, NBTConstants.TANK, AttachedInfusionTanks::new, Capabilities.INFUSION, TileEntityMekanism::getInfusionTanks, TileEntityMekanism::canHandleInfusion);
    public static final ContainerType<IPigmentTank, AttachedPigmentTanks, IPigmentHandler> PIGMENT = new ContainerType<>(MekanismDataComponents.PIGMENT_TANKS, NBTConstants.PIGMENT_TANKS, NBTConstants.TANK, AttachedPigmentTanks::new, Capabilities.PIGMENT, TileEntityMekanism::getPigmentTanks, TileEntityMekanism::canHandlePigment);
    public static final ContainerType<ISlurryTank, AttachedSlurryTanks, ISlurryHandler> SLURRY = new ContainerType<>(MekanismDataComponents.SLURRY_TANKS, NBTConstants.SLURRY_TANKS, NBTConstants.TANK, AttachedSlurryTanks::new, Capabilities.SLURRY, TileEntityMekanism::getSlurryTanks, TileEntityMekanism::canHandleSlurry);
    public static final ContainerType<IHeatCapacitor, AttachedHeatCapacitors, IHeatHandler> HEAT = new ContainerType<>(MekanismDataComponents.HEAT_CAPACITORS, NBTConstants.HEAT_CAPACITORS, NBTConstants.CONTAINER, AttachedHeatCapacitors::new, null, TileEntityMekanism::getHeatCapacitors, TileEntityMekanism::canHandleHeat);

    public static final Codec<ContainerType<?, ?, ?>> CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap(componentType -> {
        for (ContainerType<?, ?, ?> type : TYPES) {
            if (type.attachment.value() == componentType) {
                return DataResult.success(type);
            }
        }
        return DataResult.error(() -> "Attachment type " + BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType) + " does not have a corresponding container type");
    }, containerType -> containerType.attachment.get());

    private final Map<Item, Function<ItemStack, List<CONTAINER>>> knownDefaultItemContainers = new Reference2ObjectOpenHashMap<>();
    private final Map<EntityType<?>, Function<Entity, List<CONTAINER>>> knownDefaultEntityContainers = new Reference2ObjectOpenHashMap<>();
    private final BiFunction<List<CONTAINER>, @Nullable IContentsListener, ATTACHMENT> attachmentConstructor;
    private final BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile;
    @Nullable
    private final BiFunction<ItemStack, List<CONTAINER>, ATTACHMENT> itemAttachmentConstructor;
    private final DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHMENT>> attachment;
    @Nullable
    private final IMultiTypeCapability<HANDLER, ?> capability;
    private final Predicate<TileEntityMekanism> canHandle;
    private final String containerTag;
    private final String containerKey;

    private ContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHMENT>> attachment, String containerTag, String containerKey,
          BiFunction<List<CONTAINER>, @Nullable IContentsListener, ATTACHMENT> attachmentConstructor, @Nullable IMultiTypeCapability<HANDLER, ?> capability,
          BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile, Predicate<TileEntityMekanism> canHandle) {
        this(attachment, containerTag, containerKey, attachmentConstructor, null, capability, containersFromTile, canHandle);
    }

    private ContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHMENT>> attachment, String containerTag, String containerKey,
          BiFunction<List<CONTAINER>, @Nullable IContentsListener, ATTACHMENT> attachmentConstructor, @Nullable BiFunction<ItemStack, List<CONTAINER>, ATTACHMENT> itemAttachmentConstructor,
          @Nullable IMultiTypeCapability<HANDLER, ?> capability, BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile, Predicate<TileEntityMekanism> canHandle) {
        TYPES_INTERNAL.add(this);
        this.attachment = attachment;
        this.containerTag = containerTag;
        this.containerKey = containerKey;
        this.attachmentConstructor = attachmentConstructor;
        this.itemAttachmentConstructor = itemAttachmentConstructor;
        this.containersFromTile = containersFromTile;
        this.capability = capability;
        this.canHandle = canHandle;
    }

    public DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHMENT>> getDataComponentTypeHolder() {
        return attachment;
    }

    @Nullable
    public ResourceLocation getAttachmentName() {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(attachment.get());
    }

    public String getTag() {
        return containerTag;
    }

    /**
     * Adds a container as default and exposes it as a capability that requires the given configs if the specified bus is present.
     */
    public void addDefaultContainer(@Nullable IEventBus eventBus, Item item, Function<ItemStack, CONTAINER> defaultCreator, IMekanismConfig... requiredConfigs) {
        addDefaultContainers(eventBus, item, defaultCreator.andThen(List::of), requiredConfigs);
    }

    /**
     * Adds some containers as default and exposes it as a capability that requires the given configs if the specified bus is present.
     */
    public void addDefaultContainers(@Nullable IEventBus eventBus, Item item, Function<ItemStack, List<CONTAINER>> defaultCreators, IMekanismConfig... requiredConfigs) {
        knownDefaultItemContainers.put(item, defaultCreators);
        if (eventBus != null && capability != null) {
            eventBus.addListener(RegisterCapabilitiesEvent.class, event -> registerItemCapabilities(event, item, false, requiredConfigs));
        }
    }

    public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, boolean exposeWhenStacked, IMekanismConfig... requiredConfigs) {
        if (capability != null) {
            event.registerItem(capability.item(), getCapabilityProvider(exposeWhenStacked, requiredConfigs), item);
        }
    }

    public List<CONTAINER> getAttachmentContainersIfPresent(ItemStack stack) {
        ATTACHMENT attachment = getAttachmentIfPresent(stack);
        return attachment == null ? Collections.emptyList() : attachment.getContainers();
    }

    @Nullable
    public ATTACHMENT getAttachmentIfPresent(ItemStack stack) {
        return stack.get(attachment);
    }

    @Nullable
    public ATTACHMENT getAttachment(ItemStack stack) {
        ATTACHMENT existingData = stack.get(attachment);
        if (existingData == null && knownDefaultItemContainers.containsKey(stack.getItem())) {
            stack.set(attachment, existingData = getDefaultInternal(stack));
        }
        return existingData;
    }

    public ATTACHMENT getDefault(ItemStack stack) {
        ATTACHMENT attachment = getDefaultInternal(stack);
        if (attachment == null) {
            throw new IllegalArgumentException("Attempted to attach a " + getAttachmentName() + " container to an object (" + RegistryUtils.getName(stack.getItem()) +
                                               ") that doesn't have containers of that type.");
        }
        return attachment;
    }

    @Nullable
    private ATTACHMENT getDefaultInternal(ItemStack stack) {
        List<CONTAINER> defaultContainers = Collections.emptyList();
        if (!stack.isEmpty()) {
            defaultContainers = knownDefaultItemContainers.getOrDefault(stack.getItem(), s -> Collections.emptyList()).apply(stack);
            if (!defaultContainers.isEmpty() && itemAttachmentConstructor != null) {
                return itemAttachmentConstructor.apply(stack, defaultContainers);
            }
        }
        if (defaultContainers.isEmpty()) {
            return null;
        }
        //TODO: If we end up supporting other types of attachment holders than stacks and entities we will want to make sure to pass a contents listener to them
        // we don't need to for items or entities as attachments on them are always saved
        return attachmentConstructor.apply(defaultContainers, null);
    }

    @SuppressWarnings("unchecked")
    protected <H extends HANDLER> ICapabilityProvider<ItemStack, Void, H> getCapabilityProvider(boolean exposeWhenStacked, IMekanismConfig... requiredConfigs) {
        if (exposeWhenStacked) {
            return getCapabilityProvider(requiredConfigs);
        } else if (requiredConfigs.length == 0) {
            return (stack, context) -> stack.getCount() == 1 ? (H) getAttachment(stack) : null;
        }
        //Only expose the capabilities if the required configs are loaded
        return (stack, context) -> stack.getCount() == 1 && hasRequiredConfigs(requiredConfigs) ? (H) getAttachment(stack) : null;
    }

    @SuppressWarnings("unchecked")
    protected <H extends HANDLER> ICapabilityProvider<ItemStack, Void, H> getCapabilityProvider(IMekanismConfig... requiredConfigs) {
        if (requiredConfigs.length == 0) {
            return (stack, context) -> (H) getAttachment(stack);
        }
        //Only expose the capabilities if the required configs are loaded
        return (stack, context) -> hasRequiredConfigs(requiredConfigs) ? (H) getAttachment(stack) : null;
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
        return stack.has(attachment) || knownDefaultItemContainers.containsKey(stack.getItem());
    }

    ListTag save(HolderLookup.Provider provider, List<CONTAINER> containers) {
        return DataHandlerUtils.writeContents(provider, containers, containerKey);
    }

    void read(HolderLookup.Provider provider, List<CONTAINER> containers, @Nullable ListTag storedContainers) {
        if (storedContainers != null) {
            DataHandlerUtils.readContents(provider, containers, storedContainers, containerKey);
        }
    }

    public void saveTo(HolderLookup.Provider provider, CompoundTag tag, TileEntityMekanism tile) {
        saveTo(provider, tag, getContainers(tile));
    }

    public void saveTo(HolderLookup.Provider provider, CompoundTag tag, List<CONTAINER> containers) {
        ListTag serialized = save(provider, containers);
        if (!serialized.isEmpty()) {
            tag.put(containerTag, serialized);
        }
    }

    public void readFrom(HolderLookup.Provider provider, CompoundTag tag, TileEntityMekanism tile) {
        readFrom(provider, tag, getContainers(tile));
    }

    public void readFrom(HolderLookup.Provider provider, CompoundTag tag, List<CONTAINER> containers) {
        read(provider, containers, tag.getList(containerTag, Tag.TAG_COMPOUND));
    }

    public void copyTo(HolderLookup.Provider provider, TileEntityMekanism tile, ItemStack stack) {
        copyTo(provider, getContainers(tile), stack);
    }

    public void copyTo(HolderLookup.Provider provider, List<CONTAINER> containers, ItemStack stack) {
        ATTACHMENT attachment = getAttachment(stack);
        if (attachment != null) {
            attachment.deserializeNBT(provider, save(provider, containers));
            if (stack.getCount() > 1) {
                Mekanism.logger.error("Copied {} to a stack ({}) of {}. This might lead to duplication of data.", getAttachmentName(), stack.getCount(), RegistryUtils.getName(stack.getItem()));
            }
        }
    }

    public void copyFrom(HolderLookup.Provider provider, ItemStack stack, TileEntityMekanism tile) {
        copyFrom(provider, stack, getContainers(tile));
    }

    public void copyFrom(HolderLookup.Provider provider, ItemStack stack, List<CONTAINER> containers) {
        ATTACHMENT attachment = getAttachment(stack);
        if (attachment != null) {
            read(provider, containers, attachment.serializeNBT(provider));
        }
    }

    public boolean canHandle(TileEntityMekanism tile) {
        return canHandle.test(tile);
    }

    public List<CONTAINER> getContainers(TileEntityMekanism tile) {
        return containersFromTile.apply(tile, null);
    }
}