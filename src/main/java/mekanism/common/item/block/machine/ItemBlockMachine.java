package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
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
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class ItemBlockMachine extends ItemBlockTooltip<BlockTile<?, ?>> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockMachine(BlockTile<?, ?> block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    public ItemBlockMachine(BlockTile<?, ?> block, Supplier<Callable<ItemStackTileEntityRenderer>> renderer) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1).setISTER(renderer));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        if (Attribute.has(getBlock(), AttributeSecurity.class)) {
            tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(stack, Dist.CLIENT)));
            if (SecurityUtils.isOverridden(stack, Dist.CLIENT)) {
                tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
            }
        }
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }
        //TODO: Make this support "multiple" tanks, and probably expose the tank via capabilities
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, fluidStack.getAmount()));
        }
        if (Attribute.has(getBlock(), AttributeInventory.class)) {
            tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
        }
        if (Attribute.has(getBlock(), AttributeUpgradeSupport.class) && ItemDataUtils.hasData(stack, NBTConstants.UPGRADES, NBT.TAG_LIST)) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(stack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
            }
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        if (Attribute.has(getBlock(), AttributeEnergy.class)) {
            FloatingLong maxEnergy = MekanismUtils.getMaxEnergy(stack, Attribute.get(getBlock(), AttributeEnergy.class).getStorage());
            return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> maxEnergy, BasicEnergyContainer.notExternal, BasicEnergyContainer.alwaysTrue));
        }
        return super.initCapabilities(stack, nbt);
    }
}