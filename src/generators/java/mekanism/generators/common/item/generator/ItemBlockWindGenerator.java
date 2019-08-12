package mekanism.generators.common.item.generator;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.common.block.BlockWindGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemBlockWindGenerator extends ItemBlockAdvancedTooltip<BlockWindGenerator> implements IItemEnergized, IItemSustainedInventory, ISecurityItem {

    public ItemBlockWindGenerator(BlockWindGenerator block) {
        super(block, new Item.Properties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack))));
        tooltip.add(TextComponentUtil.build(EnumColor.GREY, Translation.of("mekanism.gui.security"), ": ", SecurityUtils.getSecurity(itemstack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(TextComponentUtil.build(EnumColor.RED, "(", Translation.of("mekanism.gui.overridden"), ")"));
        }
        tooltip.add(TextComponentUtil.build(EnumColor.BRIGHT_GREEN, Translation.of("mekanism.tooltip.storedEnergy"), ": ", EnumColor.GREY,
              EnergyDisplay.of(getEnergy(itemstack), getMaxEnergy(itemstack))));
        ListNBT inventory = getInventory(itemstack);
        tooltip.add(TextComponentUtil.build(EnumColor.AQUA, Translation.of("mekanism.tooltip.inventory"), ": ", EnumColor.GREY,
              BooleanStateDisplay.YesNo.of(inventory != null && !inventory.isEmpty())));
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Block block = world.getBlockState(pos).getBlock();
        if (!block.isReplaceable(world, pos)) {
            return false;
        }
        for (int yPos = 1; yPos <= 4; yPos++) {
            BlockPos toCheck = pos.up(yPos);
            if (World.isValid(toCheck) || !world.getBlockState(toCheck).getBlock().isReplaceable(world, toCheck)) {
                //If there is not enough room, fail
                return false;
            }
        }
        return super.placeBlock(context, state);
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof ItemBlockWindGenerator) {
            return MekanismUtils.getMaxEnergy(itemStack, ((ItemBlockWindGenerator) item).getBlock().getStorage());
        }
        return 0;
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return true;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, new ForgeEnergyItemWrapper());
    }
}