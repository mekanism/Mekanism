package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemBlockLaser extends ItemBlockTooltip<BlockTileModel<?, ?>> implements ISecurityItem {

    public ItemBlockLaser(BlockTileModel<?, ?> block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        ISecurityObject securityObject = SecurityUtils.wrapSecurityItem(stack);
        tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(securityObject, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(securityObject, Dist.CLIENT)) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
        }
        StorageUtils.addStoredEnergy(stack, tooltip, true);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        FloatingLong maxEnergy = Attribute.get(getBlock(), AttributeEnergy.class).getStorage();
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> maxEnergy, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
    }
}