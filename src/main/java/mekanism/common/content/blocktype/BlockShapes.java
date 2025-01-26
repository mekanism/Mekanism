package mekanism.common.content.blocktype;

import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockShapes {

    private BlockShapes() {
    }

    private static VoxelShape box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return Block.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static final VoxelShape[] ELECTROLYTIC_SEPARATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] DIGITAL_MINER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_CRYSTALLIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] PRESSURIZED_REACTION_CHAMBER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] METALLURGIC_INFUSER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_WASHER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_OXIDIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_INFUSER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_DISSOLUTION_CHAMBER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ROTARY_CONDENSENTRATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] FLUIDIC_PLENISHER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ELECTRIC_PUMP = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SOLAR_NEUTRON_ACTIVATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHARGEPAD = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] FLUID_TANK = {box(2, 0, 2, 14, 16, 14)};
    public static final VoxelShape[] LASER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] LASER_AMPLIFIER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] RESISTIVE_HEATER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SEISMIC_VIBRATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] PERSONAL_CHEST = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] QUANTUM_ENTANGLOPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] LOGISTICAL_SORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] SECURITY_DESK = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_TANK = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] INDUSTRIAL_ALARM = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_DASHBOARD = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_DRIVE_ARRAY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] QIO_IMPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_EXPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_REDSTONE_ADAPTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    //Save a tiny bit of memory by just referencing the fluid tank shape as it is identical
    public static final VoxelShape[] RADIOACTIVE_WASTE_BARREL = FLUID_TANK;
    public static final VoxelShape[] MODIFICATION_STATION = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ISOTOPIC_CENTRIFUGE = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SUPERCHARGED_COIL = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] ANTIPROTONIC_NUCLEOSYNTHESIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] PIGMENT_MIXER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    //Factories
    public static final VoxelShape[] SMELTING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ENRICHING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CRUSHING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] COMPRESSING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] COMBINING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] PURIFYING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] INJECTING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] INFUSING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SAWING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShapeUtils.setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 4, 16), // base
              box(15, 3, 3, 16, 13, 13), // portToggle1
              box(0, 4, 4, 1, 12, 12), // portToggle2a
              box(4, 4, 0, 12, 12, 1), // portToggle3a
              box(4, 4, 15, 12, 12, 16), // portToggle4a
              box(1, 4, 7, 3, 11, 9), // portToggle2b
              box(7, 4, 1, 8, 11, 3), // portToggle3b
              box(7, 4, 13, 8, 11, 15), // portToggle4b
              box(8, 4, 0, 16, 16, 16), // tank1
              box(0, 4, 9, 7, 14, 16), // tank2
              box(0, 4, 0, 7, 14, 7), // tank3
              box(7, 10, 7.5, 8, 11, 8.5), // tube1
              box(4, 12, 7.5, 7, 13, 8.5), // tube2
              box(3, 12, 7.5, 4, 15, 8.5), // tube3
              box(3, 15, 3, 4, 16, 13), // tube4
              box(3, 14, 3, 4, 15, 4), // tube5
              box(3, 14, 12, 4, 15, 13),// tube6
              box(6, 10, 7.5, 7, 12, 8.5)// tube7
        ), Rotation.CLOCKWISE_90), ELECTROLYTIC_SEPARATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(16, 17, -13, 30, 27, -11), // monitor1
              box(1, 17, -13, 15, 27, -11), // monitor2
              box(-14, 17, -13, 0, 27, -11), // monitor3
              box(17, 19, -11, 29, 25, -10), // monitor_back1
              box(2, 19, -11, 14, 25, -10), // monitor_back2
              box(-13, 19, -11, -1, 25, -10), // monitor_back3
              box(-1, 21, -11, 2, 23, -10), // monitor_bar1
              box(14, 21, -11, 17, 23, -10), // monitor_bar2
              box(4, 21, -11, 12, 23, -9), // monitor_mount
              box(3, 11.3498, -16, 13, 12.3498, -11), // keyboard
              box(4, 10.3498, -15, 12, 11.3498, -11), // keyboard_bottom
              box(5, 10, -13.25, 11, 12, -9), // keyboard_support
              box(-8, 3, -9, 24, 32, 3), // frame1
              box(17, 26.99, 4, 24, 31.99, 19), // frame2a_alt
              box(-8, 26.99, 4, -1, 31.99, 19), // frame2b_alt
              box(0, 26.99, 3, 16, 31.99, 16), // frame2c_alt
              box(0, 26.99, 17, 16, 31.99, 20), // frame2d_alt
              box(-8, 21, 4, 24, 26, 19), // frame2b
              box(-8, 15, 4, 24, 20, 19), // frame2c
              box(-8, 9, 4, 24, 14, 19), // frame2d
              box(-8, 3, 4, 24, 8, 19), // frame2e
              box(-8, 3, 19.99, 24, 32, 31.99), // frame3
              box(-7, 15, 3, 23, 31, 20), // core_top
              box(-7, 4, 3, 23, 15, 20), // core_bottom
              box(-9, 24, -6, -8, 29, 0), // bracket_east1
              box(-13, 24, -8, -8, 29, -6), // bracket_north1
              box(-13, 24, 0, -8, 29, 2), // bracket_south1
              box(-9, 24, 23, -8, 29, 29), // bracket_east2
              box(-13, 24, 21, -8, 29, 23), // bracket_north2
              box(-13, 24, 29, -8, 29, 31), // bracket_south2
              box(24, 24, -6, 25, 29, 0), // bracket_west1
              box(24, 24, 0, 29, 29, 2), // bracket_south3
              box(24, 24, -8, 29, 29, -6), // bracket_north3
              box(24, 24, 23, 25, 29, 29), // bracket_west2
              box(24, 24, 29, 29, 29, 31), // bracket_south4
              box(24, 24, 21, 29, 29, 23), // bracket_north4
              box(5, 2, -6, 11, 4, 5), // power_cable1a
              box(5, 1, 5, 11, 3, 11), // power_cable1b
              box(23, 5, 5, 31, 11, 11), // power_cable2
              box(-15, 5, 5, -7, 11, 11), // power_cable3
              box(-14, 2, -7, -10, 30, 1), // beam1
              box(-14, 2, 22, -10, 30, 30), // beam2
              box(26, 2, 22, 30, 30, 30), // beam3
              box(26, 2, -7, 30, 30, 1), // beam4
              box(-15, 0, -8, -8, 2, 2), // foot1
              box(-15, 0, 21, -8, 2, 31), // foot2
              box(24, 0, 21, 31, 2, 31), // foot3
              box(24, 0, -8, 31, 2, 2), // foot4
              box(-9, 4, 4, -8, 12, 12), // port3b
              box(-16, 4, 4, -15, 12, 12), // port3a
              box(24, 4, 4, 25, 12, 12), // port2b
              box(31, 4, 4, 32, 12, 12), // port2a
              box(4, 0, 4, 12, 1, 12), // port1
              box(17, 16.5, -12.005, 18, 17.5, -11.005), // led1
              box(2, 16.5, -12.005, 3, 17.5, -11.005), // led2
              box(-13, 16.5, -12.005, -12, 17.5, -11.005) // led3
        ), DIGITAL_MINER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 5, 14, 16, 7, 16), // rim_south
              box(0, 5, 2, 2, 7, 14), // rim_west
              box(0, 5, 0, 16, 7, 2), // rim_north
              box(14, 5, 2, 16, 7, 14), // rim_east
              box(3, 5, 3, 13, 6, 13), // tray
              box(0, 4, 4, 1, 12, 12), // port_west
              box(15, 4, 4, 16, 12, 12), // port_east
              box(0, 7, 0, 1, 11, 1), // support1
              box(0, 7, 15, 1, 11, 16), // support2
              box(15, 7, 15, 16, 11, 16), // support3
              box(15, 7, 0, 16, 11, 1), // support4
              box(0, 0, 0, 16, 5, 16), // base
              box(0, 11, 0, 16, 16, 16), // tank
              box(6, 9, 8, 7, 11, 9), // rod1
              box(9, 8, 9, 10, 11, 10), // rod2
              box(7.5, 7, 6, 8.5, 11, 7), // rod3
              box(1, 7, 1, 15, 11, 15) // glass
        ), CHEMICAL_CRYSTALLIZER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 4, 6, 16, 16, 16), // body
              box(10, 4, 1, 12, 15, 6), // front_div_1
              box(13, 4, 1, 15, 15, 6), // front_div_2
              box(1, 4, 2, 10, 14, 6), // front
              box(0, 0, 0, 16, 4, 16), // base
              box(12, 13, 2, 13, 14, 6), // bar1
              box(12, 11, 2, 13, 12, 6), // bar2
              box(12, 9, 2, 13, 10, 6), // bar3
              box(12, 7, 2, 13, 8, 6), // bar4
              box(12, 5, 2, 13, 6, 6) // bar5
        ), PRESSURIZED_REACTION_CHAMBER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(11, 11, 5, 12, 16, 8), // connector1
              box(4, 11, 5, 5, 16, 8), // connector2
              box(2, 7, 2, 14, 8, 8), // divider
              box(0, 0, 0, 16, 4, 16), // base
              box(0, 4, 8, 16, 16, 16), // back
              box(1, 12, 1, 15, 15, 8), // plate_top
              box(1, 8, 1, 15, 11, 8), // plate_middle
              box(1, 4, 1, 15, 7, 8), // plate_bottom
              box(13, 11, 2, 14, 12, 3), // bar1
              box(2, 11, 2, 3, 12, 3) // bar2
        ), METALLURGIC_INFUSER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 4, 16), // base
              box(4, 13, 4, 12, 15, 12), // pipe1
              box(5, 4, 5, 11, 13, 10), // pipe2
              box(0, 4, 0, 7, 14, 8), // tank1
              box(9, 4, 0, 16, 14, 8), // tank2
              box(0, 4, 10, 16, 14, 16), // tank3
              box(13, 5, 8, 15, 11, 10), // connector1
              box(7, 7, 3, 9, 9, 5), // conduit
              box(7, 6, 1, 9, 7, 2), // bridge4
              box(7, 8, 1, 9, 9, 2), // bridge3
              box(7, 10, 1, 9, 11, 2), // bridge2
              box(7, 12, 1, 9, 13, 2), // bridge1
              box(1, 14, 4, 2, 15, 5), // vent1
              box(1, 14, 6, 2, 15, 7), // vent2
              box(13, 14, 4, 14, 15, 12), // tubeLeft2
              box(1, 5, 8, 3, 11, 10), // connector2
              box(3, 15, 3, 13, 16, 13), // port_up
              box(0, 4, 4, 1, 12, 12), // port_west
              box(15, 4, 4, 16, 12, 12) // port_east
        ), CHEMICAL_WASHER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(9, 4, 2, 13, 5, 14), // stand
              box(8, 5, 7, 14, 16, 15), // tower2
              box(8, 5, 1, 14, 16, 6), // tower1
              box(7, 7, 9, 8, 10, 12), // pipe1
              box(0, 4, 0, 7, 16, 16), // tank
              box(0, 4, 4, 1, 12, 12), // connectorToggle
              box(9, 6, 6, 13, 15, 7), // bridge
              box(13, 5, 5, 15, 11, 11), // pipe2
              box(15, 3, 3, 16, 13, 13), // connector
              box(0, 0, 0, 16, 4, 16) // base
        ), CHEMICAL_OXIDIZER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 4, 4, 1, 12, 12), // portRight
              box(15, 4, 4, 16, 12, 12), // portLeft
              box(2, 5, 1, 14, 12, 8), // tank3
              box(9, 13.5, 4, 10, 14.5, 6), // tube4
              box(7, 5, 13, 9, 11, 15), // connector1
              box(3, 5, 6, 13, 11, 9), // connector2
              box(8, 8, 9, 9, 9, 13), // connector3
              box(1, 5, 5, 2, 11, 9), // pipe2
              box(6, 13.5, 4, 7, 14.5, 6), // tube5
              box(7, 14, 13, 9, 15, 14), // tube10
              box(11, 13.5, 6.5, 13, 14.5, 7.5), // tube2
              box(4, 4, 0, 12, 12, 1), // portFront
              box(0, 0, 0, 16, 5, 16), // base
              box(3, 13.5, 7.5, 4, 14.5, 9.5), // tube8
              box(14, 5, 5, 15, 11, 9), // pipe1
              box(12, 13.5, 7.5, 13, 14.5, 9.5), // tube1
              box(6, 11.5, 4, 7, 13.5, 5), // tube6
              box(4, 4, 15, 12, 12, 16), // portBack
              box(3, 13.5, 6.5, 5, 14.5, 7.5), // tube7
              box(7, 14, 10, 9, 15, 11), // tube9
              box(5, 12.5, 5.5, 11, 15.5, 8.5), // compressor
              box(9, 11.5, 4, 10, 13.5, 5), // tube3
              box(11, 12, 2, 12, 13, 3), // exhaust1
              box(9, 12, 2, 10, 13, 3), // exhaust2
              box(6, 12, 2, 7, 13, 3), // exhaust3
              box(4, 12, 2, 5, 13, 3), // exhaust4
              box(9, 5, 9, 15, 16, 15), // tank1
              box(1, 5, 9, 7, 16, 15) // tank2
        ), CHEMICAL_INFUSER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 7, 16), // base
              box(0, 3, 4, 1, 11, 12), // port_right
              box(15, 3, 3, 16, 13, 13), // port_left
              box(0, 7, 15, 16, 15, 16), // back
              box(1, 7, 14, 15, 15, 15), // back_wall
              box(0, 15, 0, 16, 16, 16), // top
              box(0, 12, 0, 16, 13, 15), // middle
              box(4, 13, 4, 12, 15, 14), // vat
              box(1, 13, 1, 2, 15, 2), // support_1
              box(14, 13, 1, 15, 15, 2), // support_2
              box(1, 7, 1, 15, 12, 14) // glass
        ), CHEMICAL_DISSOLUTION_CHAMBER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 5, 16), // base
              box(0, 15, 0, 16, 16, 16), // top
              box(0, 13, 0, 16, 14, 16), // middle
              box(7.5, 11, 7.5, 8.5, 13, 8.5), // shaft
              box(4, 14, 4, 12, 15, 12), // bridge
              box(7, 5, 5, 9, 11, 11), // pipe
              box(9, 5, 1, 15, 13, 15), // tankLeft
              box(1, 5, 1, 7, 13, 15), // tankRight
              box(15, 4, 4, 16, 12, 12), // portLeft
              box(0, 3, 3, 1, 13, 13), // portRight
              box(14, 14, 14, 15, 15, 15), // support1
              box(14, 14, 1, 15, 15, 2), // support2
              box(1, 14, 1, 2, 15, 2), // support3
              box(1, 14, 14, 2, 15, 15), // support4
              box(7, 11, 2, 9, 12, 3), // tube1
              box(7, 9, 2, 9, 10, 3), // tube2
              box(7, 7, 2, 9, 8, 3), // tube3
              box(7, 5, 2, 9, 6, 3), // tube4
              box(7, 7, 13, 9, 8, 14), // tube5
              box(7, 9, 13, 9, 10, 14), // tube6
              box(7, 11, 13, 9, 12, 14), // tube7
              box(7, 5, 13, 9, 6, 14)// tube8
        ), ROTARY_CONDENSENTRATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(3, 1, 3, 13, 13, 13), // main
              box(4, 4, 15, 12, 12, 16), // port_side
              box(5, 5, 13, 11, 11, 15), // energy_connector
              box(2, 13, 2, 14, 14, 14), // top
              box(3, 15, 3, 13, 16, 13), // port_top
              box(4, 0, 4, 12, 1, 12), // bottom
              //box(5, -7, 5, 11, 1, 11), // pipe
              box(4, 14, 4, 12, 15, 12), // fluid_connector
              box(2, 6, 6, 3, 10, 10), // knob_1
              box(13, 6, 6, 14, 10, 10) // knob_2
        ), FLUIDIC_PLENISHER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              box(4, 1, 4, 12, 12, 12), // pumpCasing
              box(10, 10, 1, 11, 11, 5), // powerConnectorFrame1
              box(5, 10, 1, 6, 11, 5), // powerConnectorFrame2
              box(5, 5, 1, 6, 6, 5), // powerConnectorFrame3
              box(10, 5, 1, 11, 6, 5), // powerConnectorFrame4
              box(5, 0, 5, 11, 15, 11), // pumpBase
              box(4, 15, 4, 12, 16, 12), // pumpPortTop
              box(4, 13, 4, 12, 14, 12), // pumpRingTop
              box(6, 6, 1, 10, 10, 4), // powerConnector
              box(4, 4, 0, 12, 12, 1) // powerPort
        ), Rotation.CLOCKWISE_180), ELECTRIC_PUMP);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(6, 27, 4, 10, 28, 14), // panel_base
              box(7, 26, 8, 9, 27, 14), // panel_connector
              box(0, 0, 0, 16, 4, 16), // base_bottom
              box(0, 5, 0, 16, 14, 16), // base_top
              box(2, 4, 2, 14, 5, 14), // base_middle
              box(1, 4, 3, 2, 5, 5), // thing1
              box(1, 4, 6, 2, 5, 10), // thing2
              box(1, 4, 11, 2, 5, 13), // thing3
              box(14, 4, 11, 15, 5, 13), // thing4
              box(14, 4, 6, 15, 5, 10), // thing5
              box(14, 4, 3, 15, 5, 5), // thing6
              box(6, 4, 14, 10, 5, 16), // energy_connector
              box(4, 4, 0, 12, 12, 1), // port
              box(6, 14, 14, 10, 28, 16), // energy
              box(5, 14, 3, 11, 15, 9), // tray
              box(7, 14, 9, 9, 15, 14), // cable
              box(6, 14, 12, 10, 16, 13), // brace1
              box(6, 14, 10, 10, 16, 11), // brace2
              box(5, 14, 10, 6, 15, 11), // brace3
              box(5, 14, 12, 6, 15, 13), // brace4
              box(10, 14, 10, 11, 15, 11), // brace5
              box(10, 14, 12, 11, 15, 13), // brace6
              box(7, 25, 5, 9, 26, 7), // emitter
              box(6, 26, 4, 10, 27, 8), // laser

              box(4, 28, 0, 12, 29, 16), // panel_mid (extended to be slightly wider)
              //Rough estimates of the two panels
              //Right
              box(12, 29, 0, 14, 30, 16),
              box(14, 30, 0, 16.5, 31, 16),
              //Left
              box(2, 29, 0, 4, 30, 16),
              box(-0.5, 30, 0, 2, 31, 16)
        ), SOLAR_NEUTRON_ACTIVATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 1, 16), // base
              box(9, 1, 12, 10, 5, 15), // stand_left
              box(6, 1, 12, 7, 5, 15), // stand_right
              box(5, 5, 12, 11, 11, 15), // port_back
              box(4, 4, 15, 12, 12, 16) // port
        ), CHARGEPAD);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(5, 2, 4, 11, 9, 12), // body
              box(8.995, 10, 7.5, 9.995, 13, 8.5), // wire
              box(11, 8, 4, 12, 9, 12), // fin1
              box(11, 6, 4, 12, 7, 12), // fin2
              box(11, 4, 4, 12, 5, 12), // fin3
              box(11, 2, 4, 12, 3, 12), // fin4
              box(4, 8, 4, 5, 9, 12), // fin5
              box(4, 6, 4, 5, 7, 12), // fin6
              box(4, 4, 4, 5, 5, 12), // fin7
              box(4, 2, 4, 5, 3, 12), // fin8
              box(5, 9, 5, 11, 10, 11), // shaft
              box(7, 10, 7, 9, 16, 9), // center
              box(10.995, 3, 9, 11.995, 8, 10), // rod1
              box(10.995, 3, 6, 11.995, 8, 7), // rod2
              box(4.005, 3, 6, 5.005, 8, 7), // rod3
              box(4.005, 3, 9, 5.005, 8, 10), // rod4
              box(4, 0, 4, 12, 1, 12), // port
              box(5, 1, 5, 11, 2, 11), // connector
              box(6, 13, 6, 10, 14, 10), // ring1
              box(6, 11, 6, 10, 12, 10) // ring2
        ), LASER, true, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(1, 1, 1, 15, 15, 15),//Base
              box(0, 3, 3, 1, 13, 13),//S1
              box(3, 3, 15, 13, 13, 16),//S2
              box(15, 3, 3, 16, 13, 13),//S3
              box(3, 0, 3, 13, 1, 13),//S4
              box(3, 3, 0, 13, 13, 1),//S5
              box(3, 15, 3, 13, 16, 13)//S6
        ), LASER_AMPLIFIER, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 4, 4, 1, 12, 12), // port_left
              box(15, 4, 4, 16, 12, 12), // port_right
              box(4, 13, 1.5, 5, 14, 14.5), // bar1
              box(6, 13, 1.5, 7, 14, 14.5), // bar2
              box(9, 13, 1.5, 10, 14, 14.5), // bar3
              box(11, 13, 1.5, 12, 14, 14.5), // bar4
              box(0, 0, 0, 16, 7, 16), // base
              box(13, 7, 0, 16, 16, 16), // wall_right
              box(0, 7, 0, 3, 16, 16), // wall_left
              box(3, 6, 1, 13, 15, 2), // fin1
              box(3, 6, 3, 13, 15, 4), // fin2
              box(3, 6, 5, 13, 15, 6), // fin3
              box(3, 6, 7, 13, 15, 8), // fin4
              box(3, 6, 8, 13, 15, 9), // fin5
              box(3, 6, 10, 13, 15, 11), // fin6
              box(3, 6, 12, 13, 15, 13), // fin7
              box(3, 6, 14, 13, 15, 15) // fin8
        ), RESISTIVE_HEATER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 5, 3), // base1
              box(0, 0, 13, 16, 5, 16), // base2
              box(0, 0, 3, 3, 5, 13), // base3
              box(13, 0, 3, 16, 5, 13), // base4
              box(6, 5, 14, 10, 30, 16), // back
              box(0, 30, 0, 16, 32, 16), // top
              box(4, 28, 6, 5, 29, 7), // rivet1
              box(4, 28, 8, 5, 29, 9), // rivet2
              box(4, 28, 10, 5, 29, 11), // rivet3
              box(4, 28, 12, 5, 29, 13), // rivet4
              box(11, 28, 12, 12, 29, 13), // rivet5
              box(11, 28, 10, 12, 29, 11), // rivet6
              box(11, 28, 8, 12, 29, 9), // rivet7
              box(3, 29, 3, 13, 30, 14), // plate
              box(4, 0, 4, 12, 2, 12), // plate2
              box(5, 2, 5, 11, 3, 11), // plate3
              box(5, 25, 5, 11, 29, 14), // motor
              box(6, 13, 6, 10, 15, 10), // brace
              box(6, 18, 6, 10, 25, 10), // brace2
              box(7, 13, 10, 9, 15, 14), // brace3
              box(7, 2, 7, 9, 18, 9), // shaft
              box(4, 5, 15.01, 12, 13, 16.01), // port
              box(4, 5, 16.01, 12, 13, 16.01), // port_led
              box(11, 28, 6, 12, 29, 7), // rivet8

              //Walls uses full walls instead of angles because even though we have code to calculate the proper angles
              // it causes lag when looking at the overly complicated bounding box
              box(1, 5, 1, 2, 30, 15),//pole1 - pole4
              box(1, 5, 14, 15, 30, 15),//pole1 - pole2
              box(14, 5, 1, 15, 30, 15)//pole2 - pole3
        ), SEISMIC_VIBRATOR);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(1, 0, 1, 15, 14, 15),//Main chest
              box(7, 7, 0, 9, 11, 1)//latch
        ), PERSONAL_CHEST);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 3, 3, 3), // corner1
              box(0, 0, 13, 3, 3, 16), // corner2
              box(13, 0, 13, 16, 3, 16), // corner3
              box(13, 0, 0, 16, 3, 3), // corner4
              box(13, 13, 0, 16, 16, 3), // corner5
              box(0, 13, 0, 3, 16, 3), // corner6
              box(0, 13, 13, 3, 16, 16), // corner7
              box(13, 13, 13, 16, 16, 16), // corner8
              box(0, 0, 3, 1, 1, 13), // edge1
              box(0, 15, 3, 1, 16, 13), // edge2
              box(15, 15, 3, 16, 16, 13), // edge3
              box(15, 0, 3, 16, 1, 13), // edge4
              box(3, 0, 0, 13, 1, 1), // edge5
              box(3, 15, 0, 13, 16, 1), // edge6
              box(3, 15, 15, 13, 16, 16), // edge7
              box(3, 0, 15, 13, 1, 16), // edge8
              box(15, 3, 15, 16, 13, 16), // edge9
              box(0, 3, 15, 1, 13, 16), // edge10
              box(0, 3, 0, 1, 13, 1), // edge11
              box(15, 3, 0, 16, 13, 1), // edge12
              box(1, 13, 3, 3, 15, 13), // inner1
              box(13, 13, 3, 15, 15, 13), // inner2
              box(13, 1, 3, 15, 3, 13), // inner3
              box(1, 1, 3, 3, 3, 13), // inner4
              box(3, 1, 13, 13, 3, 15), // inner5
              box(3, 1, 1, 13, 3, 3), // inner6
              box(3, 13, 1, 13, 15, 3), // inner7
              box(3, 13, 13, 13, 15, 15), // inner8
              box(1, 3, 13, 3, 13, 15), // inner9
              box(1, 3, 1, 3, 13, 3), // inner10
              box(13, 3, 1, 15, 13, 3), // inner11
              box(13, 3, 13, 15, 13, 15), // inner12
              box(4, 4, 0, 12, 12, 1), // port_small_north
              box(15, 4, 4, 16, 12, 12), // port_small_east
              box(4, 4, 15, 12, 12, 16), // port_small_south
              box(0, 4, 4, 1, 12, 12), // port_small_west
              box(4, 15, 4, 12, 16, 12), // port_small_up
              box(4, 0, 4, 12, 1, 12) // port_small_down
        ), QUANTUM_ENTANGLOPORTER, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              box(2, 2, 15, 14, 14, 16), // portBackLarge
              box(7, 10, 2, 9, 12, 3), // pistonBrace1
              box(7, 4, 2, 9, 6, 3), // pistonBrace2
              box(4, 4, 13, 12, 12, 14), // ring1
              box(4, 4, 11, 12, 12, 12), // ring2
              box(4, 4, 9, 12, 12, 10), // ring3
              box(4, 4, 7, 12, 12, 8), // ring4
              box(4, 4, 5, 12, 12, 6), // ring5
              box(4, 4, 3, 12, 12, 4), // ring6
              box(4, 4, 1, 12, 12, 2), // ring7
              box(7, 11, 4, 9, 13, 5), // pistonConnector1
              box(7, 3, 4, 9, 5, 5), // pistonConnector2
              box(3, 3, 14, 13, 13, 15), // connectorBack
              box(7, 11, 9.01, 9, 13, 14.01), // pistonBase1
              box(7, 3, 9.01, 9, 5, 14.01), // pistonBase2
              box(3, 3, 0, 13, 13, 1), // portFront
              box(5, 5, 1, 11, 11, 15), // pipe
              box(7, 12, 5, 9, 13, 9), // pistonBar1
              box(7, 3, 5, 9, 4, 9), // pistonBar2
              box(11.005, 6.5, 4, 12.005, 9.5, 11), // panel1
              box(3.995, 6.5, 4, 4.995, 9.5, 11), // panel2
              box(4, 4, 16, 12, 12, 17), // portBack
              box(11.5, 7.5, 8, 12.5, 8.5, 9), // bulb1
              box(3.5, 7.5, 8, 4.5, 8.5, 9), // bulb2
              box(3.5, 7.5, 6, 4.5, 8.5, 7), // bulb3
              box(11.5, 7.5, 6, 12.5, 8.5, 7) // bulb4
        ), Direction.NORTH), LOGISTICAL_SORTER, true, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 6, 0, 16, 13, 16), // desk_top
              box(1, 5, 1, 15, 6, 15), // desk_middle
              box(0, 0, 0, 16, 5, 16), // desk_base
              box(1, 14, 11, 15, 24, 13), // monitor
              box(2, 15, 10.99, 14, 23, 10.99), // monitor_screen_led
              box(2, 13.5, 11.5, 3, 14.5, 12.5), // button_led
              box(2, 16, 13, 14, 22, 14), // monitor_back
              box(4, 13, 10, 12, 14, 14), // stand_base
              box(7, 14, 13, 9, 21, 14), // stand_neck
              box(3, 13, 2, 13, 14, 7) // keyboard
        ), SECURITY_DESK);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(6, 13, 4, 10, 14, 5), // rim0
              box(10, 13, 4, 12, 16, 5), // rim1
              box(11, 13, 5, 12, 16, 11), // rim2
              box(4, 13, 11, 12, 16, 12), // rim3
              box(4, 13, 5, 5, 16, 11), // rim4
              box(4, 13, 4, 6, 16, 5), // rim5
              box(3, 1, 3, 13, 13, 13), // tank
              box(4, 0, 4, 12, 1, 12), // tankBase
              box(6.5, 14, 6.5, 9.5, 15, 9.5), // valve
              box(7, 12, 7, 9, 14, 9) // valveBase
        ), CHEMICAL_TANK);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(5, 15, 5, 11, 16, 11),
              box(6, 11, 6, 10, 15, 10)
        ), INDUSTRIAL_ALARM, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(1, 15, 1, 15, 16, 15)
        ), QIO_DASHBOARD, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 12, 0, 16, 16, 9), // drive_top
              box(0, 0, 9, 16, 16, 16), // drive_front
              box(0, 6, 0, 16, 9, 9), // rack_top
              box(0, 0, 0, 16, 3, 9), // rack_bottom
              box(0, 3, 0, 1, 6, 1), // post_bottom_right
              box(0, 9, 0, 1, 12, 1), // post_top_right
              box(15, 3, 0, 16, 6, 1), // post_bottom_left
              box(15, 9, 0, 16, 12, 1) // post_top_left
        ), QIO_DRIVE_ARRAY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4, 0, 4, 12, 1, 12), // disc_base
              box(5, 4, 5, 11, 5, 6), // ring_top_led_1
              box(5, 4, 10, 11, 5, 11), // ring_top_led_2
              box(5, 4, 6, 6, 5, 10), // ring_top_led_3
              box(10, 4, 6, 11, 5, 10), // ring_top_led_4
              box(10, 2, 6, 11, 3, 10), // ring_bottom_led_1
              box(5, 2, 6, 6, 3, 10), // ring_bottom_led_2
              box(5, 2, 10, 11, 3, 11), // ring_bottom_led_3
              box(5, 2, 5, 11, 3, 6), // ring_bottom_led_4
              box(9, 1, 6, 10, 6, 7), // post_1
              box(9, 1, 9, 10, 6, 10), // post_2
              box(6, 1, 9, 7, 6, 10), // post_3
              box(6, 1, 6, 7, 6, 7), // post_4
              box(9, 8, 9, 10, 9, 10), // top_post_led_1
              box(6, 9, 6, 7, 10, 7), // top_post_led_2
              box(7.5, 1, 7.5, 8.5, 6, 8.5), // core_led
              box(5, 6, 5, 11, 7, 11), // disc_mid
              box(9, 7, 9, 10, 8, 10), // antenna_1
              box(6, 7, 6, 7, 9, 7) // antenna_2
        ), QIO_IMPORTER, true, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4, 0, 4, 12, 1, 12), // disc_base
              box(5, 4, 5, 11, 5, 6), // ring_top_led_1
              box(5, 4, 10, 11, 5, 11), // ring_top_led_2
              box(5, 4, 6, 6, 5, 10), // ring_top_led_3
              box(10, 4, 6, 11, 5, 10), // ring_top_led_4
              box(10, 2, 6, 11, 3, 10), // ring_bottom_led_1
              box(5, 2, 6, 6, 3, 10), // ring_bottom_led_2
              box(5, 2, 10, 11, 3, 11), // ring_bottom_led_3
              box(5, 2, 5, 11, 3, 6), // ring_bottom_led_4
              box(9, 1, 6, 10, 6, 7), // post_1
              box(9, 1, 9, 10, 6, 10), // post_2
              box(6, 1, 9, 7, 6, 10), // post_3
              box(6, 1, 6, 7, 6, 7), // post_4
              box(7, 7, 7, 9, 8, 9), // top_post_led_1
              box(7, 8.01, 7, 9, 9.01, 9), // top_post_led_2
              box(7.5, 1, 7.5, 8.5, 6, 8.5), // core_led
              box(5, 6, 5, 11, 7, 11), // disc_mid
              box(6, 8, 6, 10, 9, 10), // disc_top
              box(9, 9, 9, 10, 10, 10), // crown_1
              box(6, 9, 9, 7, 10, 10), // crown_2
              box(6, 9, 6, 7, 10, 7), // crown_3
              box(9, 9, 6, 10, 10, 7) // crown_4
        ), QIO_EXPORTER, true, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4, 0, 4, 12, 1, 12), // disc_base
              box(5, 2, 5, 11, 3, 6), // ring_led_1
              box(5, 2, 10, 11, 3, 11), // ring_led_2
              box(5, 2, 6, 6, 3, 10), // ring_led_3
              box(10, 2, 6, 11, 3, 10), // ring_led_4
              box(9, 1, 6, 10, 4, 7), // post_1
              box(9, 1, 9, 10, 4, 10), // post_2
              box(6, 1, 9, 7, 4, 10), // post_3
              box(6, 1, 6, 7, 4, 7), // post_4
              box(7.5, 1, 7.5, 8.5, 4, 8.5), // core_led
              box(5, 4, 5, 11, 5, 11), // disc_mid
              box(6, 8, 6, 10, 9, 10), // disc_top
              box(7, 5, 7, 9, 11, 9) // torch
        ), QIO_REDSTONE_ADAPTER, true, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(-16, 13, 0, 16, 16, 16), // desk_top
              box(13, 0, 0, 16, 13, 3), // desk_part1
              box(0, 0, 0, 3, 13, 3), // desk_part2
              box(-16, 8, 0, 0, 11, 16), // desk_part3
              box(0, 8, 3, 16, 11, 16), // desk_part4
              box(0, 0, 3, 16, 6, 16), // desk_part5
              box(-16, 0, 0, 0, 6, 16), // desk_part6
              box(-15, 11, 1, 0, 13, 15), // desk_filler_right_top
              box(-15, 6, 1, 0, 8, 15), // desk_filler_right_bottom
              box(0, 11, 3, 15, 13, 15), // desk_filler_left_top
              box(0, 6, 3, 15, 8, 15), // desk_filler_left_bottom
              box(1, 17.5, 10.5, 15, 25.5, 12.5), // monitor
              box(2, 17, 11, 3, 18, 12), // led
              box(2, 15, 1, 14, 17, 7), // keyboard
              box(6, 16, 11, 10, 22, 13), // monitor_stand_arm
              box(5, 16, 10, 11, 17, 14), // monitor_stand_base
              box(-14, 16, 2, -1, 17, 14), // modifier_base
              box(-14, 16, 14, -11, 30, 16), // modifier_arm_right1
              box(-14, 6, 14, -11, 16, 16), // modifier_arm_right2
              box(-4, 16, 14, -1, 30, 16), // modifier_arm_left1
              box(-4, 6, 14, -1, 16, 16), // modifier_arm_left2
              box(-11, 23, 8, -10, 27, 9), // modifier_probe_right
              box(-11, 22, 8, -10, 23, 9), // modifier_probe_right_led
              box(-5, 23, 8, -4, 27, 9), // modifier_probe_left
              box(-5, 22, 8, -4, 23, 9), // modifier_probe_left_led
              box(-13, 27, 7, -2, 28, 10), // modifier_probe_base
              box(-11, 28, 6, -4, 30, 11), // modifier_arm_top_center
              box(-14, 28, 6, -11, 30, 14), // modifier_arm_top_right
              box(-4, 28, 6, -1, 30, 14), // modifier_arm_top_left
              box(4, 4, 15, 12, 12, 16) // energy_port
        ), MODIFICATION_STATION);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 2, 16), // base
              box(1, 2, 14, 2, 3, 15), // rivet1
              box(1, 2, 1, 2, 3, 2), // rivet2
              box(14, 2, 1, 15, 3, 2), // rivet3
              box(14, 2, 14, 15, 3, 15), // rivet4
              box(3, 2, 3, 13, 16, 13), // tower1
              box(3, 27, 3, 13, 30, 13), // tower2
              box(3, 16, 3, 6, 27, 6), // tower3
              box(10, 16, 3, 13, 27, 6), // tower4
              box(3, 16, 10, 6, 27, 13), // tower5
              box(10, 16, 10, 13, 27, 13), // tower6
              box(4, 16, 4, 12, 27, 12), // glass_tank
              box(2, 9, 2, 14, 10, 14), // ring1
              box(2, 7, 2, 14, 8, 14), // ring2
              box(2, 5, 2, 14, 6, 14), // ring3
              box(2, 3, 2, 14, 4, 14), // ring4
              box(2, 10, 10, 3, 14, 11), // pipe1
              box(4, 30, 4, 6, 32, 6), // node1
              box(5, 30, 10, 6, 32, 11), // node2
              box(6, 30, 6, 10, 31, 10), // coil
              box(4, 4, 0, 12, 12, 1), // port
              box(5, 5, 1, 11, 11, 3), // port_connector
              box(6, 18, 7, 7, 25, 8), // random_shape1
              box(6, 17, 7, 9, 18, 8), // random_shape2
              box(8, 15, 7, 9, 17, 8) // random_shape3
        ), ISOTOPIC_CENTRIFUGE);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(5, 1, 5, 11, 10, 11), // body
              box(4.995, 8, 7, 5.995, 13, 8), // wire1
              box(10.005, 8, 8, 11.005, 13, 9), // wire2
              box(7, 15, 7, 9, 16, 9), // center
              box(4, 0, 4, 12, 1, 12), // port
              box(6, 10, 6, 10, 15, 10), // coil_large
              box(7, 3, 4, 9, 8, 6), // coil_1
              box(7, 3, 10, 9, 8, 12), // coil_2
              box(4, 3, 7, 6, 8, 9), // coil_3
              box(10, 3, 7, 12, 8, 9) // coil_4
        ), SUPERCHARGED_COIL, true, true);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 5, 0, 16, 8, 16),
              box(1, 3, 1, 15, 5, 15),
              box(0, 0, 0, 16, 3, 16),
              box(12, 8, 0, 16, 16, 10),
              box(11, 9, 4, 12, 14, 9),
              box(4, 9, 4, 5, 14, 9),
              box(5, 11, 6, 6, 12, 7),
              box(10, 11, 6, 11, 12, 7),
              box(0, 8, 10, 16, 16, 13),
              box(0, 13, 13, 16, 16, 16),
              box(1, 11, 13, 15, 12, 15), // fin1
              box(1, 9, 13, 15, 10, 15), // fin2
              box(0, 8, 0, 4, 16, 10),
              box(6, 8, 13, 10, 13, 16), // divider
              box(6, 3, 15, 10, 5, 16), // divider
              box(2, 8, 13, 3, 13, 14), // fuel_rod_led1
              box(4, 8, 13, 5, 13, 14), // fuel_rod_led2
              box(11, 8, 13, 12, 13, 14), // fuel_rod_led3
              box(13, 8, 13, 14, 13, 14), // fuel_rod_led4
              box(4, 4, 15, 12, 12, 16), // port
              box(4, 8, 1, 12, 15, 10) // glass
        ), ANTIPROTONIC_NUCLEOSYNTHESIZER);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 16, 16), // base
              box(5, 27, 4, 11, 28, 16), // mixer_rod_support
              box(6, 28, 5, 10, 30, 9), // mixer_motor
              box(1, 16, 1, 15, 18, 13), // basin_lid
              box(2, 16, 12, 14, 20, 16), // basin_hinge
              box(6, 20, 13, 10, 30, 15), // back_electronics_box
              box(5, 30, 4, 11, 32, 16), // top_electronics_box
              box(7, 18, 6, 9, 27, 8) // mixer_rod
        ), PIGMENT_MIXER);

        //Factories:
        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(0, 0, 5, 16, 4, 16), // base
              box(1, 0, 4, 15, 15, 15), // body
              box(4, 4, 14, 12, 12, 16), // port
              box(11, 4, 5, 16, 16, 10), // stack1
              box(0, 4, 5, 5, 16, 10), // stack2
              box(11, 4, 11, 16, 16, 16), // stack3
              box(0, 4, 11, 5, 16, 16) // stack4
        ), SMELTING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(4, 4, 14, 12, 12, 16), // port
              box(1, 7, 4, 15, 14, 14), // core
              box(2, 4, 4, 14, 7, 10), // middle
              box(5, 5, 10, 11, 7, 14), // middle_connector
              box(0, 0, 4, 16, 4, 16), // base
              box(0, 14, 4, 16, 16, 16), // shell_01
              box(0, 6, 4, 2, 14, 16), // shell_02
              box(14, 6, 4, 16, 14, 16) // shell_03
        ), ENRICHING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(2, 0, 7, 14, 4, 12), // base
              box(2, 10, 7, 14, 12, 12), // bracket
              box(3, 4, 7, 13, 12, 12), // shaft
              box(0, 12, 4, 16, 16, 16), // top
              box(1, 0, 4, 15, 12, 7), // leg1
              box(1, 0, 12, 15, 12, 15), // leg2
              box(4, 4, 14, 12, 12, 16) // port
        ), CRUSHING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(0, 0, 12, 16, 16, 16), // vent
              box(0, 12, 4, 16, 16, 11), // top
              box(0, 0, 4, 16, 4, 11), // base
              box(1, 0, 4, 15, 15, 12), // core
              box(0, 5, 5, 16, 11, 12), // window
              box(4, 4, 14, 12, 12, 16) // port
        ), COMPRESSING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 12, 4, 16, 16, 16), // top
              box(5, 4, 4, 11, 12, 14), // body
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(4, 4, 14, 12, 12, 16), // port
              box(0, 0, 4, 16, 4, 16), // bottom
              box(1, 4, 5, 15, 7, 15), // plate2
              box(1, 8, 5, 15, 11, 15), // plate1
              box(3, 11, 12, 13, 12, 14), // press2
              box(3, 11, 6, 13, 12, 8) // press1
        ), COMBINING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 12, 4, 16, 16, 16), // top
              box(1, 0, 4, 15, 15, 15), // body
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(4, 4, 14, 12, 12, 16), // port
              box(11, 1, 5, 16, 12, 10), // stack1
              box(0, 1, 5, 5, 12, 10), // stack2
              box(0, 1, 11, 16, 12, 16) // stack3
        ), PURIFYING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(4, 4, 4, 12, 12, 16), // port
              box(0, 0, 4, 16, 4, 16), // base
              box(0, 4, 4, 7, 16, 16), // tank1
              box(9, 4, 4, 16, 16, 16), // tank2
              box(7, 12, 12, 9, 14, 13), // tube1
              box(7, 12, 9, 9, 14, 10), // tube2
              box(7, 12, 6, 9, 14, 7) // tube3
        ), INJECTING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 4, 16, 4, 16), // base
              box(2, 4, 4, 14, 13, 14), // body
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(4, 4, 14, 12, 12, 16), // port
              box(0, 13, 4, 16, 16, 16), // fin1
              box(0, 9, 4, 4, 12, 16), // fin2a
              box(12, 9, 4, 16, 12, 16), // fin2b
              box(0, 5, 4, 5, 8, 16), // fin3a
              box(11, 5, 4, 16, 8, 16) // fin3b
        ), INFUSING_FACTORY);

        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(0, 0, 0, 16, 16, 4), // front_panel
              box(4, 4, 14, 12, 12, 16), // port
              box(0, 5, 5, 7, 16, 16), // saw2
              box(9, 5, 5, 16, 16, 16), // saw1
              box(0, 0, 4, 16, 4, 16), // base
              box(1, 4, 4, 15, 15, 15) // core
        ), SAWING_FACTORY);
    }

    public static VoxelShape[] getShape(FactoryTier tier, FactoryType type) {
        return switch (type) {
            case SMELTING -> SMELTING_FACTORY;
            case ENRICHING -> ENRICHING_FACTORY;
            case CRUSHING -> CRUSHING_FACTORY;
            case COMPRESSING -> COMPRESSING_FACTORY;
            case COMBINING -> COMBINING_FACTORY;
            case PURIFYING -> PURIFYING_FACTORY;
            case INJECTING -> INJECTING_FACTORY;
            case INFUSING -> INFUSING_FACTORY;
            case SAWING -> SAWING_FACTORY;
        };
    }
}