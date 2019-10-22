package mekanism.common.item;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IIncrementalEnum;
import mekanism.api.IMekWrench;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.base.IItemNetwork;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO COFH IToolHammer, BuildCraft IToolWrench
/*@InterfaceList({
      @Interface(iface = "buildcraft.api.tools.IToolWrench", modid = MekanismHooks.BUILDCRAFT_MOD_ID),
      @Interface(iface = "cofh.api.item.IToolHammer", modid = MekanismHooks.COFH_API_MOD_ID)
})*/
public class ItemConfigurator extends ItemEnergized implements IMekWrench, IItemNetwork {

    public final int ENERGY_PER_CONFIGURE = 400;
    public final int ENERGY_PER_ITEM_DUMP = 8;

    public ItemConfigurator() {
        super("configurator", 60000);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(itemstack, world, tooltip, flag);
        tooltip.add(TextComponentUtil.build(EnumColor.PINK, Translation.of("gui.mekanism.state"), ": ", getState(itemstack)));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            BlockPos pos = context.getPos();
            Direction side = context.getFace();
            Hand hand = context.getHand();
            ItemStack stack = player.getHeldItem(hand);
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            TileEntity tile = world.getTileEntity(pos);

            if (getState(stack).isConfigurating()) { //Configurate
                TransmissionType transmissionType = Objects.requireNonNull(getState(stack).getTransmission(), "Configurating state requires transmission type");
                if (tile instanceof ISideConfiguration && ((ISideConfiguration) tile).getConfig().supports(transmissionType)) {
                    ISideConfiguration config = (ISideConfiguration) tile;
                    ConfigInfo info = config.getConfig().getConfig(transmissionType);
                    if (info != null) {
                        RelativeSide relativeSide = RelativeSide.fromDirections(config.getOrientation(), side);
                        DataType dataType = info.getDataType(relativeSide);
                        if (!player.isSneaking()) {
                            player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                                  Translation.of("tooltip.mekanism.configurator.view_mode", TextComponentUtil.build(transmissionType)), ": ", dataType.getColor(), dataType,
                                  " (", dataType.getColor().getColoredName(), ")"));
                        } else {
                            if (getEnergy(stack) >= ENERGY_PER_CONFIGURE) {
                                if (SecurityUtils.canAccess(player, tile)) {
                                    setEnergy(stack, getEnergy(stack) - ENERGY_PER_CONFIGURE);
                                    dataType = info.incrementDataType(relativeSide);
                                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                                          Translation.of("tooltip.mekanism.configurator.toggle_mode", TextComponentUtil.build(transmissionType)), ": ",
                                          dataType.getColor(), dataType, " (", dataType.getColor().getColoredName(), ")"));
                                    if (config instanceof TileEntityMekanism) {
                                        Mekanism.packetHandler.sendUpdatePacket((TileEntityMekanism) config);
                                    }
                                } else {
                                    SecurityUtils.displayNoAccess(player);
                                }
                            }
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
                if (SecurityUtils.canAccess(player, tile)) {
                    return CapabilityUtils.getCapabilityHelper(tile, Capabilities.CONFIGURABLE_CAPABILITY, side).getIfPresentElse(config -> {
                              if (player.isSneaking()) {
                                  return config.onSneakRightClick(player, side);
                              }
                              return config.onRightClick(player, side);
                          },
                          ActionResultType.PASS
                    );
                } else {
                    SecurityUtils.displayNoAccess(player);
                    return ActionResultType.SUCCESS;
                }
            } else if (getState(stack) == ConfiguratorMode.EMPTY) { //Empty
                if (tile instanceof IMekanismInventory) {
                    IMekanismInventory inv = (IMekanismInventory) tile;
                    if (inv.hasInventory()) {
                        if (SecurityUtils.canAccess(player, tile)) {
                            //TODO: Switch this to items being handled by TileEntityMekanism, energy handled here (via lambdas?)
                            List<IInventorySlot> inventorySlots = inv.getInventorySlots(null);
                            for (IInventorySlot inventorySlot : inventorySlots) {
                                ItemStack slotStack = inventorySlot.getStack();
                                if (!slotStack.isEmpty()) {
                                    double configuratorEnergy = getEnergy(stack);
                                    if (configuratorEnergy < ENERGY_PER_ITEM_DUMP) {
                                        break;
                                    }
                                    Block.spawnAsEntity(world, pos, slotStack.copy());
                                    inventorySlot.setStack(ItemStack.EMPTY);
                                    setEnergy(stack, configuratorEnergy - ENERGY_PER_ITEM_DUMP);
                                }
                            }
                            return ActionResultType.SUCCESS;
                        } else {
                            SecurityUtils.displayNoAccess(player);
                            return ActionResultType.FAIL;
                        }
                    }
                }
            } else if (getState(stack) == ConfiguratorMode.ROTATE) { //Rotate
                Direction[] rotations = block.getValidRotations(state, world, pos);
                if (rotations != null && rotations.length > 0) {
                    List<Direction> l = Arrays.asList(rotations);
                    //TODO: Convert direction to Rotation
                    /*if (!player.isSneaking() && l.contains(side)) {
                        block.rotate(state, world, pos, side);
                    } else if (player.isSneaking() && l.contains(side.getOpposite())) {
                        block.rotate(state, world, pos, side.getOpposite());
                    }*/
                }
                return ActionResultType.SUCCESS;
            } else if (getState(stack) == ConfiguratorMode.WRENCH) { //Wrench
                return ActionResultType.PASS;
            }
        }
        return ActionResultType.PASS;
    }

    public EnumColor getColor(ConfiguratorMode mode) {
        return mode.getColor();
    }

    public void setState(ItemStack itemstack, ConfiguratorMode state) {
        ItemDataUtils.setInt(itemstack, "state", state.ordinal());
    }

    public ConfiguratorMode getState(ItemStack itemstack) {
        return EnumUtils.CONFIGURATOR_MODES[ItemDataUtils.getInt(itemstack, "state")];
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    //TODO: BuildCraft
    /*@Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public boolean canWrench(PlayerEntity player, Hand hand, ItemStack wrench, BlockRayTraceResult rayTrace) {
        return canUseWrench(wrench, player, rayTrace.getPos());
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public void wrenchUsed(PlayerEntity player, Hand hand, ItemStack wrench, BlockRayTraceResult rayTrace) {
    }*/

    @Override
    public boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    /*cofh IToolHammer */
    //TODO: COFH IToolHammer
    /*@Override
    public boolean isUsable(ItemStack stack, LivingEntity user, BlockPos pos) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public boolean isUsable(ItemStack stack, LivingEntity user, Entity entity) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public void toolUsed(ItemStack item, LivingEntity user, BlockPos pos) {
    }

    @Override
    public void toolUsed(ItemStack item, LivingEntity user, Entity entity) {
    }*/
    /*end cofh IToolHammer */

    @Override
    public void handlePacketData(IWorld world, ItemStack stack, PacketBuffer dataStream) {
        if (!world.isRemote()) {
            setState(stack, dataStream.readEnumValue(ConfiguratorMode.class));
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    public enum ConfiguratorMode implements IIncrementalEnum<ConfiguratorMode>, IHasTextComponent {
        CONFIGURATE_ITEMS("configurate", TransmissionType.ITEM, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_FLUIDS("configurate", TransmissionType.FLUID, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_GASES("configurate", TransmissionType.GAS, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_ENERGY("configurate", TransmissionType.ENERGY, EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_HEAT("configurate", TransmissionType.HEAT, EnumColor.BRIGHT_GREEN, true),
        EMPTY("empty", null, EnumColor.DARK_RED, false),
        ROTATE("rotate", null, EnumColor.YELLOW, false),
        WRENCH("wrench", null, EnumColor.PINK, false);

        private static ConfiguratorMode[] MODES = values();
        private String name;
        @Nullable
        private final TransmissionType transmissionType;
        private EnumColor color;
        private boolean configurating;

        ConfiguratorMode(String s, @Nullable TransmissionType s1, EnumColor c, boolean b) {
            name = s;
            transmissionType = s1;
            color = c;
            configurating = b;
        }

        @Override
        public ITextComponent getTextComponent() {
            if (this.transmissionType != null) {
                return TextComponentUtil.build(color, Translation.of("tooltip.mekanism.configurator." + name), " (", transmissionType, ")");
            }
            return TextComponentUtil.build(color, Translation.of("tooltip.mekanism.configurator." + name));
        }

        public EnumColor getColor() {
            return color;
        }

        public boolean isConfigurating() {
            return configurating;
        }

        @Nullable
        public TransmissionType getTransmission() {
            switch (this) {
                case CONFIGURATE_ITEMS:
                    return TransmissionType.ITEM;
                case CONFIGURATE_FLUIDS:
                    return TransmissionType.FLUID;
                case CONFIGURATE_GASES:
                    return TransmissionType.GAS;
                case CONFIGURATE_ENERGY:
                    return TransmissionType.ENERGY;
                case CONFIGURATE_HEAT:
                    return TransmissionType.HEAT;
                default:
                    return null;
            }
        }

        @Nonnull
        @Override
        public ConfiguratorMode byIndex(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }
}