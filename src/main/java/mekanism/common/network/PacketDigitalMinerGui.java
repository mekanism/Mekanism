package mekanism.common.network;

import mekanism.api.Coord4D;

//TODO: Fix
public class PacketDigitalMinerGui {

    private MinerGuiPacket packetType;
    private Coord4D coord4D;
    private int guiType;
    private int windowId = -1;
    private int index = -1;

    public PacketDigitalMinerGui(MinerGuiPacket type, Coord4D coord, int guiID, int extra, int extra2) {
        packetType = type;
        coord4D = coord;
        guiType = guiID;
        if (packetType == MinerGuiPacket.CLIENT) {
            windowId = extra;
        } else if (packetType == MinerGuiPacket.SERVER_INDEX) {
            index = extra;
        } else if (packetType == MinerGuiPacket.CLIENT_INDEX) {
            windowId = extra;
            index = extra2;
        }
    }

    /*public static void handle(PacketDigitalMinerGui message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            if (!player.world.isRemote) {
                World worldServer = ServerLifecycleHooks.getCurrentServer().getWorld(message.coord4D.dimension);
                if (message.coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner) {
                    openServerGui(message.packetType, message.guiType, worldServer, (ServerPlayerEntity) player, message.coord4D, message.index);
                }
            } else if (message.coord4D.getTileEntity(player.world) instanceof TileEntityDigitalMiner) {
                try {
                    if (message.packetType == MinerGuiPacket.CLIENT) {
                        Minecraft.getInstance().displayGuiScreen(getGui(message.packetType, message.guiType, player, player.world, message.coord4D.getPos(), -1));
                    } else if (message.packetType == MinerGuiPacket.CLIENT_INDEX) {
                        Minecraft.getInstance().displayGuiScreen(getGui(message.packetType, message.guiType, player, player.world, message.coord4D.getPos(), message.index));
                    }
                    player.openContainer.windowId = message.windowId;
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
            }
        }, player);
    }

    public static void encode(PacketDigitalMinerGui pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        pkt.coord4D.write(buf);
        buf.writeInt(pkt.guiType);
        if (pkt.packetType == MinerGuiPacket.CLIENT || pkt.packetType == MinerGuiPacket.CLIENT_INDEX) {
            buf.writeInt(pkt.windowId);
        }
        if (pkt.packetType == MinerGuiPacket.SERVER_INDEX || pkt.packetType == MinerGuiPacket.CLIENT_INDEX) {
            buf.writeInt(pkt.index);
        }
    }

    public static PacketDigitalMinerGui decode(PacketBuffer buf) {
        MinerGuiPacket packetType = buf.readEnumValue(MinerGuiPacket.class);
        Coord4D coord4D = Coord4D.read(buf);
        int guiType = buf.readInt();
        int windowId = -1;
        int index = -1;
        if (packetType == MinerGuiPacket.CLIENT || packetType == MinerGuiPacket.CLIENT_INDEX) {
            windowId = buf.readInt();
        }
        if (packetType == MinerGuiPacket.SERVER_INDEX || packetType == MinerGuiPacket.CLIENT_INDEX) {
            index = buf.readInt();
        }
        int extra = 0;
        int extra2 = 0;
        if (packetType == MinerGuiPacket.CLIENT) {
            extra = windowId;
        } else if (packetType == MinerGuiPacket.SERVER_INDEX) {
            extra = index;
        } else if (packetType == MinerGuiPacket.CLIENT_INDEX) {
            extra2 = index;
        }
        //TODO: Evaluate this
        return new PacketDigitalMinerGui(packetType, coord4D, guiType, extra, extra2);
    }

    public static void openServerGui(MinerGuiPacket t, int guiType, World world, ServerPlayerEntity playerMP, Coord4D obj, int i) {
        Container container;
        playerMP.closeContainer();
        switch (guiType) {
            case 0:
                container = new DigitalMinerConfigContainer(playerMP, (TileEntityMekanism) obj.getTileEntity(world));
                break;
            case 4:
                container = new DigitalMinerContainer(playerMP.inventory, (TileEntityDigitalMiner) obj.getTileEntity(world));
                break;
            case 5:
                container = new DigitalMinerConfigContainer(playerMP, (TileEntityMekanism) obj.getTileEntity(world));
                break;
//				case 1:
//				case 2:
//				case 3:
//				case 6:
            default:
                container = new FilterContainer<TileEntityDigitalMiner>(playerMP.inventory, (TileEntityMekanism) obj.getTileEntity(world));
                break;
        }

        playerMP.getNextWindowId();
        int window = playerMP.currentWindowId;
        if (t == MinerGuiPacket.SERVER) {
            Mekanism.packetHandler.sendTo(new PacketDigitalMinerGui(MinerGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
        } else if (t == MinerGuiPacket.SERVER_INDEX) {
            Mekanism.packetHandler.sendTo(new PacketDigitalMinerGui(MinerGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
        }
        playerMP.openContainer = container;
        playerMP.openContainer.windowId = window;
        playerMP.openContainer.addListener(playerMP);
        if (guiType == 0) {
            TileEntityDigitalMiner tile = (TileEntityDigitalMiner) obj.getTileEntity(world);
            for (PlayerEntity player : tile.playersUsing) {
                Mekanism.packetHandler.sendTo(new PacketTileEntity(obj, tile.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) player);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Screen getGui(MinerGuiPacket packetType, int type, PlayerEntity player, World world, BlockPos pos, int index) {
        if (type == 0) {
            return new GuiDigitalMinerConfig(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
        } else if (type == 4) {
            return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner) world.getTileEntity(pos));
        } else if (type == 5) {
            return new GuiMFilterSelect(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
        } else {
            if (packetType == MinerGuiPacket.CLIENT) {
                if (type == 1) {
                    return new GuiMItemStackFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                } else if (type == 2) {
                    return new GuiMOreDictFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                } else if (type == 3) {
                    return new GuiMMaterialFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                } else if (type == 6) {
                    return new GuiMModIDFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                }
            } else if (packetType == MinerGuiPacket.CLIENT_INDEX) {
                if (type == 1) {
                    return new GuiMItemStackFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos),
                          index);
                } else if (type == 2) {
                    return new GuiMOreDictFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos), index);
                } else if (type == 3) {
                    return new GuiMMaterialFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos), index);
                } else if (type == 6) {
                    return new GuiMModIDFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos), index);
                }
            }
        }
        return null;
    }*/

    public enum MinerGuiPacket {
        SERVER,
        CLIENT,
        SERVER_INDEX,
        CLIENT_INDEX
    }
}