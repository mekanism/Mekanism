package mekanism.common.item.block;

import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.security.SecurityObject;
import mekanism.common.component.IComponentAware;
import mekanism.common.component.component.UpgradeAware;
import mekanism.common.component.component.UpgradeAware.UpgradeAmount;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.component.containers.energy.ComponentBackedEnergyContainer;
import mekanism.common.component.containers.energy.EnergyContainerBuilder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ItemBlockTooltip<BLOCK extends Block> extends ItemBlockMekanism<BLOCK> implements ICapabilityAware, IComponentAware {

    //TODO - 26.2: Re-evaluate what callers should even be using this, vs just using ItemBlockMekanism now that we moved the description elsewhere
    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @Override//TODO - 26.2: Should we move this impl into ItemBlockMekanism? Then the only thing this class would do other than the super one is handling energy caps, components, and security
    public void onDestroyed(ItemEntity item, DamageSource damageSource) {
        //Try to drop the inventory contents if we are a block item that persists our inventory
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (exposesEnergyCap()) {
            //Ignore NBT for energized items causing re-equip animations
            //TODO - 26.2: Only ignore the energy attachment?
            // return slotChanged || !ItemStack.matchesIgnoringComponents(oldStack, newStack, );
            return slotChanged || oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (exposesEnergyCap()) {
            //Ignore NBT for energized items causing block break reset
            //TODO - 26.2: Only ignore the energy attachment?
            return oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    protected Predicate<AutomationType> getEnergyCapInsertPredicate() {
        return ConstantPredicates.alwaysTrue();
    }

    protected boolean exposesEnergyCap() {
        return Attribute.has(getBlock(), AttributeEnergy.class);
    }

    protected IContainerCreator<IEnergyContainer, Long> getDefaultEnergyContainer() {
        BLOCK block = getBlock();
        AttributeEnergy attributeEnergy = Attribute.getOrThrow(block, AttributeEnergy.class);
        LongSupplier maxEnergy = attributeEnergy::getStorage;
        if (Attribute.has(block, AttributeUpgradeSupport.class)) {
            return EnergyContainerBuilder.creator(attachedAccess -> {
                //If our block supports energy upgrades, make a more dynamically updating cache for our item's max energy
                LongSupplier capacity = new UpgradeBasedUnsignedLongCache(attachedAccess, maxEnergy);
                return new ComponentBackedEnergyContainer(attachedAccess, BasicEnergyContainer.manualOnly, getEnergyCapInsertPredicate(),
                      capacity, () -> MekanismUtils.calculateUsage(capacity.getAsLong()));
            });
        }
        //If we don't support energy upgrades, our max energy isn't dependent on another attachment, we can safely clamp to the config values
        return EnergyContainerBuilder.basicCreator(BasicEnergyContainer.manualOnly, getEnergyCapInsertPredicate(), () -> MekanismUtils.calculateUsage(maxEnergy.getAsLong()), maxEnergy);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (Attribute.has(getBlock(), AttributeSecurity.class)) {
            event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
            event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
        }
    }

    @Override
    public void addComponents(IEventBus eventBus) {
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            //Only expose the capability the required configs are loaded and the item wants to
            IEventBus energyEventBus = exposesEnergyCap() ? eventBus : null;
            ContainerType.ENERGY.addDefaultCreators(energyEventBus, this, this::getDefaultEnergyContainer, MekanismConfig.storage, MekanismConfig.usage);
        }
    }

    private static class UpgradeBasedUnsignedLongCache implements LongSupplier {

        //TODO: Eventually fix this, ideally we want this to update the overall cached value if this changes because of the config
        // for how much energy a machine can store changes
        private final LongSupplier baseStorage;
        private final ItemAccess attachedAccess;
        private UpgradeAmount lastInstalled;
        private long value;

        private UpgradeBasedUnsignedLongCache(ItemAccess attachedAccess, LongSupplier baseStorage) {
            this.attachedAccess = attachedAccess;
            UpgradeAware upgradeAware = this.attachedAccess.getResource().getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            this.lastInstalled = upgradeAware.getUpgradeCount(UpgradeIds.ENERGY);
            this.baseStorage = baseStorage;
            this.value = this.lastInstalled.getMaxEnergy(this.baseStorage.getAsLong());
        }

        @Override
        public long getAsLong() {
            UpgradeAware upgradeAware = attachedAccess.getResource().getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            UpgradeAmount installed = upgradeAware.getUpgradeCount(UpgradeIds.ENERGY);
            if (!installed.equals(lastInstalled)) {
                lastInstalled = installed;
                value = this.lastInstalled.getMaxEnergy(baseStorage.getAsLong());
            }
            return value;
        }
    }
}
