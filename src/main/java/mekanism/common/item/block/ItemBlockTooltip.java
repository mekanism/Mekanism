package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class ItemBlockTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockMekanism<BLOCK> {

    private final boolean hasDetails;

    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        this(block, false, properties);
    }

    public ItemBlockTooltip(BLOCK block) {
        this(block, true, ItemDeferredRegister.getMekBaseProperties().stacksTo(1));
    }

    protected ItemBlockTooltip(BLOCK block, boolean hasDetails, Properties properties) {
        super(block, properties);
        this.hasDetails = hasDetails;
    }

    @Override
    public void onDestroyed(@Nonnull ItemEntity item, @Nonnull DamageSource damageSource) {
        //Try to drop the inventory contents if we are a block item that persists our inventory
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockPlaceContext context, @Nonnull BlockState state) {
        AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
        return (hasBounding == null || WorldUtils.areBlocksValidAndReplaceable(context.getLevel(), hasBounding.getPositions(context.getClickedPos(), state))) &&
               super.placeBlock(context, state);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(getBlock().getDescription().translate());
        } else if (hasDetails && MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addDetails(stack, world, tooltip, flag);
        } else {
            addStats(stack, world, tooltip, flag);
            if (hasDetails) {
                tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    protected void addStats(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
    }

    protected void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        //Note: Security and owner info gets skipped if the stack doesn't expose them
        MekanismAPI.getSecurityUtils().addSecurityTooltip(stack, tooltip);
        addTypeDetails(stack, world, tooltip, flag);
        //TODO: Make this support "multiple" tanks, and probably expose the tank via capabilities
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, TextUtils.format(fluidStack.getAmount())));
        }
        if (Attribute.has(getBlock(), AttributeInventory.class) && stack.getItem() instanceof IItemSustainedInventory inventory) {
            tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(inventory.hasInventory(stack))));
        }
        if (Attribute.has(getBlock(), AttributeUpgradeSupport.class)) {
            MekanismUtils.addUpgradesToTooltip(stack, tooltip);
        }
    }

    protected void addTypeDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        //Put this here so that energy cubes can skip rendering energy here
        if (exposesEnergyCap(stack)) {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }
    }
}