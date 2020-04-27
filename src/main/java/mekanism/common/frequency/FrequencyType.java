package mekanism.common.frequency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.NBTConstants;
import mekanism.common.Mekanism;
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
          () -> Mekanism.teleporterFrequencies,
          IdentitySerializer.NAME);
    public static final FrequencyType<InventoryFrequency> INVENTORY = register("Inventory",
        (key, uuid) -> new InventoryFrequency((String) key, uuid),
        (tag, fromUpdate) -> new InventoryFrequency(tag, fromUpdate),
        (packet) -> new InventoryFrequency(packet),
        () -> Mekanism.entangloporterFrequencies,
        IdentitySerializer.NAME);
    public static final FrequencyType<SecurityFrequency> SECURITY = register("Security",
        (key, uuid) -> new SecurityFrequency(uuid),
        (tag, fromUpdate) -> new SecurityFrequency(tag, fromUpdate),
        (packet) -> new SecurityFrequency(packet),
        () -> Mekanism.securityFrequencies,
        IdentitySerializer.UUID);
    public static final FrequencyType<QIOFrequency> QIO = register("QIO",
        (key, uuid) -> new QIOFrequency((String) key, uuid),
        (tag, fromUpdate) -> new QIOFrequency(tag, fromUpdate),
        (packet) -> new QIOFrequency(packet),
        () -> Mekanism.qioFrequencies,
        IdentitySerializer.NAME);

    private String name;
    private BiFunction<Object, UUID, FREQ> creationFunction;
    private BiFunction<CompoundNBT, Boolean, FREQ> nbtCreationFunction;
    private Function<PacketBuffer, FREQ> packetCreationFunction;
    private Supplier<FrequencyManagerWrapper<FREQ>> managerSupplier;
    private IdentitySerializer identitySerializer;

    private static <FREQ extends Frequency> FrequencyType<FREQ> register(String name, BiFunction<Object, UUID, FREQ> creationFunction,
          BiFunction<CompoundNBT, Boolean, FREQ> nbtCreationFunction, Function<PacketBuffer, FREQ> packetCreationFunction,
          Supplier<FrequencyManagerWrapper<FREQ>> managerSupplier, IdentitySerializer identitySerializer) {
        FrequencyType<FREQ> type = new FrequencyType<>(name, creationFunction, nbtCreationFunction, packetCreationFunction, managerSupplier, identitySerializer);
        registryMap.put(name, type);
        return type;
    }

    private FrequencyType(String name, BiFunction<Object, UUID, FREQ> creationFunction, BiFunction<CompoundNBT, Boolean, FREQ> nbtCreationFunction,
        Function<PacketBuffer, FREQ> packetCreationFunction, Supplier<FrequencyManagerWrapper<FREQ>> managerSupplier, IdentitySerializer identitySerializer) {
        this.name = name;
        this.creationFunction = creationFunction;
        this.nbtCreationFunction = nbtCreationFunction;
        this.packetCreationFunction = packetCreationFunction;
        this.managerSupplier = managerSupplier;
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

    public FrequencyManagerWrapper<FREQ> getWrapperManager() {
        return managerSupplier.get();
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
}