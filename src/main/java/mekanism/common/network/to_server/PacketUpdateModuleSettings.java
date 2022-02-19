package mekanism.common.network.to_server;

import java.util.List;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.MathUtils;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

//TODO: Eventually it would be nice to make this more generic in terms of how it can sync module data so that we can support custom types
// though given the module tweaker screen doesn't currently have a way to support custom types it isn't that big a deal to make this support it yet either
public class PacketUpdateModuleSettings implements IMekanismPacket {

    public static PacketUpdateModuleSettings create(int slotId, ModuleData<?> moduleType, int dataIndex, ModuleConfigData<?> configData) {
        if (configData instanceof ModuleBooleanData) {
            return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, ModuleDataType.BOOLEAN, configData.get());
        } else if (configData instanceof ModuleEnumData) {
            return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, ModuleDataType.ENUM, ((ModuleEnumData<?>) configData).get().ordinal());
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
        PlayerEntity player = context.getSender();
        if (player != null && dataIndex >= 0 && value != null) {
            ItemStack stack = player.inventory.getItem(slotId);
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

    //Very dirty in terms of the unchecked casts but the various details are actually checked relatively accurately
    private <TYPE> void setValue(ModuleConfigItem<TYPE> moduleConfigItem) {
        ModuleConfigData<TYPE> configData = moduleConfigItem.getData();
        if (configData instanceof ModuleBooleanData && dataType == ModuleDataType.BOOLEAN) {
            ((ModuleConfigItem<Boolean>) moduleConfigItem).set((boolean) value);
        } else if (configData instanceof ModuleEnumData && dataType == ModuleDataType.ENUM) {
            moduleConfigItem.set((TYPE) MathUtils.getByIndexMod(((ModuleEnumData<?>) configData).getEnums(), (int) value));
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeVarInt(slotId);
        buffer.writeRegistryId(moduleType);
        buffer.writeVarInt(dataIndex);
        buffer.writeEnum(dataType);
        switch (dataType) {
            case BOOLEAN:
                buffer.writeBoolean((boolean) value);
                break;
            case ENUM:
                buffer.writeVarInt((int) value);
                break;
        }
    }

    public static PacketUpdateModuleSettings decode(PacketBuffer buffer) {
        int slotId = buffer.readVarInt();
        ModuleData<?> moduleType = buffer.readRegistryId();
        int dataIndex = buffer.readVarInt();
        ModuleDataType dataType = buffer.readEnum(ModuleDataType.class);
        Object data = null;
        switch (dataType) {
            case BOOLEAN:
                data = buffer.readBoolean();
                break;
            case ENUM:
                data = buffer.readVarInt();
                break;
        }
        return new PacketUpdateModuleSettings(slotId, moduleType, dataIndex, dataType, data);
    }

    private enum ModuleDataType {
        BOOLEAN,
        ENUM
    }
}