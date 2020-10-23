package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemPortableTeleporter extends ItemEnergized implements IFrequencyItem {

    public ItemPortableTeleporter(Properties properties) {
        super(MekanismConfig.gear.portableTeleporterChargeRate, MekanismConfig.gear.portableTeleporterMaxEnergy, properties.rarity(Rarity.RARE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        if (getFrequency(stack) != null) {
            tooltip.add(MekanismLang.FREQUENCY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getFrequency(stack).getKey()));
            tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, !getFrequency(stack).isPublic() ? MekanismLang.PRIVATE : MekanismLang.PUBLIC));
        }
        super.addInformation(stack, world, tooltip, flag);
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
                NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new PortableTeleporterContainer(i, inv, hand, stack)), buf -> {
                    buf.writeEnumValue(hand);
                    buf.writeItemStack(stack);
                });
            }
        } else {
            if (!world.isRemote) {
                SecurityUtils.displayNoAccess(player);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}