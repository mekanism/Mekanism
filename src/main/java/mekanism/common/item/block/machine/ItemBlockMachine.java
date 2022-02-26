package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;

public class ItemBlockMachine extends ItemBlockTooltip<BlockTile<?, ?>> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockMachine(BlockTile<?, ?> block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().stacksTo(1));
    }

    @Override
    protected void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(MekanismUtils.tryGetClientPlayer(), getOwnerUUID(stack)).getTextComponent());
        if (Attribute.has(getBlock(), AttributeSecurity.class)) {
            ISecurityObject securityObject = SecurityUtils.wrapSecurityItem(stack);
            tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(securityObject, Dist.CLIENT)));
            if (SecurityUtils.isOverridden(securityObject, Dist.CLIENT)) {
                tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
        }
        addTypeDetails(stack, world, tooltip, flag);
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }
        //TODO: Make this support "multiple" tanks, and probably expose the tank via capabilities
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, TextUtils.format(fluidStack.getAmount())));
        }
        if (Attribute.has(getBlock(), AttributeInventory.class)) {
            tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
        }
        if (Attribute.has(getBlock(), AttributeUpgradeSupport.class)) {
            MekanismUtils.addUpgradesToTooltip(stack, tooltip);
        }
    }

    protected void addTypeDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            AttributeEnergy attributeEnergy = Attribute.get(getBlock(), AttributeEnergy.class);
            FloatingLongSupplier maxEnergy;
            if (Attribute.has(getBlock(), AttributeUpgradeSupport.class)) {
                //If our block supports upgrades, make a more dynamically updating cache for our item's max energy
                maxEnergy = new UpgradeBasedFloatingLongCache(stack, attributeEnergy::getStorage);
            } else {
                //Otherwise, just return that the max is what the base max is
                maxEnergy = attributeEnergy::getStorage;
            }
            return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(maxEnergy, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
        }
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            //Ignore NBT for energized items causing re-equip animations
            return oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            //Ignore NBT for energized items causing block break reset
            return oldStack.getItem() != newStack.getItem();
        }
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    private static class UpgradeBasedFloatingLongCache implements FloatingLongSupplier {

        private final ItemStack stack;
        //TODO: Eventually fix this, ideally we want this to update the overall cached value if this changes because of the config
        // for how much energy a machine can store changes
        private final FloatingLongSupplier baseStorage;
        @Nullable
        private CompoundTag lastNBT;
        private FloatingLong value;

        private UpgradeBasedFloatingLongCache(ItemStack stack, FloatingLongSupplier baseStorage) {
            this.stack = stack;
            if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_UPGRADE, Tag.TAG_COMPOUND)) {
                this.lastNBT = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE).copy();
            } else {
                this.lastNBT = null;
            }
            this.baseStorage = baseStorage;
            this.value = MekanismUtils.getMaxEnergy(this.stack, this.baseStorage.get());
        }

        @Nonnull
        @Override
        public FloatingLong get() {
            if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_UPGRADE, Tag.TAG_COMPOUND)) {
                CompoundTag upgrades = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE);
                if (lastNBT == null || !lastNBT.equals(upgrades)) {
                    lastNBT = upgrades.copy();
                    value = MekanismUtils.getMaxEnergy(stack, baseStorage.get());
                }
            } else if (lastNBT != null) {
                lastNBT = null;
                value = MekanismUtils.getMaxEnergy(stack, baseStorage.get());
            }
            return value;
        }
    }
}