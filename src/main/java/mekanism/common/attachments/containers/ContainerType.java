package mekanism.common.attachments.containers;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
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
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ContainerType<CONTAINER extends INBTSerializable<CompoundTag>, ATTACHMENT extends AttachedContainers<CONTAINER>, HANDLER> {

    public static final ContainerType<IEnergyContainer, AttachedEnergyContainers, IStrictEnergyHandler> ENERGY =
          new ContainerType<>(MekanismAttachmentTypes.ENERGY_CONTAINERS, NBTConstants.ENERGY_CONTAINERS, AttachedEnergyContainers::new, Capabilities.STRICT_ENERGY) {
              @Override
              public void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, IMekanismConfig... requiredConfigs) {
                  EnergyCompatUtils.registerItemCapabilities(event, item, getCapabilityProvider(requiredConfigs));
              }

              @Override
              protected void registerEntityCapabilities(RegisterCapabilitiesEvent event, EntityType<?> entityType, IMekanismConfig... requiredConfigs) {
                  EnergyCompatUtils.registerEntityCapabilities(event, entityType, getCapabilityProvider(requiredConfigs));
              }
          };
    //TODO - 1.20.4: Implement these
    public static final ContainerType<IInventorySlot, AttachedInventorySlots, IItemHandler> ITEM = new ContainerType<>(MekanismAttachmentTypes.INVENTORY_SLOTS, NBTConstants.ITEMS, AttachedInventorySlots::new, Capabilities.ITEM);
    public static final ContainerType<IExtendedFluidTank, AttachedFluidTanks, IFluidHandler> FLUID = new ContainerType<>(MekanismAttachmentTypes.FLUID_TANKS, NBTConstants.FLUID_TANKS, AttachedFluidTanks::new, AttachedItemFluidTanks::new, Capabilities.FLUID);
    public static final ContainerType<IGasTank, AttachedGasTanks, IGasHandler> GAS = new ContainerType<>(MekanismAttachmentTypes.GAS_TANKS, NBTConstants.GAS_TANKS, AttachedGasTanks::new, Capabilities.GAS);
    public static final ContainerType<IInfusionTank, AttachedInfusionTanks, IInfusionHandler> INFUSION = new ContainerType<>(MekanismAttachmentTypes.INFUSION_TANKS, NBTConstants.INFUSION_TANKS, AttachedInfusionTanks::new, Capabilities.INFUSION);
    public static final ContainerType<IPigmentTank, AttachedPigmentTanks, IPigmentHandler> PIGMENT = new ContainerType<>(MekanismAttachmentTypes.PIGMENT_TANKS, NBTConstants.PIGMENT_TANKS, AttachedPigmentTanks::new, Capabilities.PIGMENT);
    public static final ContainerType<ISlurryTank, AttachedSlurryTanks, ISlurryHandler> SLURRY = new ContainerType<>(MekanismAttachmentTypes.SLURRY_TANKS, NBTConstants.SLURRY_TANKS, AttachedSlurryTanks::new, Capabilities.SLURRY);
    //TODO - 1.20.4: Re-evaluate this in regards to the capability
    public static final ContainerType<IHeatCapacitor, AttachedHeatCapacitors, IHeatHandler> HEAT = new ContainerType<>(MekanismAttachmentTypes.HEAT_CAPACITORS, NBTConstants.HEAT_CAPACITORS, AttachedHeatCapacitors::new, null);

    private final Map<Item, Function<ItemStack, List<CONTAINER>>> knownDefaultItemContainers = new Reference2ObjectOpenHashMap<>();
    private final Map<EntityType<?>, Function<Entity, List<CONTAINER>>> knownDefaultEntityContainers = new Reference2ObjectOpenHashMap<>();
    private final Function<List<CONTAINER>, ATTACHMENT> attachmentConstructor;
    @Nullable
    private final BiFunction<ItemStack, List<CONTAINER>, ATTACHMENT> itemAttachmentConstructor;
    private final Supplier<AttachmentType<ATTACHMENT>> attachment;
    @Nullable
    private final IMultiTypeCapability<HANDLER, ?> capability;
    private final String containerTag;

    private ContainerType(Supplier<AttachmentType<ATTACHMENT>> attachment, String containerTag, Function<List<CONTAINER>, ATTACHMENT> attachmentConstructor,
          @Nullable IMultiTypeCapability<HANDLER, ?> capability) {
        this(attachment, containerTag, attachmentConstructor, null, capability);
    }

    private ContainerType(Supplier<AttachmentType<ATTACHMENT>> attachment, String containerTag, Function<List<CONTAINER>, ATTACHMENT> attachmentConstructor,
          @Nullable BiFunction<ItemStack, List<CONTAINER>, ATTACHMENT> itemAttachmentConstructor, @Nullable IMultiTypeCapability<HANDLER, ?> capability) {
        this.attachment = attachment;
        this.containerTag = containerTag;
        this.attachmentConstructor = attachmentConstructor;
        this.itemAttachmentConstructor = itemAttachmentConstructor;
        this.capability = capability;
    }

    public ResourceLocation getAttachmentName() {
        return Objects.requireNonNull(NeoForgeRegistries.ATTACHMENT_TYPES.getKey(attachment.get()));
    }

    public String getTag() {
        return containerTag;
    }

    //TODO - 1.20.4: Rename this method to be more obvious that it also registers the capability
    public void addDefaultContainer(@Nullable IEventBus eventBus, Item item, Function<ItemStack, CONTAINER> defaultCreator, IMekanismConfig... requiredConfigs) {
        addDefaultContainers(eventBus, item, defaultCreator.andThen(List::of), requiredConfigs);
    }

    public void addDefaultContainers(@Nullable IEventBus eventBus, Item item, Function<ItemStack, List<CONTAINER>> defaultCreators, IMekanismConfig... requiredConfigs) {
        //TODO - 1.20.4: Do we need to check if the config is loaded here if there are required configs? Given our creator may create the container before the necessary configs are present
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

    public void addDefaultContainer(RegisterCapabilitiesEvent event, Holder<EntityType<?>> entityType, Function<Entity, CONTAINER> defaultCreator, IMekanismConfig... requiredConfigs) {
        addDefaultContainers(event, entityType, defaultCreator.andThen(List::of), requiredConfigs);
    }

    public void addDefaultContainers(RegisterCapabilitiesEvent event, Holder<EntityType<?>> holder, Function<Entity, List<CONTAINER>> defaultCreators, IMekanismConfig... requiredConfigs) {
        EntityType<?> entityType = holder.value();
        //TODO - 1.20.4: Do we need to check if the config is loaded here if there are required configs? Given our creator may create the container before the necessary configs are present
        knownDefaultEntityContainers.put(entityType, defaultCreators);
        registerEntityCapabilities(event, entityType, requiredConfigs);
    }

    protected void registerEntityCapabilities(RegisterCapabilitiesEvent event, EntityType<?> entityType, IMekanismConfig... requiredConfigs) {
        if (capability != null) {
            event.registerEntity(capability.entity(), entityType, getCapabilityProvider(requiredConfigs));
        }
    }

    @Nullable
    public ATTACHMENT getAttachmentIfPresent(IAttachmentHolder holder) {
        //TODO - 1.20.4: Use this helper in more places
        if (holder.hasData(attachment)) {
            return holder.getData(attachment);
        }
        return null;
    }

    @Nullable
    public ATTACHMENT getAttachment(IAttachmentHolder holder) {
        if (holder.hasData(attachment)) {
            return holder.getData(attachment);
        }
        if (holder instanceof ItemStack stack) {
            if (knownDefaultItemContainers.containsKey(stack.getItem())) {
                //TODO - 1.20.4: A way to load legacy data? Potentially when doesn't have attachment but is known default container
                return stack.getData(attachment);
            }
        } else if (holder instanceof Entity entity) {
            if (knownDefaultEntityContainers.containsKey(entity.getType())) {
                //TODO - 1.20.4: A way to load legacy data? Potentially when doesn't have attachment but is known default container
                return holder.getData(attachment);
            }
        }
        //TODO - 1.20.4: Support other types of holders?
        return null;
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
        if (!defaultContainers.isEmpty()) {
            return attachmentConstructor.apply(defaultContainers);
        }
        //TODO - 1.20.4: Return an immutable non serialized version that potentially NO-OPs certain aspects
        return attachmentConstructor.apply(List.of());
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
}