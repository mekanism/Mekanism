package mekanism.common.item.block;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.ITier;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockMekanism<BLOCK extends Block> extends BlockItem implements ICapabilityAware, IAttachmentAware {

    @NotNull
    private final BLOCK block;

    public ItemBlockMekanism(@NotNull BLOCK block, Item.Properties properties) {
        super(block, properties);
        this.block = block;
    }

    @NotNull
    @Override
    public BLOCK getBlock() {
        return block;
    }

    public ITier getTier() {
        return null;
    }

    public TextColor getTextColor(ItemStack stack) {
        ITier tier = getTier();
        return tier == null ? null : tier.getBaseTier().getColor();
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        TextColor color = getTextColor(stack);
        if (color == null) {
            return super.getName(stack);
        }
        return TextComponentUtil.build(color, super.getName(stack));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (exposesEnergyCap(oldStack) && exposesEnergyCap(newStack)) {
            //Ignore NBT for energized items causing re-equip animations
            return slotChanged || oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (exposesEnergyCap(oldStack) && exposesEnergyCap(newStack)) {
            //Ignore NBT for energized items causing block break reset
            return oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    protected Predicate<@NotNull AutomationType> getEnergyCapInsertPredicate() {
        return BasicEnergyContainer.alwaysTrue;
    }

    protected final boolean exposesEnergyCap(ItemStack stack) {
        //Only expose it if the block can't stack
        return Attribute.has(block, AttributeEnergy.class) && !stack.isStackable();
    }

    @Nullable
    protected IEnergyContainer getDefaultEnergyContainer(ItemStack stack) {
        AttributeEnergy attributeEnergy = Attribute.get(block, AttributeEnergy.class);
        if (attributeEnergy == null) {
            throw new IllegalStateException("Block " + RegistryUtils.getName(block) + " expected to have energy attribute");
        }
        FloatingLongSupplier maxEnergy = attributeEnergy::getStorage;
        if (Attribute.matches(block, AttributeUpgradeSupport.class, attribute -> attribute.supportedUpgrades().contains(Upgrade.ENERGY))) {
            //If our block supports energy upgrades, make a more dynamically updating cache for our item's max energy
            maxEnergy = new UpgradeBasedFloatingLongCache(stack, maxEnergy);
        }
        return RateLimitEnergyContainer.create(maxEnergy, BasicEnergyContainer.manualOnly, getEnergyCapInsertPredicate());
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (Attribute.has(block, AttributeSecurity.class)) {
            event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> stack.getData(MekanismAttachmentTypes.SECURITY), this);
            event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (stack, ctx) -> stack.getData(MekanismAttachmentTypes.SECURITY), this);
        }
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        if (Attribute.has(block, AttributeEnergy.class)) {
            //Only expose the capability if the stack can't stack and the required configs are loaded
            IEventBus energyEventBus = new ItemStack(this).isStackable() ? null : eventBus;
            ContainerType.ENERGY.addDefaultContainer(energyEventBus, this, this::getDefaultEnergyContainer, MekanismConfig.storage, MekanismConfig.usage);
        }
    }

    private static class UpgradeBasedFloatingLongCache implements FloatingLongSupplier {

        //TODO: Eventually fix this, ideally we want this to update the overall cached value if this changes because of the config
        // for how much energy a machine can store changes
        private final FloatingLongSupplier baseStorage;
        private final UpgradeAware upgradeAware;
        private int lastInstalled;
        private FloatingLong value;

        private UpgradeBasedFloatingLongCache(ItemStack stack, FloatingLongSupplier baseStorage) {
            this.upgradeAware = stack.getData(MekanismAttachmentTypes.UPGRADES);
            this.lastInstalled = this.upgradeAware.getUpgradeCount(Upgrade.ENERGY);
            this.baseStorage = baseStorage;
            this.value = MekanismUtils.getMaxEnergy(this.lastInstalled, this.baseStorage.get());
        }

        @NotNull
        @Override
        public FloatingLong get() {
            int installed = upgradeAware.getUpgradeCount(Upgrade.ENERGY);
            if (installed != lastInstalled) {
                lastInstalled = installed;
                value = MekanismUtils.getMaxEnergy(this.lastInstalled, baseStorage.get());
            }
            return value;
        }
    }
}