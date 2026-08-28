package mekanism.client;

import mekanism.api.text.ILangEntry;
import mekanism.common.base.holiday.HolidayManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.modlist.DefaultModDisplayInfo;
import net.neoforged.neoforge.client.gui.modlist.ImageResource;
import org.jspecify.annotations.Nullable;

public class MekanismModDisplayInfo extends DefaultModDisplayInfo {

    private final ILangEntry translatableDisplayName;

    public MekanismModDisplayInfo(ModContainer container, ILangEntry translatableDisplayName) {
        super(container);
        this.translatableDisplayName = translatableDisplayName;
        //TODO - 26.2: Do we want to set licenseURL in the mod files? If so do we care we might forget to update what branch it is pointing at when we change branches?
    }

    @Override
    public Component displayName() {
        //Allow translating the display name so that it can display upside down
        return translatableDisplayName.translate();
    }

    @Override
    @Nullable
    public ImageResource icon() {
        Identifier icon = HolidayManager.getCustomModIconToday();
        if (icon != null) {
            return ImageResource.packAsset(icon);
        }
        return super.icon();
    }
}