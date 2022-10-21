package mekanism.common.network.to_server;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.gear.config.ModuleIntegerData;
import mekanism.api.math.MathUtils;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

//TODO: Eventually it would be nice to make this more generic in terms of how it can sync module data so that we can support custom types
// though given the module tweaker screen doesn't currently have a way to support custom types it isn't that big a deal to make this support it yet either
public class PacketUpdateModuleSettings implements IMekanismPacket {

    public static PacketUpdateModuleSettings create(int slotId, ModuleData<?> moduleType, int dataIndex, ModuleConfigData<?> configData) {
        if (configData instanceof ModuleEnumData<?> enumData) {
            return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, ModuleDataType.ENUM, enumData.get().ordinal());
        }
        for (ModuleDataType type : ModuleDataType.VALUES) {
            if (type.typeMatches(configData)) {
                return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, type, configData.get());
            }
        }
        throw new IllegalArgumentException("Unknown config data type.");
    }

    private final ModuleData<?> moduleType;
    private final int slotId;
    private final int dataIndex;
    private final ModuleDataType dataType;
    private final Object value;

    private PacketUpdateModuleSettings(int slotId, ModuleData<?> moduleType, int dataIndex, ModuleDataType dataType, Object value) {
        this.slotId = slotId;
        this.moduleType = moduleType;
        this.dataIndex = dataIndex;
        this.dataType = dataType;
        this.value = value;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null && dataIndex >= 0 && value != null) {
            ItemStack stack = player.getInventory().getItem(slotId);
            if (!stack.isEmpty() && stack.getItem() instanceof IModuleContainerItem) {
                Module<?> module = ModuleHelper.INSTANCE.load(stack, moduleType);
                if (module != null) {
                    List<ModuleConfigItem<?>> configItems = module.getConfigItems();
                    if (dataIndex < configItems.size()) {
                        setValue(configItems.get(dataIndex));
                    }
                }
            }
        }
    }

    private <TYPE> void setValue(ModuleConfigItem<TYPE> moduleConfigItem) {
        ModuleConfigData<TYPE> configData = moduleConfigItem.getData();
        if (configData instanceof ModuleEnumData && dataType == ModuleDataType.ENUM) {
            moduleConfigItem.set((TYPE) MathUtils.getByIndexMod(((ModuleEnumData<?>) configData).getEnums(), (int) value));
        } else if (dataType.typeMatches(configData)) {
            //noinspection unchecked
            moduleConfigItem.set((TYPE) value);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slotId);
        buffer.writeRegistryId(MekanismAPI.moduleRegistry(), moduleType);
        buffer.writeVarInt(dataIndex);
        buffer.writeEnum(dataType);
        switch (dataType) {
            case BOOLEAN -> buffer.writeBoolean((boolean) value);
            //Don't convert to var int for colors as we often will use the full range
            case COLOR -> buffer.writeInt((int) value);
            case INTEGER, ENUM -> buffer.writeVarInt((int) value);
        }
    }

    public static PacketUpdateModuleSettings decode(FriendlyByteBuf buffer) {
        int slotId = buffer.readVarInt();
        ModuleData<?> moduleType = buffer.readRegistryIdSafe(ModuleData.class);
        int dataIndex = buffer.readVarInt();
        ModuleDataType dataType = buffer.readEnum(ModuleDataType.class);
        Object data = switch (dataType) {
            case BOOLEAN -> buffer.readBoolean();
            case COLOR -> buffer.readInt();
            case INTEGER, ENUM -> buffer.readVarInt();
        };
        return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, dataType, data);
    }

    private enum ModuleDataType {
        BOOLEAN(data -> data instanceof ModuleBooleanData),
        //Must be above integer so it uses the color type
        COLOR(data -> data instanceof ModuleColorData),
        INTEGER(data -> data instanceof ModuleIntegerData),
        ENUM(data -> data instanceof ModuleEnumData);

        //DO NOT MODIFY
        private static final ModuleDataType[] VALUES = values();

        private final Predicate<ModuleConfigData<?>> configDataPredicate;

        ModuleDataType(Predicate<ModuleConfigData<?>> configDataPredicate) {
            this.configDataPredicate = configDataPredicate;
        }

        public boolean typeMatches(ModuleConfigData<?> data) {
            return configDataPredicate.test(data);
        }
    }
}