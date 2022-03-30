package mekanism.common.item.block.machine;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemBlockSeismicVibrator extends ItemBlockTooltip<BlockTile<?, ?>> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockSeismicVibrator(BlockTile<?, ?> block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().stacksTo(1));
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.seismicVibrator());
    }

    @Override
    protected void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        AttributeEnergy attributeEnergy = Attribute.get(getBlock(), AttributeEnergy.class);
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(attributeEnergy::getStorage, BasicEnergyContainer.manualOnly,
              BasicEnergyContainer.alwaysTrue));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        //Ignore NBT for energized items causing re-equip animations
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return oldStack.getItem() != newStack.getItem();
    }
}