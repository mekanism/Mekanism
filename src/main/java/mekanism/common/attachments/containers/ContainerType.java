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
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedGasTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedInfusionTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedPigmentTanks;
import mekanism.common.attachments.containers.AttachedChemicalTanks.AttachedSlurryTanks;
import mekanism.common.attachments.containers.AttachedFluidTanks.AttachedItemFluidTanks;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IMultiTypeCapability;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.chemical.IGasTile;
import mekanism.common.tile.interfaces.chemical.IInfusionTile;
import mekanism.common.tile.interfaces.chemical.IPigmentTile;
import mekanism.common.tile.interfaces.chemical.ISlurryTile;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ContainerType<CONTAINER extends INBTSerializable<CompoundTag>, ATTACHMENT extends AttachedContainers<CONTAINER>, HANDLER> {

    private static final List<ContainerType<?, ?, ?>> TYPES_INTERNAL = new ArrayList<>();
    public static final List<ContainerType<?, ?, ?>> TYPES = Collections.unmodifiableList(TYPES_INTERNAL);

    public static final ContainerType<IEnergyContainer, AttachedEnergyContainers, IStrictEnergyHandler> ENERGY =
          new ContainerType<>(MekanismAttachmentTypes.ENERGY_CONTAINERS, NBTConstants.ENERGY_CONTAINERS, AttachedEnergyContainers::new, Capabilities.STRICT_ENERGY, IMekanismStrictEnergyHandler::getEnergyContainers,
                IMekanismStrictEnergyHandler::canHandleEnergy) {
              @Override
              public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, IMekanismConfig... requiredConfigs) {
                  EnergyCompatUtils.registerItemCapabilities(event, item, getCapabilityProvider(requiredConfigs));
              }

              @Override
              protected void registerEntityCapabilities(RegisterCapabilitiesEvent event, EntityType<?> entityType, IMekanismConfig... requiredConfigs) {
                  EnergyCompatUtils.registerEntityCapabilities(event, entityType, getCapabilityProvider(requiredConfigs));
              }
          };
    //TODO - 1.20.4: Implement item containers using this system?
    public static final ContainerType<IInventorySlot, AttachedInventorySlots, IItemHandler> ITEM = new ContainerType<>(MekanismAttachmentTypes.INVENTORY_SLOTS, NBTConstants.ITEMS, AttachedInventorySlots::new, Capabilities.ITEM, IMekanismInventory::getInventorySlots, IMekanismInventory::hasInventory);
    public static final ContainerType<IExtendedFluidTank, AttachedFluidTanks, IFluidHandler> FLUID = new ContainerType<>(MekanismAttachmentTypes.FLUID_TANKS, NBTConstants.FLUID_TANKS, AttachedFluidTanks::new, AttachedItemFluidTanks::new, Capabilities.FLUID, IMekanismFluidHandler::getFluidTanks, IMekanismFluidHandler::canHandleFluid);
    public static final ContainerType<IGasTank, AttachedGasTanks, IGasHandler> GAS = new ContainerType<>(MekanismAttachmentTypes.GAS_TANKS, NBTConstants.GAS_TANKS, AttachedGasTanks::new, Capabilities.GAS, IGasTile::getGasTanks, IGasTile::canHandleGas);
    public static final ContainerType<IInfusionTank, AttachedInfusionTanks, IInfusionHandler> INFUSION = new ContainerType<>(MekanismAttachmentTypes.INFUSION_TANKS, NBTConstants.INFUSION_TANKS, AttachedInfusionTanks::new, Capabilities.INFUSION, IInfusionTile::getInfusionTanks, IInfusionTile::canHandleInfusion);
    public static final ContainerType<IPigmentTank, AttachedPigmentTanks, IPigmentHandler> PIGMENT = new ContainerType<>(MekanismAttachmentTypes.PIGMENT_TANKS, NBTConstants.PIGMENT_TANKS, AttachedPigmentTanks::new, Capabilities.PIGMENT, IPigmentTile::getPigmentTanks, IPigmentTile::canHandlePigment);
    public static final ContainerType<ISlurryTank, AttachedSlurryTanks, ISlurryHandler> SLURRY = new ContainerType<>(MekanismAttachmentTypes.SLURRY_TANKS, NBTConstants.SLURRY_TANKS, AttachedSlurryTanks::new, Capabilities.SLURRY, ISlurryTile::getSlurryTanks, ISlurryTile::canHandleSlurry);
    public static final ContainerType<IHeatCapacitor, AttachedHeatCapacitors, IHeatHandler> HEAT = new ContainerType<>(MekanismAttachmentTypes.HEAT_CAPACITORS, NBTConstants.HEAT_CAPACITORS, AttachedHeatCapacitors::new, null, IMekanismHeatHandler::getHeatCapacitors, IMekanismHeatHandler::canHandleHeat);

    public static final Codec<ContainerType<?, ?, ?>> CODEC = NeoForgeRegistries.ATTACHMENT_TYPES.byNameCodec().comapFlatMap(attachmentType -> {
        for (ContainerType<?, ?, ?> type : TYPES) {
            if (type.attachment.value() == attachmentType) {
                return DataResult.success(type);
            }
        }
        return DataResult.error(() -> "Attachment type " + NeoForgeRegistries.ATTACHMENT_TYPES.getKey(attachmentType) + " does not have a corresponding container type");
    }, containerType -> containerType.attachment.get());

    //TODO - 1.20.4: Replace with just using TYPES once we move items over to attachments
    public static final List<ContainerType<?, ?, ?>> SUBSTANCES = Util.make(() -> {
        List<ContainerType<?, ?, ?>> containers = new ArrayList<>(TYPES);
        containers.remove(ITEM);
        return Collections.unmodifiableList(containers);
    });

    private final Map<Item, Function<ItemStack, List<CONTAINER>>> knownDefaultItemContainers = new Reference2ObjectOpenHashMap<>();
    private final Map<EntityType<?>, Function<Entity, List<CONTAINER>>> knownDefaultEntityContainers = new Reference2ObjectOpenHashMap<>();
    private final BiFunction<List<CONTAINER>, @Nullable IContentsListener, ATTACHMENT> attachmentConstructor;
    private final BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile;
    @Nullable
    private final BiFunction<ItemStack, List<CONTAINER>, ATTACHMENT> itemAttachmentConstructor;
    private final DeferredHolder<AttachmentType<?>, AttachmentType<ATTACHMENT>> attachment;
    @Nullable
    private final IMultiTypeCapability<HANDLER, ?> capability;
    private final Predicate<TileEntityMekanism> canHandle;
    private final String containerTag;

    private ContainerType(DeferredHolder<AttachmentType<?>, AttachmentType<ATTACHMENT>> attachment, String containerTag, BiFunction<List<CONTAINER>, @Nullable IContentsListener, ATTACHMENT> attachmentConstructor,
          @Nullable IMultiTypeCapability<HANDLER, ?> capability, BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile, Predicate<TileEntityMekanism> canHandle) {
        this(attachment, containerTag, attachmentConstructor, null, capability, containersFromTile, canHandle);
    }

    private ContainerType(DeferredHolder<AttachmentType<?>, AttachmentType<ATTACHMENT>> attachment, String containerTag, BiFunction<List<CONTAINER>, @Nullable IContentsListener, ATTACHMENT> attachmentConstructor,
          @Nullable BiFunction<ItemStack, List<CONTAINER>, ATTACHMENT> itemAttachmentConstructor, @Nullable IMultiTypeCapability<HANDLER, ?> capability,
          BiFunction<TileEntityMekanism, @Nullable Direction, List<CONTAINER>> containersFromTile, Predicate<TileEntityMekanism> canHandle) {
        TYPES_INTERNAL.add(this);
        this.attachment = attachment;
        this.containerTag = containerTag;
        this.attachmentConstructor = attachmentConstructor;
        this.itemAttachmentConstructor = itemAttachmentConstructor;
        this.containersFromTile = containersFromTile;
        this.capability = capability;
        this.canHandle = canHandle;
    }

    public DeferredHolder<AttachmentType<?>, AttachmentType<ATTACHMENT>> getAttachmentHolder() {
        return attachment;
    }

    @Nullable
    public ResourceLocation getAttachmentName() {
        return NeoForgeRegistries.ATTACHMENT_TYPES.getKey(attachment.get());
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
            eventBus.addListener(RegisterCapabilitiesEvent.class, event -> registerItemCapabilities(event, item, requiredConfigs));
        }
    }

    public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, IMekanismConfig... requiredConfigs) {
        if (capability != null) {
            event.registerItem(capability.item(), getCapabilityProvider(requiredConfigs), item);
        }
    }

    /**
     * Adds a container as default and exposes it as a capability that requires the given configs if the specified bus is present.
     */
    public void addDefaultContainer(RegisterCapabilitiesEvent event, Holder<EntityType<?>> entityType, Function<Entity, CONTAINER> defaultCreator, IMekanismConfig... requiredConfigs) {
        addDefaultContainers(event, entityType, defaultCreator.andThen(List::of), requiredConfigs);
    }

    /**
     * Adds a container as default and exposes it as a capability that requires the given configs if the specified bus is present.
     */
    public void addDefaultContainers(RegisterCapabilitiesEvent event, Holder<EntityType<?>> holder, Function<Entity, List<CONTAINER>> defaultCreators, IMekanismConfig... requiredConfigs) {
        EntityType<?> entityType = holder.value();
        knownDefaultEntityContainers.put(entityType, defaultCreators);
        registerEntityCapabilities(event, entityType, requiredConfigs);
    }

    protected void registerEntityCapabilities(RegisterCapabilitiesEvent event, EntityType<?> entityType, IMekanismConfig... requiredConfigs) {
        if (capability != null) {
            event.registerEntity(capability.entity(), entityType, getCapabilityProvider(requiredConfigs));
        }
    }

    public List<CONTAINER> getAttachmentContainersIfPresent(IAttachmentHolder holder) {
        ATTACHMENT attachment = getAttachmentIfPresent(holder);
        return attachment == null ? List.of() : attachment.getContainers();
    }

    @Nullable
    public ATTACHMENT getAttachmentIfPresent(IAttachmentHolder holder) {
        if (holder.hasData(attachment)) {
            return holder.getData(attachment);
        }
        if (holder instanceof ItemStack stack && hasLegacyData(stack)) {
            //If the holder is an item that has legacy data then we want have the attachment get attached which will cause the legacy data to be removed
            // and converted to the new format
            return holder.getData(attachment);
        }
        return null;
    }

    @Nullable
    public ATTACHMENT getAttachment(IAttachmentHolder holder) {
        if (holder.hasData(this.attachment)) {
            return holder.getData(this.attachment);
        } else if (holder instanceof ItemStack stack) {
            if (knownDefaultItemContainers.containsKey(stack.getItem())) {
                return stack.getData(this.attachment);
            }
        } else if (holder instanceof Entity entity) {
            if (knownDefaultEntityContainers.containsKey(entity.getType())) {
                return holder.getData(this.attachment);
            }
        }
        return null;
    }

    @Deprecated//TODO - 1.21?: Remove all usages of this
    public boolean hasLegacyData(ItemStack stack) {
        return !stack.isEmpty() && ItemDataUtils.getMekData(stack).filter(mekData -> mekData.contains(containerTag, Tag.TAG_LIST)).isPresent();
    }

    @Deprecated//TODO - 1.21?: Remove this way of loading legacy data
    public ATTACHMENT getDefaultWithLegacy(IAttachmentHolder holder) {
        ATTACHMENT attachment = getDefault(holder);
        //If it is an itemstack try to load legacy data
        if (holder instanceof ItemStack stack && !stack.isEmpty()) {
            ItemDataUtils.getAndRemoveData(stack, containerTag, (c, k) -> c.getList(k, Tag.TAG_COMPOUND)).ifPresent(attachment::deserializeNBT);
        }
        return attachment;
    }

    public ATTACHMENT getDefault(IAttachmentHolder holder) {
        List<CONTAINER> defaultContainers = List.of();
        if (holder instanceof ItemStack stack) {
            if (!stack.isEmpty()) {
                defaultContainers = knownDefaultItemContainers.getOrDefault(stack.getItem(), s -> List.of()).apply(stack);
                if (!defaultContainers.isEmpty() && itemAttachmentConstructor != null) {
                    return itemAttachmentConstructor.apply(stack, defaultContainers);
                }
            }
        } else if (holder instanceof Entity entity) {
            defaultContainers = knownDefaultEntityContainers.getOrDefault(entity.getType(), s -> List.of()).apply(entity);
        }
        if (defaultContainers.isEmpty()) {
            throw new IllegalArgumentException("Attempted to attach a " + getAttachmentName() + " container to an object that doesn't have containers of that type.");
        }
        //TODO: If we end up supporting other types of attachment holders than stacks and entities we will want to make sure to pass a contents listener to them
        // we don't need to for items or entities as attachments on them are always saved
        return attachmentConstructor.apply(defaultContainers, null);
    }

    protected <HOLDER extends IAttachmentHolder, CONTEXT, H extends HANDLER> ICapabilityProvider<HOLDER, CONTEXT, H> getCapabilityProvider(IMekanismConfig... requiredConfigs) {
        ICapabilityProvider<HOLDER, CONTEXT, ?> provider;
        if (requiredConfigs.length == 0) {
            provider = (stack, context) -> getAttachment(stack);
        } else {
            provider = (stack, context) -> {
                //Only expose the capabilities if the required configs are loaded
                for (IMekanismConfig requiredConfig : requiredConfigs) {
                    if (!requiredConfig.isLoaded()) {
                        return null;
                    }
                }
                return getAttachment(stack);
            };
        }
        return (ICapabilityProvider<HOLDER, CONTEXT, H>) provider;
    }

    public boolean supports(ItemStack stack) {
        if (stack.hasData(attachment) || hasLegacyData(stack)) {
            return true;
        }
        return knownDefaultItemContainers.containsKey(stack.getItem());
    }

    public void saveTo(CompoundTag tag, List<CONTAINER> containers) {
        tag.put(containerTag, DataHandlerUtils.writeContainers(containers));
    }

    public void readFrom(CompoundTag tag, List<CONTAINER> containers) {
        DataHandlerUtils.readContainers(containers, tag.getList(containerTag, Tag.TAG_COMPOUND));
    }

    public boolean canHandle(TileEntityMekanism tile) {
        return canHandle.test(tile);
    }

    public List<CONTAINER> getContainers(TileEntityMekanism tile) {
        return containersFromTile.apply(tile, null);
    }

    public ListTag serialize(TileEntityMekanism tile) {
        return DataHandlerUtils.writeContainers(getContainers(tile));
    }

    public void deserialize(TileEntityMekanism tile, @Nullable ListTag containers) {
        if (containers != null) {
            DataHandlerUtils.readContainers(getContainers(tile), containers);
        }
    }
}