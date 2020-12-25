package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkHooks;

//TODO - 10.1: Decide if we should display the "has inventory" for if there is stuff stored in the crafting windows
public class ItemPortableQIODashboard extends Item implements IFrequencyItem, IGuiItem, IItemSustainedInventory {

    public ItemPortableQIODashboard(Properties properties) {
        super(properties.maxStackSize(1).rarity(Rarity.RARE));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (getOwnerUUID(stack) == null) {
            if (!world.isRemote) {
                SecurityUtils.claimItem(player, stack);
            }
        } else if (SecurityUtils.canAccess(player, stack)) {
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) player, getContainerProvider(stack, hand), buf -> {
                    buf.writeEnumValue(hand);
                    buf.writeItemStack(stack);
                });
            }
        } else {
            SecurityUtils.displayNoAccess(player);
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public INamedContainerProvider getContainerProvider(ItemStack stack, Hand hand) {
        return new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new PortableQIODashboardContainer(i, inv, hand, stack));
    }

    @Override
    public void setFrequency(ItemStack stack, Frequency frequency) {
        IFrequencyItem.super.setFrequency(stack, frequency);
        setColor(stack, frequency != null ? ((QIOFrequency) frequency).getColor() : null);
    }

    public EnumColor getColor(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COLOR, NBT.TAG_INT)) {
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
}
