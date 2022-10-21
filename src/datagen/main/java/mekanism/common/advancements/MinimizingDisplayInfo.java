package mekanism.common.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinimizingDisplayInfo extends DisplayInfo {

    public MinimizingDisplayInfo(ItemStack icon, Component title, Component description, @Nullable ResourceLocation background, FrameType frame, boolean showToast,
          boolean announceChat, boolean hidden) {
        super(icon, title, description, background, frame, showToast, announceChat, hidden);
    }

    @NotNull
    @Override
    public JsonElement serializeToJson() {
        JsonObject json = super.serializeToJson().getAsJsonObject();
        //Remove any values that are the same as their default value in order to further minimize the json
        if (getFrame() == FrameType.TASK) {
            json.remove(DataGenJsonConstants.FRAME);
        }
        if (GsonHelper.getAsBoolean(json, DataGenJsonConstants.ANNOUNCE_TO_CHAT)) {
            json.remove(DataGenJsonConstants.ANNOUNCE_TO_CHAT);
        }
        if (!GsonHelper.getAsBoolean(json, DataGenJsonConstants.HIDDEN)) {
            //Look at inverse as this defaults to false
            json.remove(DataGenJsonConstants.HIDDEN);
        }
        if (GsonHelper.getAsBoolean(json, DataGenJsonConstants.SHOW_TOAST)) {
            json.remove(DataGenJsonConstants.SHOW_TOAST);
        }
        return json;
    }
}