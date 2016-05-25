package buildcraft.api.core;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Identifies an in world tile entity or movable entity. Feel free to extend this class if your tile entity contains
 * multiple identifiable {@link IPowerConnection}. Just make sure to register it with
 * {@link #registerType(String, Supplier)} to make loading work.
 * <p>
 * Created on 1 Jan 2016 by AlexIIL */
public abstract class Identifier {
    private static Map<String, Supplier<Identifier>> registeredIdentifiers;

    static {
        registeredIdentifiers.put("mincraft:tile", new Supplier<Identifier>() {
            @Override
            public Identifier get() {
                return new Tile(0, null);
            }
        });
        registeredIdentifiers.put("mincraft:entity", new Supplier<Identifier>() {
            @Override
            public Identifier get() {
                return new MovableEntity(0, null);
            }
        });
    }

    protected final int dimId;

    public static void registerType(String type, Supplier<Identifier> constructor) {
        if (constructor == null) throw new NullPointerException("constructor");
        if (constructor.get() == null) throw new NullPointerException("constructor.get()");
        registeredIdentifiers.put(type, constructor);
    }

    public static Identifier getFor(Object obj) {
        if (obj == null) throw new NullPointerException("obj");
        if (obj instanceof TileEntity) {
            return Tile.create((TileEntity) obj);
        }
        if (obj instanceof Entity) {
            return MovableEntity.create((Entity) obj);
        }
        if (obj instanceof NBTTagCompound) {
            NBTTagCompound nbt = (NBTTagCompound) obj;
            String type = nbt.getString("type");
            if (!registeredIdentifiers.containsKey(type)) return null;
            return registeredIdentifiers.get(type).get().load(nbt.getCompoundTag("data"));
        }
        throw new IllegalArgumentException("Unknown type " + obj.getClass());
    }

    public static NBTTagCompound save(Identifier ident) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", ident.typeId());
        tag.setTag("data", ident.save());
        return tag;
    }

    private Identifier(int dimId) {
        this.dimId = dimId;
    }

    public abstract Object getByIdentifier(MinecraftServer server);

    public abstract boolean isLoaded(MinecraftServer server);

    protected abstract Identifier load(NBTTagCompound tag);

    protected abstract NBTTagCompound save();

    public abstract String typeId();

    @SideOnly(Side.CLIENT)
    public abstract Object getByIdentifierClient();

    @SideOnly(Side.CLIENT)
    public abstract boolean isLoadedClient();

    public static class Tile extends Identifier {
        private final BlockPos pos;

        public static Tile create(TileEntity tile) {
            if (tile.getWorld() == null) throw new NullPointerException("tile.getWorld() for the tile " + tile.getClass());
            if (tile.getPos() == null) throw new NullPointerException("tile.getPos() for the tile " + tile.getClass());
            return new Tile(tile.getWorld().provider.getDimension(), tile.getPos());
        }

        private Tile(int dimId, BlockPos pos) {
            super(dimId);
            this.pos = pos;
        }

        @Override
        public Tile load(NBTTagCompound tag) {
            int dimId = tag.getInteger("dimId");
            int[] arr = tag.getIntArray("pos");
            if (arr.length == 0) return null;
            return new Tile(dimId, new BlockPos(arr[0], arr[1], arr[2]));
        }

        @Override
        protected NBTTagCompound save() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("dimId", dimId);
            tag.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            return tag;
        }

        @Override
        public String typeId() {
            return "minecraft:tile";
        }

        @Override
        public TileEntity getByIdentifier(MinecraftServer server) {
            World world = server.worldServerForDimension(dimId);
            if (world == null) return null;
            return world.getTileEntity(pos);
        }

        @Override
        public boolean isLoaded(MinecraftServer server) {
            World world = server.worldServerForDimension(dimId);
            if (world == null) return false;
            return world.getTileEntity(pos) != null;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public TileEntity getByIdentifierClient() {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null || world.provider.getDimension() != dimId) return null;
            return world.getTileEntity(pos);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean isLoadedClient() {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null || world.provider.getDimension() != dimId) return false;
            return world.getTileEntity(pos) != null;
        }

    }

    public static class MovableEntity extends Identifier {
        private final UUID uniqueId;

        public static MovableEntity create(Entity ent) {
            if (ent.getEntityWorld() == null) throw new NullPointerException("ent.getEntityWorld() for the entity " + ent.getClass());
            if (ent.getPersistentID() == null) throw new NullPointerException("ent.getPersistentID() for the entity " + ent.getClass());
            return new MovableEntity(ent.getEntityWorld().provider.getDimension(), ent.getPersistentID());
        }

        private MovableEntity(int dimId, UUID uniqueId) {
            super(dimId);
            this.uniqueId = uniqueId;
        }

        private Entity get(World world) {
            for (Entity entity : world.loadedEntityList) {
                if (entity.getPersistentID().equals(uniqueId)) {
                    return entity;
                }
            }
            return null;
        }

        @Override
        public Entity getByIdentifier(MinecraftServer server) {
            World world = server.worldServerForDimension(dimId);
            if (world == null || world.provider.getDimension() != dimId) return null;
            return get(world);
        }

        @Override
        public boolean isLoaded(MinecraftServer server) {
            World world = server.worldServerForDimension(dimId);
            if (world == null || world.provider.getDimension() != dimId) return false;
            return get(world) != null;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public Entity getByIdentifierClient() {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null || world.provider.getDimension() != dimId) return null;
            return get(world);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean isLoadedClient() {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null || world.provider.getDimension() != dimId) return false;
            return get(world) != null;
        }

        @Override
        protected MovableEntity load(NBTTagCompound tag) {
            int dimId = tag.getInteger("dimId");
            UUID uuid = UUID.fromString(tag.getString("uuid"));
            return new MovableEntity(dimId, uuid);
        }

        @Override
        protected NBTTagCompound save() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("dimId", dimId);
            tag.setString("uuid", uniqueId.toString());
            return tag;
        }

        @Override
        public String typeId() {
            return "minecraft:entity";
        }
    }
}
