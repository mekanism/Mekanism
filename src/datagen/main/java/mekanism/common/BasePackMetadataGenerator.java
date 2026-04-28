package mekanism.common;

import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;

public class BasePackMetadataGenerator extends PackMetadataGenerator {

    public BasePackMetadataGenerator(PackOutput output, IHasTranslationKey description) {
        super(output);
        int minVersion = Integer.MAX_VALUE;
        int maxVersion = 0;
        for (PackType packType : PackType.values()) {
            PackFormat version = DetectedVersion.BUILT_IN.packVersion(packType);
            maxVersion = Math.max(maxVersion, version);
            minVersion = Math.min(minVersion, version);
        }
        add(PackMetadataSection.TYPE, new PackMetadataSection(
              TextComponentUtil.build(description),
              new InclusiveRange<>(minVersion, maxVersion)
        ));
    }
}