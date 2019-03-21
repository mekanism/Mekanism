package mekanism.common.item;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IMekWrench;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.IItemNetwork;
import mekanism.common.base.ISideConfiguration;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@InterfaceList({
      @Interface(iface = "buildcraft.api.tools.IToolWrench", modid = MekanismHooks.BUILDCRAFT_MOD_ID),
      @Interface(iface = "cofh.api.item.IToolHammer", modid = MekanismHooks.COFH_API_MOD_ID)
})
public class ItemConfigurator extends ItemEnergized implements IMekWrench, IToolWrench, IItemNetwork, IToolHammer {

    public final int ENERGY_PER_CONFIGURE = 400;
    public final int ENERGY_PER_ITEM_DUMP = 8;

    private Random random = new Random();

    public ItemConfigurator() {
        super(60000);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);
        list.add(
              EnumColor.PINK + LangUtils.localize("gui.state") + ": " + getColor(getState(itemstack)) + getStateDisplay(
                    getState(itemstack)));
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
          float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            Block block = world.getBlockState(pos).getBlock();
            TileEntity tile = world.getTileEntity(pos);

            if (getState(stack).isConfigurating()) //Configurate
            {
                if (tile instanceof ISideConfiguration && ((ISideConfiguration) tile).getConfig()
                      .supports(getState(stack).getTransmission())) {
                    ISideConfiguration config = (ISideConfiguration) tile;
                    SideData initial = config.getConfig()
                          .getOutput(getState(stack).getTransmission(), side, config.getOrientation());

                    if (initial != TileComponentConfig.EMPTY) {
                        if (!player.isSneaking()) {
                            player.sendMessage(new TextComponentString(
                                  EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " " + getViewModeText(
                                        getState(stack).getTransmission()) + ": " + initial.color + initial.localize()
                                        + " (" + initial.color.getColoredName() + ")"));
                        } else {
                            if (getEnergy(stack) >= ENERGY_PER_CONFIGURE) {
                                if (SecurityUtils.canAccess(player, tile)) {
                                    setEnergy(stack, getEnergy(stack) - ENERGY_PER_CONFIGURE);
                                    MekanismUtils.incrementOutput(config, getState(stack).getTransmission(),
                                          MekanismUtils.getBaseOrientation(side, config.getOrientation()));
                                    SideData data = config.getConfig()
                                          .getOutput(getState(stack).getTransmission(), side, config.getOrientation());
                                    player.sendMessage(new TextComponentString(
                                          EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " "
                                                + getToggleModeText(getState(stack).getTransmission()) + ": "
                                                + data.color + data.localize() + " (" + data.color.getColoredName()
                                                + ")"));

                                    if (config instanceof TileEntityBasicBlock) {
                                        TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) config;
                                        Mekanism.packetHandler.sendToReceivers(
                                              new TileEntityMessage(Coord4D.get(tileEntity),
                                                    tileEntity.getNetworkedData(new TileNetworkList())),
                                              new Range4D(Coord4D.get(tileEntity)));
                                    }
                                } else {
                                    SecurityUtils.displayNoAccess(player);
                                }
                            }
                        }
                    }

                    return EnumActionResult.SUCCESS;
                } else if (CapabilityUtils.hasCapability(tile, Capabilities.CONFIGURABLE_CAPABILITY, side)) {
                    IConfigurable config = CapabilityUtils
                          .getCapability(tile, Capabilities.CONFIGURABLE_CAPABILITY, side);

                    if (SecurityUtils.canAccess(player, tile)) {
                        if (player.isSneaking()) {
                            return config.onSneakRightClick(player, side);
                        } else {
                            return config.onRightClick(player, side);
                        }
                    } else {
                        SecurityUtils.displayNoAccess(player);

                        return EnumActionResult.SUCCESS;
                    }
                }
            } else if (getState(stack) == ConfiguratorMode.EMPTY) //Empty
            {
                if (tile instanceof TileEntityContainerBlock) {
                    IInventory inv = (IInventory) tile;

                    if (SecurityUtils.canAccess(player, tile)) {
                        for (int i = 0; i < inv.getSizeInventory(); i++) {
                            ItemStack slotStack = inv.getStackInSlot(i);

                            if (!slotStack.isEmpty()) {
                                if (getEnergy(stack) < ENERGY_PER_ITEM_DUMP) {
                                    break;
                                }

                                float xRandom = random.nextFloat() * 0.8F + 0.1F;
                                float yRandom = random.nextFloat() * 0.8F + 0.1F;
                                float zRandom = random.nextFloat() * 0.8F + 0.1F;

                                EntityItem item = new EntityItem(world, pos.getX() + xRandom, pos.getY() + yRandom,
                                      pos.getZ() + zRandom, slotStack.copy());

                                if (slotStack.hasTagCompound()) {
                                    item.getItem().setTagCompound(slotStack.getTagCompound().copy());
                                }

                                float k = 0.05F;
                                item.motionX = random.nextGaussian() * k;
                                item.motionY = random.nextGaussian() * k + 0.2F;
                                item.motionZ = random.nextGaussian() * k;
                                world.spawnEntity(item);

                                inv.setInventorySlotContents(i, ItemStack.EMPTY);
                                setEnergy(stack, getEnergy(stack) - ENERGY_PER_ITEM_DUMP);
                            }
                        }

                        return EnumActionResult.SUCCESS;
                    } else {
                        SecurityUtils.displayNoAccess(player);
                        return EnumActionResult.FAIL;
                    }
                }
            } else if (getState(stack) == ConfiguratorMode.ROTATE) //Rotate
            {
                EnumFacing[] rotations = block.getValidRotations(world, pos);

                if (rotations != null && rotations.length > 0) {
                    List<EnumFacing> l = Arrays.asList(block.getValidRotations(world, pos));

                    if (!player.isSneaking() && l.contains(side)) {
                        block.rotateBlock(world, pos, side);
                    } else if (player.isSneaking() && l.contains(side.getOpposite())) {
                        block.rotateBlock(world, pos, side.getOpposite());
                    }
                }

                return EnumActionResult.SUCCESS;
            } else if (getState(stack) == ConfiguratorMode.WRENCH) //Wrench
            {
                return EnumActionResult.PASS;
            }
        }

        return EnumActionResult.PASS;
    }

    public String getViewModeText(TransmissionType type) {
        String base = LangUtils.localize("tooltip.configurator.viewMode");
        return String.format(base, type.localize().toLowerCase(Locale.ROOT));
    }

    public String getToggleModeText(TransmissionType type) {
        String base = LangUtils.localize("tooltip.configurator.toggleMode");
        return String.format(base, type.localize());
    }

    public String getStateDisplay(ConfiguratorMode mode) {
        return mode.getName();
    }

    public EnumColor getColor(ConfiguratorMode mode) {
        return mode.getColor();
    }

    public void setState(ItemStack itemstack, ConfiguratorMode state) {
        ItemDataUtils.setInt(itemstack, "state", state.ordinal());
    }

    public ConfiguratorMode getState(ItemStack itemstack) {
        return ConfiguratorMode.values()[ItemDataUtils.getInt(itemstack, "state")];
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        return canUseWrench(wrench, player, rayTrace.getBlockPos());
    }

    @Override
    @Method(modid = MekanismHooks.BUILDCRAFT_MOD_ID)
    public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
    }

    @Override
    public boolean canUseWrench(ItemStack stack, EntityPlayer player, BlockPos pos) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    /*cofh IToolHammer */
    @Override
    public boolean isUsable(ItemStack stack, EntityLivingBase user, BlockPos pos) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public boolean isUsable(ItemStack stack, EntityLivingBase user, Entity entity) {
        return getState(stack) == ConfiguratorMode.WRENCH;
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {
    }
    /*end cofh IToolHammer */

    @Override
    public void handlePacketData(ItemStack stack, ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int state = dataStream.readInt();
            setState(stack, ConfiguratorMode.values()[state]);
        }
    }

    public enum ConfiguratorMode {
        CONFIGURATE_ITEMS("configurate", "(" + TransmissionType.ITEM.localize() + ")", EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_FLUIDS("configurate", "(" + TransmissionType.FLUID.localize() + ")", EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_GASES("configurate", "(" + TransmissionType.GAS.localize() + ")", EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_ENERGY("configurate", "(" + TransmissionType.ENERGY.localize() + ")", EnumColor.BRIGHT_GREEN, true),
        CONFIGURATE_HEAT("configurate", "(" + TransmissionType.HEAT.localize() + ")", EnumColor.BRIGHT_GREEN, true),
        EMPTY("empty", "", EnumColor.DARK_RED, false),
        ROTATE("rotate", "", EnumColor.YELLOW, false),
        WRENCH("wrench", "", EnumColor.PINK, false);

        private String name;
        private String info;
        private EnumColor color;
        private boolean configurating;

        ConfiguratorMode(String s, String s1, EnumColor c, boolean b) {
            name = s;
            info = s1;
            color = c;
            configurating = b;
        }

        public String getName() {
            return LangUtils.localize("tooltip.configurator." + name) + " " + info;
        }

        public EnumColor getColor() {
            return color;
        }

        public boolean isConfigurating() {
            return configurating;
        }

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
    }
}
