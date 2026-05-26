package mekanism.common.item.gear;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ICustomModule.ModuleDamageAbsorbInfo;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.attachments.containers.fluid.FluidTanksBuilder;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.laser.item.LaserDissipationHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.LivingEntityEquipmentWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekaSuitArmor extends ItemSpecialArmor implements IModuleContainerItem, IJetpackItem, ICustomCreativeTabContents, IAttachmentAware, ICapabilityAware {

    //TODO: Expand this system so that modules can maybe define needed tanks?
    private final List<GenericTankSpec<ChemicalResource>> chemicalTankSpecs = new ArrayList<>();
    private final List<GenericTankSpec<ChemicalResource>> chemicalTankSpecsView = Collections.unmodifiableList(chemicalTankSpecs);
    private final List<GenericTankSpec<FluidResource>> fluidTankSpecs = new ArrayList<>();
    private final List<GenericTankSpec<FluidResource>> fluidTankSpecsView = Collections.unmodifiableList(fluidTankSpecs);
    private final float absorption;
    //Full laser dissipation causes 3/4 of the energy to be dissipated and the remaining energy to be refracted
    private final double laserDissipation;
    private final double laserRefraction;
    private final ArmorType armorType;

    public ItemMekaSuitArmor(ArmorType armorType, Item.Properties properties) {
        super(MekanismArmorMaterials.MEKASUIT, armorType, IModuleHelper.INSTANCE.applyModuleContainerProperties(
              properties.rarity(Rarity.EPIC).setNoCombineRepair().stacksTo(1)
        ));
        this.armorType = armorType;
        switch (armorType) {
            case HELMET -> {
                fluidTankSpecs.add(GenericTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitNutritionalTransferRate, MekanismConfig.gear.mekaSuitNutritionalMaxStorage,
                      fluid -> fluid.is(MekanismFluids.NUTRITIONAL_PASTE), itemType -> hasModule(itemType, MekanismModules.NUTRITIONAL_INJECTION_UNIT)));
                absorption = 0.15F;
                laserDissipation = 0.15;
                laserRefraction = 0.2;
            }
            case CHESTPLATE -> {
                chemicalTankSpecs.add(GenericTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitJetpackTransferRate, itemAccess -> {
                    //Note: We intentionally don't require the module to be enabled for purposes of calculating capacity
                    ItemResource itemType = itemAccess.getResource();
                    if (itemType.isEmpty()) {
                        return 0;
                    }
                    IModule<ModuleJetpackUnit> module = IModuleHelper.INSTANCE.getModule(itemType, MekanismModules.JETPACK_UNIT);
                    return module != null ? MekanismConfig.gear.mekaSuitJetpackMaxStorage.get() * module.getInstalledCount() : 0L;
                }, chemical -> chemical.is(MekanismChemicals.HYDROGEN), itemType -> hasModule(itemType, MekanismModules.JETPACK_UNIT)));
                absorption = 0.4F;
                laserDissipation = 0.3;
                laserRefraction = 0.4;
            }
            case LEGGINGS -> {
                absorption = 0.3F;
                laserDissipation = 0.1875;
                laserRefraction = 0.25;
            }
            case BOOTS -> {
                absorption = 0.15F;
                laserDissipation = 0.1125;
                laserRefraction = 0.15;
            }
            default -> throw new IllegalArgumentException("Unknown Equipment Slot Type");
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, T entity, @NotNull Consumer<Item> onBroken) {
        // safety check
        return 0;
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltipAdder);
        } else {
            ItemAccess itemAccess = ItemAccess.forStack(stack);
            StorageUtils.addStoredEnergy(itemAccess, tooltipAdder, true);
            if (!chemicalTankSpecs.isEmpty()) {
                StorageUtils.addStoredChemical(itemAccess, tooltipAdder);
            }
            if (!fluidTankSpecs.isEmpty()) {
                StorageUtils.addStoredFluid(itemAccess, tooltipAdder);
            }
            tooltipAdder.accept(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean isGazeDisguise(@NotNull ItemStack stack, @NotNull Player player, @Nullable LivingEntity entity) {
        return true;//only called on helmet slot, no need to check type
    }

    @Override
    public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return armorType == ArmorType.BOOTS;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        //Try to avoid replacing this item if there are any modules currently installed
        return super.isNotReplaceableByPickAction(stack, player, inventorySlot) || hasInstalledModules(stack);
    }

    @Override
    public int getEnchantmentLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
        //Enchantments in our data
        IModuleContainer container = ModuleHelper.get().getModuleContainerUnsafe(stack);
        int moduleLevel = container.getModuleEnchantmentLevel(enchantment);
        return Math.max(moduleLevel, super.getEnchantmentLevel(stack, enchantment));
    }

    @NotNull
    @Override
    public ItemEnchantments getAllEnchantments(@NotNull ItemStack stack, RegistryLookup<Enchantment> lookup) {
        ItemEnchantments enchantments = super.getAllEnchantments(stack, lookup);
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        if (container != null) {
            ItemEnchantments moduleEnchantments = container.moduleBasedEnchantments();
            if (enchantments.isEmpty()) {
                //Skip copying if there are no builtin enchantments
                return moduleEnchantments;
            } else if (!moduleEnchantments.isEmpty()) {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : moduleEnchantments.entrySet()) {
                    mutable.upgrade(entry.getKey(), entry.getIntValue());
                }
                return mutable.toImmutable();
            }
        }
        return enchantments;
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(item));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot != null && slot.getType() == Type.HUMANOID_ARMOR && entity instanceof Player player) {
            ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
            if (container != null) {
                for (Module<?> module : container.modules()) {
                    module.tick(container, stack, player);
                }
            }
        }
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        if (!chemicalTankSpecs.isEmpty()) {
            ContainerType.CHEMICAL.addDefaultCreators(eventBus, this, () -> {
                ChemicalTanksBuilder builder = ChemicalTanksBuilder.builder();
                for (GenericTankSpec<ChemicalResource> spec : chemicalTankSpecs) {
                    spec.addTank(builder, ComponentBackedChemicalTank::new);
                }
                return builder.build();
            }, MekanismConfig.gear);
        }
        if (!fluidTankSpecs.isEmpty()) {
            ContainerType.FLUID.addDefaultCreators(eventBus, this, () -> {
                FluidTanksBuilder builder = FluidTanksBuilder.builder();
                for (GenericTankSpec<FluidResource> spec : fluidTankSpecs) {
                    spec.addTank(builder, ComponentBackedFluidTank::new);
                }
                return builder.build();
            }, MekanismConfig.gear);
        }
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        //Note: The all our providers only expose the capabilities (both those via attachments and those here) if the required configs for initializing that capability are loaded
        event.registerItem(Capabilities.RADIATION_SHIELDING, (stack, _) -> {
            if (!MekanismConfig.gear.isLoaded() || !isModuleEnabled(stack, MekanismModules.RADIATION_SHIELDING_UNIT)) {
                return null;
            }
            return RadiationShieldingHandler.create(ItemHazmatSuitArmor.getShieldingByArmor(armorType));
        }, this);

        event.registerItem(Capabilities.LASER_DISSIPATION, (stack, _) -> {
            //Note: This doesn't rely on configs, so we can skip the gear loaded check
            return isModuleEnabled(stack, MekanismModules.LASER_DISSIPATION_UNIT) ? LaserDissipationHandler.create(laserDissipation, laserRefraction) : null;
        }, this);
    }

    public List<GenericTankSpec<ChemicalResource>> getChemicalTankSpecs() {
        return chemicalTankSpecsView;
    }

    public List<GenericTankSpec<FluidResource>> getFluidTankSpecs() {
        return fluidTankSpecsView;
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        //Note: We ignore radial modes as those are just for the Meka-Tool currently
        return slotType == armorType.getSlot() && getModules(stack).stream().anyMatch(IModule::handlesModeChange);
    }

    //TODO - 26.1 Elytra unit
    /*@Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        if (getType() == ArmorType.CHESTPLATE && !entity.isShiftKeyDown()) {
            //Don't allow elytra flight if the player is sneaking. This lets the player exit elytra flight early
            IModuleContainer container = moduleContainer(stack);
            if (container != null) {
                IModule<ModuleElytraUnit> elytra = container.getIfEnabled(MekanismModules.ELYTRA_UNIT);
                if (elytra != null && elytra.canUseEnergy(entity, stack, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get())) {
                    //If we can use the elytra, check if the jetpack unit is also installed, and if it is,
                    // only mark that we can use the elytra if the jetpack is not set to hover or if it is if it has no hydrogen stored
                    IModule<ModuleJetpackUnit> jetpack = container.getIfEnabled(MekanismModules.JETPACK_UNIT);
                    return jetpack == null || jetpack.getCustomInstance().mode() != JetpackMode.HOVER ||
                           StorageUtils.getContainedChemical(stack, MekanismChemicals.HYDROGEN).isEmpty();
                }
            }
        }
        return false;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        //Note: As canElytraFly is checked just before this we don't bother validating ahead of time we have the energy
        // or that we are the correct slot
        if (!entity.level().isClientSide()) {
            int nextFlightTicks = flightTicks + 1;
            if (nextFlightTicks % MekanismUtils.TICKS_PER_HALF_SECOND == 0) {
                if (nextFlightTicks % SharedConstants.TICKS_PER_SECOND == 0) {
                    IModule<ModuleElytraUnit> module = getEnabledModule(stack, MekanismModules.ELYTRA_UNIT);
                    if (module != null) {
                        module.useEnergy(entity, stack, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get());
                    }
                }
                entity.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }
        return true;
    }*/

    @Override
    public boolean canUseJetpack(ItemStack stack) {
        if (armorType == ArmorType.CHESTPLATE) {
            if (isModuleEnabled(stack, MekanismModules.JETPACK_UNIT)) {
                return ChemicalUtils.hasChemicalOfType(stack, MekanismChemicals.HYDROGEN);
            }
            return getModules(stack).stream().anyMatch(module -> module.isEnabled() && module.getUntypedData().isExclusive(ExclusiveFlag.OVERRIDE_JUMP.getMask()));
        }
        return false;
    }

    @Override
    public JetpackMode getJetpackMode(ItemStack stack) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(stack, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            return module.getCustomInstance().mode();
        }
        return JetpackMode.DISABLED;
    }

    @Override
    public double getJetpackThrust(ItemStack stack) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(stack, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            float thrustMultiplier = module.getCustomInstance().getThrustMultiplier();
            int neededGas = Mth.ceil(thrustMultiplier);
            //Note: We verified we have at least one mB of gas before we get to the point of getting the thrust,
            // so we only need to do extra validation if we need more than a single mB of hydrogen
            if (neededGas > 1) {
                long containedGas = StorageUtils.getContainedChemical(stack, MekanismChemicals.HYDROGEN);
                if (neededGas > containedGas) {
                    //If we don't have enough gas stored to go at the set thrust, scale down the thrust
                    // to be whatever gas we have remaining
                    thrustMultiplier = containedGas;
                }
            }
            return 0.15 * thrustMultiplier;
        }
        return 0;
    }

    @Override
    public void useJetpackFuel(ItemStack stack) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(stack, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            //TODO - 26.1: Change params passed to this method to get a better item access?
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack));
            if (handler != null) {
                int amount = Mth.ceil(module.getCustomInstance().getThrustMultiplier());
                try (Transaction transaction = Transaction.openRoot()) {
                    //TODO - 26.1: Validate we have enough fuel?
                    handler.extract(MekanismChemicals.HYDROGEN.asResource(), amount, transaction);
                    transaction.commit();
                }
            }
        }
    }

    /*TODO - 26.1: check that thse are handled by the item props
    @Override
    public int getDefense() {
        return getMaterial().value().getDefense(getType());
    }

    @Override
    public float getToughness() {
        return getMaterial().value().toughness();
    }*/

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        //Ignore NBT for energized items causing re-equip animations
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return oldStack.getItem() != newStack.getItem();
    }

    public static float getDamageAbsorbed(Player player, DamageSource source, float amount) {
        if (amount <= 0) {
            return 0;
        }
        float ratioAbsorbed = 0;
        List<FoundArmorDetails> armorDetails = new ArrayList<>();
        try (Transaction transaction = Transaction.openRoot()) {
            //Start by looping the armor, allowing modules to absorb damage if they can
            ResourceHandler<ItemResource> armorSlots = LivingEntityEquipmentWrapper.of(player, EquipmentSlot.Type.HUMANOID_ARMOR);
            for (int slot = 0, size = armorSlots.size(); slot < size; slot++) {
                ItemResource itemType = armorSlots.getResource(slot);
                if (!itemType.isEmpty() && itemType.value() instanceof ItemMekaSuitArmor armor) {
                    ItemAccess itemAccess = ItemAccess.forHandlerIndexStrict(armorSlots, slot);
                    IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(itemAccess);
                    if (energyHandler != null) {
                        FoundArmorDetails details = new FoundArmorDetails(energyHandler, armor.absorption);
                        armorDetails.add(details);
                        for (IModule<?> module : IModuleHelper.INSTANCE.getAllModules(itemType)) {
                            if (module.isEnabled()) {
                                ModuleDamageAbsorbInfo damageAbsorbInfo = getModuleDamageAbsorbInfo(module, source);
                                if (damageAbsorbInfo != null) {
                                    float absorption = damageAbsorbInfo.absorptionRatio().getAsFloat();
                                    ratioAbsorbed += absorbDamage(details.energyHandler, amount, absorption, ratioAbsorbed, damageAbsorbInfo.energyCost(), transaction);
                                    if (ratioAbsorbed >= 1) {
                                        //If we have fully absorbed the damage, stop checking/trying to absorb more
                                        break;
                                    }
                                }
                            }
                        }
                        if (ratioAbsorbed >= 1) {
                            //If we have fully absorbed the damage, stop checking/trying to absorb more
                            break;
                        }
                    }
                }
            }
            if (ratioAbsorbed < 1) {
                //If we haven't fully absorbed it check the individual pieces of armor for if they can absorb any
                Float absorbRatio = null;
                for (FoundArmorDetails details : armorDetails) {
                    if (absorbRatio == null) {
                        //If we haven't looked up yet if we can absorb the damage type and if we can't
                        // stop checking if the armor is able to
                        if (source.is(Tags.DamageTypes.IS_TECHNICAL) || !source.is(MekanismAPITags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED) && source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                            break;
                        }
                        // Next lookup the ratio at which we can absorb the given damage type from the data map
                        MekaSuitAbsorption absorptionData = IMekanismDataMapTypes.INSTANCE.getMekaSuitAbsorption(player.registryAccess(), source.typeHolder());
                        if (absorptionData == null) {
                            absorbRatio = MekanismConfig.gear.mekaSuitUnspecifiedDamageRatio.get();
                        } else {
                            absorbRatio = absorptionData.absorption();
                        }
                        if (absorbRatio == 0) {
                            //If the config or the data map specifies that the damage type shouldn't be blocked at all
                            // stop checking if the armor is able to
                            break;
                        }
                    }
                    float absorption = details.armorAbsorption * absorbRatio;
                    ratioAbsorbed += absorbDamage(details.energyHandler, amount, absorption, ratioAbsorbed, MekanismConfig.gear.mekaSuitEnergyUsageDamage, transaction);
                    if (ratioAbsorbed >= 1) {
                        //If we have fully absorbed the damage, stop checking/trying to absorb more
                        break;
                    }
                }
            }
            //Use energy/or enqueue usage for each piece as needed
            transaction.commit();
        }
        return Math.min(ratioAbsorbed, 1);
    }

    @Nullable
    private static <MODULE extends ICustomModule<MODULE>> ModuleDamageAbsorbInfo getModuleDamageAbsorbInfo(IModule<MODULE> module, DamageSource damageSource) {
        return module.getCustomInstance().getDamageAbsorbInfo(module, damageSource);
    }

    private static float absorbDamage(IStrictEnergyHandler energyHandler, float amount, float absorption, float currentAbsorbed, LongSupplier energyCost, TransactionContext transaction) {
        //Cap the amount that we can absorb to how much we have left to absorb
        absorption = Math.min(1 - currentAbsorbed, absorption);
        float toAbsorb = amount * absorption;
        if (toAbsorb > 0) {
            long usage = MathUtils.ceilToLong(energyCost.getAsLong() * toAbsorb);
            if (usage == 0L) {
                //No energy is actually needed to absorb the damage, either because of the config
                // or how small the amount to absorb is
                return absorption;
            }
            long energyUsed = EnergyUtils.extractManual(energyHandler, usage, transaction);
            if (energyUsed == usage) {
                //If we have more energy available than we need, return that we can absorb it all
                return absorption;
            } else if (energyUsed > 0) {
                //Otherwise, if we have energy available but not as much as needed to fully absorb it
                // then we calculate what ratio we are able to block
                float absorbedPercent = (float) (energyUsed / (double) usage);
                return absorption * absorbedPercent;
            }
        }
        return 0;
    }

    private record FoundArmorDetails(IStrictEnergyHandler energyHandler, float armorAbsorption) {
    }
}
