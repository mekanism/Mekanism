package mekanism.common.frequency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import mekanism.api.NBTConstants;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.security.SecurityFrequency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class FrequencyType<FREQ extends Frequency> {
    private static Map<String, FrequencyType<?>> registryMap = new HashMap<>();

    public static final FrequencyType<TeleporterFrequency> TELEPORTER = register("Teleporter",
          (key, uuid) -> new TeleporterFrequency((String) key, uuid),
          (tag, fromUpdate) -> new TeleporterFrequency(tag, fromUpdate),
          (packet) -> new TeleporterFrequency(packet),
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE,
          IdentitySerializer.NAME);
    public static final FrequencyType<InventoryFrequency> INVENTORY = register("Inventory",
          (key, uuid) -> new InventoryFrequency((String) key, uuid),
          (tag, fromUpdate) -> new InventoryFrequency(tag, fromUpdate),
          (packet) -> new InventoryFrequency(packet),
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE,
          IdentitySerializer.NAME);
    public static final FrequencyType<SecurityFrequency> SECURITY = register("Security",
          (key, uuid) -> new SecurityFrequency(uuid),
          (tag, fromUpdate) -> new SecurityFrequency(tag, fromUpdate),
          (packet) -> new SecurityFrequency(packet),
          FrequencyManagerWrapper.Type.PUBLIC_ONLY,
          IdentitySerializer.UUID);
    public static final FrequencyType<QIOFrequency> QIO = register("QIO",
          (key, uuid) -> new QIOFrequency((String) key, uuid),
          (tag, fromUpdate) -> new QIOFrequency(tag, fromUpdate),
          (packet) -> new QIOFrequency(packet),
          FrequencyManagerWrapper.Type.PRIVATE_ONLY,
          IdentitySerializer.NAME);

    private String name;
    private BiFunction<Object, UUID, FREQ> creationFunction;
    private BiFunction<CompoundNBT, Boolean, FREQ> nbtCreationFunction;
    private Function<PacketBuffer, FREQ> packetCreationFunction;
    private IdentitySerializer identitySerializer;

    private FrequencyManagerWrapper<FREQ> managerWrapper;

    private static <FREQ extends Frequency> FrequencyType<FREQ> register(String name, BiFunction<Object, UUID, FREQ> creationFunction,
          BiFunction<CompoundNBT, Boolean, FREQ> nbtCreationFunction, Function<PacketBuffer, FREQ> packetCreationFunction,
          FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        FrequencyType<FREQ> type = new FrequencyType<>(name, creationFunction, nbtCreationFunction, packetCreationFunction, managerType, identitySerializer);
        registryMap.put(name, type);
        return type;
    }

    private FrequencyType(String name, BiFunction<Object, UUID, FREQ> creationFunction, BiFunction<CompoundNBT, Boolean, FREQ> nbtCreationFunction,
        Function<PacketBuffer, FREQ> packetCreationFunction, FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        this.name = name;
        this.creationFunction = creationFunction;
        this.nbtCreationFunction = nbtCreationFunction;
        this.packetCreationFunction = packetCreationFunction;
        this.managerWrapper = FrequencyManagerWrapper.create(this, managerType);
        this.identitySerializer = identitySerializer;
    }

    public String getName() {
        return name;
    }

    public FREQ create(CompoundNBT tag, boolean fromUpdate) {
        return nbtCreationFunction.apply(tag, fromUpdate);
    }

    public FREQ create(Object key, UUID ownerUUID) {
        return creationFunction.apply(key, ownerUUID);
    }

    public FREQ create(PacketBuffer packet) {
        return packetCreationFunction.apply(packet);
    }

    public FrequencyManagerWrapper<FREQ> getManagerWrapper() {
        return managerWrapper;
    }

    public FrequencyManager<FREQ> getManager(UUID owner) {
        return owner == null ? getManagerWrapper().getPublicManager() : getManagerWrapper().getPrivateManager(owner);
    }

    public FrequencyManager<FREQ> getFrequencyManager(FREQ freq) {
        return freq.isPrivate() ? getManagerWrapper().getPrivateManager(freq.ownerUUID) : getManagerWrapper().getPublicManager();
    }

    public IdentitySerializer getKey() {
        return identitySerializer;
    }

    public void write(PacketBuffer buf) {
        buf.writeString(name);
    }

    public static FrequencyType<?> load(PacketBuffer buf) {
        return registryMap.get(buf.readString());
    }

    public static FrequencyType<?> load(CompoundNBT tag) {
        return registryMap.get(tag.getString(NBTConstants.TYPE));
    }

    public static void clear() {
        registryMap.values().forEach(type -> type.managerWrapper.clear());
    }
}