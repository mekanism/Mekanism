package mekanism.generators.common.content.blocktype;

import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockShapes {

    private BlockShapes() {
    }

    private static VoxelShape box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return Block.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static final VoxelShape[] HEAT_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] WIND_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] BIO_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SOLAR_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] GAS_BURNING_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ADVANCED_SOLAR_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CONTROL_ROD_ASSEMBLY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] FUEL_ASSEMBLY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] TURBINE_ROTOR = {box(6, 0, 6, 10, 16, 10)};

    static {
        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 7, 6, 16, 16, 15), // drum
              box(0, 0, 0, 16, 6, 16), // base
              box(3, 6, 5, 5, 15, 16), // ring1
              box(11, 6, 5, 13, 15, 16), // ring2
              box(0, 6, 2, 16, 16, 5), // back
              box(3, 6, 1, 5, 15, 2), // bar1
              box(11, 6, 1, 13, 15, 2), // bar2
              box(4, 6, 0, 12, 12, 2), // port
              box(0, 13, 0, 16, 14, 2), // fin7
              box(0, 15, 0, 16, 16, 2), // fin8
              box(0, 11, 0, 4, 12, 2), // fin1
              box(0, 9, 0, 4, 10, 2), // fin2
              box(0, 7, 0, 4, 8, 2), // fin3
              box(12, 11, 0, 16, 12, 2), // fin4
              box(12, 9, 0, 16, 10, 2), // fin5
              box(12, 7, 0, 16, 8, 2) // fin6
        ), HEAT_GENERATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4.5, 68.5, 4, 11.5, 75.5, 13),
              box(5, 5, 1, 11, 11, 11),
              box(6, 3, 2.5, 10, 5, 4.5),
              box(4, 4, 0, 12, 12, 1),
              box(2, 1, 2, 14, 3, 14),
              box(0, 0, 0, 16, 2, 16),
              box(5.5, 68.5, 13, 10.5, 74.82, 14.3),
              box(5.5, 68.75, 14.3, 10.5, 74.1, 14.9),
              box(6.5, 68.8, 14.9, 9.5, 73.8, 15.3),
              box(6.5, 69, 15.3, 9.5, 72, 15.6),
              box(6.5, 69, 15.6, 9.5, 70.3, 16),
              box(5.25, 67, 5.25, 10.75, 70, 10.75),
              box(5, 59, 5, 11, 67, 11),
              box(4.75, 51, 4.75, 11.25, 59, 11.25),
              box(4.5, 43, 4.5, 11.5, 51, 11.5),
              box(4.25, 35, 4.25, 11.75, 43, 11.75),
              box(4, 27, 4, 12, 35, 12),
              box(3.75, 19, 3.75, 12.25, 27, 12.25),
              box(3.5, 11, 3.5, 12.5, 19, 12.5),
              box(3.25, 15, 3.25, 12.75, 19, 12.75),
              box(3, 3, 3, 13, 15, 13)
        ), WIND_GENERATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(6, -4, 6, 10, 25, 10), // pole
              box(4, 22, 2, 6, 24, 14), // tube1
              box(10, 22, 2, 12, 24, 14), // tube2
              box(4, 25, 5, 12, 31, 11), // barrel
              box(5, 24, 4, 6, 32, 12), // ring1
              box(10, 24, 4, 11, 32, 12), // ring2
              box(5, -11, 1, 11, -5, 4), // connector
              box(0, -16, 0, 16, -14, 16), // base
              box(3, -14, 3, 13, -12, 13), // base2
              box(4, -12, 4, 12, -4, 12), // base3
              box(4, -12, 0, 12, -4, 1), // port
              box(14, 30, -16, 32, 31, 32), // solar_panel_east
              box(15, 29, -15, 31, 30, 31), // solar_panel_east_base
              box(12, 27, 7, 28, 29, 9), // solar_panel_east_arm
              box(-16, 30, -16, 2, 31, 32), // solar_panel_west
              box(-15, 29, -15, 1, 30, 31), // solar_panel_west_base
              box(-12, 27, 7, 4, 29, 9) // solar_panel_west_arm
        ).move(0, 1, 0), ADVANCED_SOLAR_GENERATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 7, 16), // base
              box(3, 14.01, 0.99, 13, 15.01, 1.99), // bar
              box(13, 7, 0, 16, 16, 8), // sideRight
              box(0, 7, 0, 3, 16, 8), // sideLeft
              box(0, 7, 8, 16, 16, 16), // back
              box(2, 7, 1, 14, 15, 8) // glass
        ), Rotation.CLOCKWISE_180), BIO_GENERATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 6, 0, 16, 8, 16), // solarPanel
              box(4, 0, 4, 12, 1, 12), // solarPanelPort
              box(6, 4, 6, 10, 6, 10), // solarPanelConnector
              box(7, 2, 7, 9, 4, 9), // solarPanelRod1
              box(5, 1, 5, 6, 5, 6), // solarPanelRod2
              box(10, 1, 5, 11, 5, 6), // solarPanelRod3
              box(5, 1, 10, 6, 5, 11), // solarPanelRod3
              box(10, 1, 10, 11, 5, 11), // solarPanelRod4
              box(6, 1, 6, 10, 2, 10), // solarPanelPipeBase
              box(1, 5, 1, 15, 6, 15) // solarPanelBottom
        ), SOLAR_GENERATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4, 12, 13, 12, 13, 14), // port_connector_south1
              box(4, 11.3536, 12.6464, 12, 12.8536, 13.6464), // port_connector_south2
              box(0.5, 12, 4, 2, 13, 12), // port_connector_west1
              box(2, 12, 4, 3, 13, 12), // port_connector_west2
              box(1, 5, 4, 2, 5, 12), // bottom_connector1
              box(4, 5, 14, 12, 5, 15), // bottom_connector2
              box(14, 5, 4, 15, 5, 12), // bottom_connector3
              box(4, 5, 1, 12, 5, 2), // bottom_connector4
              box(4, 12, 2, 12, 13, 3), // port_connector_north1
              box(4, 10.6464, 2.0607, 12, 12.1464, 3.0607), // port_connector_north2
              box(12.8431, 17.0858, 4, 14.3431, 18.0858, 12), // port_connector_east1
              box(13, 12, 4, 14, 13, 12), // port_connector_east2
              box(3, 6, 3, 13, 16, 13), // chamber
              box(12, 5, 1, 15, 14, 4), // tank1
              box(1, 5, 1, 4, 14, 4), // tank4
              box(1, 5, 12, 4, 14, 15), // tank3
              box(12, 5, 12, 15, 14, 15), // tank2
              box(0, 0, 0, 16, 4, 16), // base
              box(2, 4, 2, 14, 5, 14), // base_platform
              box(4, 4, 0, 12, 12, 1), // port_north
              box(15, 4, 4, 16, 12, 12), // port_east
              box(4, 4, 15, 12, 12, 16), // port_south
              box(0, 4, 4, 1, 12, 12) // port_west
        ), GAS_BURNING_GENERATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4, 7, 4, 6, 15, 6), // connector_1
              box(10, 8, 4, 12, 16, 6), // connector_2
              box(10, 7, 10, 12, 15, 12), // connector_3
              box(4, 8, 10, 6, 16, 12), // connector_4
              box(7, 4, 7, 9, 12, 9), // connector_5
              box(1, 3, 1, 3, 7, 3), // rod_1
              box(1, 3, 5, 15, 7, 7), // rod_2
              box(1, 3, 9, 15, 7, 11), // rod_3
              box(1, 3, 13, 3, 7, 15), // rod_4
              box(5, 3, 1, 7, 7, 15), // rod_5
              box(9, 3, 1, 11, 7, 15), // rod_6
              box(13, 3, 13, 15, 7, 15), // rod_7
              box(13, 3, 1, 15, 7, 3), // rod_8
              box(2, 3, 2, 14, 8, 14), // core
              box(13, 9, 0, 16, 14, 3), // control_rod_frame1
              box(13, 9, 13, 16, 14, 16), // control_rod_frame2
              box(0, 9, 0, 3, 14, 3), // control_rod_frame3
              box(0, 9, 13, 3, 14, 16), // control_rod_frame4
              box(0, 7, 0, 16, 9, 3), // control_rod_frame5
              box(0, 14, 0, 16, 16, 3), // control_rod_frame6
              box(0, 7, 3, 3, 9, 13), // control_rod_frame7
              box(0, 14, 3, 3, 16, 13), // control_rod_frame8
              box(13, 7, 3, 16, 9, 13), // control_rod_frame9
              box(13, 14, 3, 16, 16, 13), // control_rod_frame10
              box(0, 7, 13, 16, 9, 16), // control_rod_frame11
              box(0, 14, 13, 16, 16, 16), // control_rod_frame12
              box(0, 0, 0, 16, 3, 16) // connector_ring_bottom
        ), CONTROL_ROD_ASSEMBLY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(2, 3, 2, 14, 13, 14), // core
              box(13, 1, 1, 15, 15, 3), // rod_1
              box(9, 1, 1, 11, 15, 15), // rod_2
              box(5, 1, 1, 7, 15, 15), // rod_3
              box(1, 1, 1, 3, 15, 3), // rod_4
              box(1, 1, 5, 15, 15, 7), // rod_5
              box(1, 1, 9, 15, 15, 11), // rod_6
              box(1, 1, 13, 3, 15, 15), // rod_7
              box(13, 1, 13, 15, 15, 15), // rod_8
              box(0, 13, 0, 16, 16, 16), // connector_ring_top
              box(0, 0, 0, 16, 3, 16) // connector_ring_bottom
        ), FUEL_ASSEMBLY);
    }
}
