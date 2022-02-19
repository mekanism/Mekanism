package mekanism.client.gui.element.gauge;

import mekanism.common.tile.component.config.DataType;

public class GaugeType {

    public static final GaugeType STANDARD = get(GaugeInfo.STANDARD, GaugeOverlay.STANDARD);
    public static final GaugeType SMALL = get(GaugeInfo.STANDARD, GaugeOverlay.SMALL);
    public static final GaugeType SMALL_MED = get(GaugeInfo.STANDARD, GaugeOverlay.SMALL_MED);
    public static final GaugeType MEDIUM = get(GaugeInfo.STANDARD, GaugeOverlay.MEDIUM);
    public static final GaugeType WIDE = get(GaugeInfo.STANDARD, GaugeOverlay.WIDE);

    private final GaugeInfo gaugeInfo;
    private final GaugeOverlay gaugeOverlay;

    private GaugeType(GaugeInfo gaugeInfo, GaugeOverlay gaugeOverlay) {
        this.gaugeInfo = gaugeInfo;
        this.gaugeOverlay = gaugeOverlay;
    }

    public GaugeInfo getGaugeInfo() {
        return gaugeInfo;
    }

    public GaugeOverlay getGaugeOverlay() {
        return gaugeOverlay;
    }

    public GaugeType with(DataType type) {
        GaugeInfo info = GaugeInfo.get(type);
        return info == gaugeInfo ? this : with(info);
    }

    public GaugeType with(GaugeInfo info) {
        return new GaugeType(info, gaugeOverlay);
    }

    public static GaugeType get(GaugeInfo info, GaugeOverlay overlay) {
        return new GaugeType(info, overlay);
    }
}