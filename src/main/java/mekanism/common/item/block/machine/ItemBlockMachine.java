package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.machine.prefab.BlockBase;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class ItemBlockMachine extends ItemBlockAdvancedTooltip<BlockBase<?, ?>> implements IItemEnergized, IItemSustainedInventory, ISecurityItem {

    public ItemBlockMachine(BlockBase<?, ?> block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    public ItemBlockMachine(BlockBase<?, ?> block, Supplier<Callable<ItemStackTileEntityRenderer>> renderer) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1).setISTER(renderer));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(stack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(stack, Dist.CLIENT)) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
        }
        tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(getEnergy(stack), getMaxEnergy(stack))));
        //TODO: Should we make this support "multiple" tanks
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, fluidStack.getAmount()));
        }
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
        if (ItemDataUtils.hasData(stack, NBTConstants.UPGRADES, NBT.TAG_LIST)) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(stack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
            }
        }
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof ItemBlockMachine) {
            return MekanismUtils.getMaxEnergy(itemStack, Attribute.get(((ItemBlockMachine) item).getBlock(), AttributeEnergy.class).getStorage());
        }
        return 0;
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, new ForgeEnergyItemWrapper());
    }
}