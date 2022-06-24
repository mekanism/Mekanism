package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.security.item.ItemStackOwnerObject;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemPortableQIODashboard extends CapabilityItem implements IFrequencyItem, IGuiItem, IItemSustainedInventory {

    public ItemPortableQIODashboard(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void onDestroyed(@Nonnull ItemEntity item, @Nonnull DamageSource damageSource) {
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        MekanismAPI.getSecurityUtils().addSecurityTooltip(stack, tooltip);
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        return SecurityUtils.INSTANCE.claimOrOpenGui(world, player, hand, getContainerType()::tryOpenGui);
    }

    @Override
    public ContainerTypeRegistryObject<PortableQIODashboardContainer> getContainerType() {
        return MekanismContainerTypes.PORTABLE_QIO_DASHBOARD;
    }

    @Override
    public void setFrequency(ItemStack stack, Frequency frequency) {
        IFrequencyItem.super.setFrequency(stack, frequency);
        setColor(stack, frequency == null ? null : ((QIOFrequency) frequency).getColor());
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.QIO;
    }

    public EnumColor getColor(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COLOR, Tag.TAG_INT)) {
            return EnumColor.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.COLOR));
        }
        return null;
    }

    public void setColor(ItemStack stack, EnumColor color) {
        if (color == null) {
            ItemDataUtils.removeData(stack, NBTConstants.COLOR);
        } else {
            ItemDataUtils.setInt(stack, NBTConstants.COLOR, color.ordinal());
        }
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        capabilities.add(new ItemStackOwnerObject());
        super.gatherCapabilities(capabilities, stack, nbt);
    }
}
