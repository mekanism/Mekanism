package mekanism.common;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public class BasePackMetadataGenerator extends PackMetadataGenerator {

    public BasePackMetadataGenerator(PackOutput output, IHasTranslationKey description) {
        super(output);
        Map<PackType, Integer> packTypeVersions = new EnumMap<>(PackType.class);
        int maxVersion = 0;
        for (PackType packType : PackType.values()) {
            int version = DetectedVersion.BUILT_IN.getPackVersion(packType);
            packTypeVersions.put(packType, version);
            maxVersion = Math.max(maxVersion, version);
        }
        add(PackMetadataSection.TYPE, new PackMetadataSection(Component.translatable(description.getTranslationKey()), maxVersion, packTypeVersions));
    }
}