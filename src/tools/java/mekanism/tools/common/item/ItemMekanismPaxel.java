package mekanism.tools.common.item;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.IPaxelMaterial;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
public class ItemMekanismPaxel extends AxeItem implements IHasRepairType, IAttributeRefresher {

    private static final ToolAction PAXEL_DIG = ToolAction.get("paxel_dig");
    private static final Set<ToolAction> PAXEL_ACTIONS = Util.make(Collections.newSetFromMap(new IdentityHashMap<>()), actions -> {
        actions.add(PAXEL_DIG);
        actions.addAll(ToolActions.DEFAULT_PICKAXE_ACTIONS);
        actions.addAll(ToolActions.DEFAULT_SHOVEL_ACTIONS);
        actions.addAll(ToolActions.DEFAULT_AXE_ACTIONS);
    });

    private final IPaxelMaterial material;
    private final AttributeCache attributeCache;

    public ItemMekanismPaxel(MaterialCreator material, Item.Properties properties) {
        //TODO - 1.20.5: Figure this out
        super(Tiers.IRON, properties);
        //super(material, material.getPaxelDamage(), material.getPaxelAtkSpeed(), properties);
        this.material = material;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.paxelDamage, material.paxelAtkSpeed);
    }

    public ItemMekanismPaxel(VanillaPaxelMaterialCreator material, Item.Properties properties) {
        //TODO - 1.20.5: Figure this out
        super(Tiers.IRON, properties);
        //super(material.getVanillaTier(), material.getPaxelDamage(), material.getPaxelAtkSpeed(), properties);
        this.material = material;
        //Don't add the material's damage as a listener as the vanilla component is not configurable
        this.attributeCache = new AttributeCache(this, material.paxelDamage, material.paxelAtkSpeed);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    //TODO - 1.20.5: ??
    //@Override
    public float getAttackDamage() {
        return material.getPaxelDamage() + getTier().getAttackDamageBonus();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return PAXEL_ACTIONS.contains(action);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return state.is(ToolsTags.Blocks.MINEABLE_WITH_PAXEL) ? material.getPaxelEfficiency() : 1;
    }

    /**
     * {@inheritDoc}
     *
     * Merged version of {@link AxeItem#useOn(UseOnContext)} and {@link net.minecraft.world.item.ShovelItem#useOn(UseOnContext)}
     */
    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Attempt to use the paxel as an axe
        InteractionResult axeResult = super.useOn(context);
        if (axeResult != InteractionResult.PASS) {
            return axeResult;
        }

        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState blockstate = world.getBlockState(blockpos);
        BlockState resultToSet = null;
        //We cannot strip the item that was right-clicked, so attempt to use the paxel as a shovel
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        }
        BlockState foundResult = blockstate.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
        if (foundResult != null && world.isEmptyBlock(blockpos.above())) {
            //We can flatten the item as a shovel
            world.playSound(player, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            resultToSet = foundResult;
        } else if (blockstate.getBlock() instanceof CampfireBlock && blockstate.getValue(CampfireBlock.LIT)) {
            //We can use the paxel as a shovel to extinguish a campfire
            if (!world.isClientSide) {
                world.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, blockpos, 0);
            }
            CampfireBlock.dowse(player, world, blockpos, blockstate);
            resultToSet = blockstate.setValue(CampfireBlock.LIT, false);
        }
        if (resultToSet == null) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide) {
            ItemStack stack = context.getItemInHand();
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockpos, stack);
            }
            world.setBlock(blockpos, resultToSet, Block.UPDATE_ALL_IMMEDIATE);
            if (player != null) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public int getEnchantmentValue() {
        return material.getPaxelEnchantability();
    }

    @NotNull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairIngredient();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getPaxelMaxUses();
    }

    @NotNull
    @Override
    public ItemAttributeModifiers getAttributeModifiers(@NotNull ItemStack stack) {
        return attributeCache.get();
    }

    @Override
    public void addToBuilder(List<ItemAttributeModifiers.Entry> builder) {
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.ATTACK_DAMAGE,
              new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(), Operation.ADD_VALUE),
              EquipmentSlotGroup.MAINHAND
        ));
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.ATTACK_SPEED,
              new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", material.getPaxelAtkSpeed(), Operation.ADD_VALUE),
              EquipmentSlotGroup.MAINHAND
        ));
    }

    // Need to override both method as DiggerItem performs two different behaviors
    //TODO - 1.20.5: Figure this out
    /*@Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(ToolsTags.Blocks.MINEABLE_WITH_PAXEL) && TierSortingRegistry.isCorrectTierForDrops(getTier(), state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return this.isCorrectToolForDrops(state);
    }*/
}