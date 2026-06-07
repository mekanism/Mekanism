package mekanism.common.tests.qio;

import java.util.Set;
import java.util.function.Supplier;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tests.MekanismTests;
import mekanism.common.tests.helpers.QIOGameTestHelper;
import mekanism.common.tests.util.StructureBuilderUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

//TODO - 26.1: Add tests to validate the state of the drive item after inventory modifications
//TODO - 26.1: Test to make sure items can get split among multiple drives, and then rolling back behaves as expected
@ForEachTest(groups = "qio.transactional")
public class TransactionalQIOTest {

    private static final String BASE_DRIVE = MekanismTests.MODID + ":base_qio_drive";
    //Note: Our template is lazy so that we ensure the QIO drive is registered
    @RegisterStructureTemplate(BASE_DRIVE)
    public static final Supplier<StructureTemplate> BASE_DRIVE_TEMPLATE = StructureTemplateBuilder.lazy(1, 1, 1, builder -> builder
          .set(0, 0, 0, MekanismBlocks.QIO_DRIVE_ARRAY.defaultState(), StructureBuilderUtils.withDrive(MekanismItems.BASE_QIO_DRIVE))
    );

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that frequency insertions don't work if there is no storage for the frequency.")
    public static void testQIOInsertNoStorage(final QIOGameTestHelper helper) {
        helper.startWithFrequency()
              .thenExecute(frequency -> {
                  try (Transaction simulation = Transaction.openRoot()) {
                      long inserted = frequency.massInsert(ItemResource.of(Items.STONE), 1_000, simulation);
                      if (inserted > 0) {
                          helper.fail("Inserting into a frequency with no backing storage should always fail");
                      }
                  }
              }).thenSucceed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that frequency extraction doesn't work if there is no storage for the frequency.")
    public static void testQIOExtractNoStorage(final QIOGameTestHelper helper) {
        helper.startWithFrequency()
              .thenExecute(frequency -> {
                  try (Transaction simulation = Transaction.openRoot()) {
                      long inserted = frequency.massExtract(ItemResource.of(Items.STONE), 1_000, simulation);
                      if (inserted > 0) {
                          helper.fail("Extracting from a frequency with no backing storage should always fail");
                      }
                  }
              }).thenSucceed();
    }

    @GameTest(template = BASE_DRIVE)
    @TestHolder(description = "Tests to make sure frequency insertions properly roll back when the transaction is not committed.")
    public static void testQIOSimulateInsert(final QIOGameTestHelper helper) {
        helper.startWithFrequency()
              .thenExecute(helper::addDrives)
              .thenExecute(frequency -> helper.testInsert(frequency, ItemResource.of(Items.STONE), 1_000, false))
              .thenSucceed();
    }

    @GameTest(template = BASE_DRIVE)
    @TestHolder(description = "Tests to make sure frequency insertions properly reflected after the transaction is committed.")
    public static void testQIOInsert(final QIOGameTestHelper helper) {
        helper.startWithFrequency()
              .thenExecute(helper::addDrives)
              .thenExecute(frequency -> helper.testInsert(frequency, ItemResource.of(Items.STONE), 1_000, true))
              .thenSucceed();
    }

    @GameTest(template = BASE_DRIVE)
    @TestHolder(description = "Tests to make sure frequency extractions properly roll back when the transaction is not committed.")
    public static void testQIOSimulateExtract(final QIOGameTestHelper helper) {
        helper.startWithFrequency()
              .thenExecute(helper::addDrives)
              .thenExecute(frequency -> helper.testExtract(frequency, ItemResource.of(Items.STONE), 1_000, false))
              .thenSucceed();
    }

    @GameTest(template = BASE_DRIVE)
    @TestHolder(description = "Tests to make sure frequency extractions are properly reflected after the transaction is committed.")
    public static void testQIOExtract(final QIOGameTestHelper helper) {
        helper.startWithFrequency()
              .thenExecute(helper::addDrives)
              .thenExecute(frequency -> helper.testExtract(frequency, ItemResource.of(Items.STONE), 1_000, true))
              .thenSucceed();
    }

    @GameTest(template = BASE_DRIVE)
    @TestHolder(description = "Tests to make sure that the frequency does not persist any unnecessary objects when a transaction is rolled back.")
    public static void testQIORollback(final QIOGameTestHelper helper) {
        helper.startWithFrequency()
              .thenExecute(helper::addDrives)
              .thenExecute(frequency -> {
                  ItemResource itemType = ItemResource.of(Items.STONE);
                  long initialAmount = 1_000;
                  int assertCall = 0;
                  try (Transaction transaction = Transaction.openRoot()) {
                      frequency.massInsert(itemType, initialAmount, transaction);
                      try (Transaction nonPersistent = Transaction.open(transaction)) {
                          frequency.massExtract(itemType, initialAmount, nonPersistent);
                          helper.assertHasCache(frequency, itemType, assertCall++);
                      }
                      helper.assertHasCache(frequency, itemType, assertCall++);
                      transaction.commit();
                  }
                  Set<QIODriveKey> driveKeys = frequency.getDriveKeys(itemType);
                  try (Transaction outer = Transaction.openRoot()) {
                      try (Transaction transaction = Transaction.open(outer)) {
                          frequency.massExtract(itemType, initialAmount, transaction);
                          helper.assertHasCache(frequency, itemType, assertCall++);
                          helper.assertHasDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                          helper.assertNoDriveCache(frequency, frequency.getDriveKeys(itemType), itemType, assertCall++);
                          try (Transaction nonPersistent = Transaction.open(transaction)) {
                              frequency.massInsert(itemType, initialAmount, nonPersistent);
                              helper.assertHasDriveCache(frequency, driveKeys, itemType, assertCall++);
                          }
                          helper.assertHasDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                          helper.assertNoDriveCache(frequency, frequency.getDriveKeys(itemType), itemType, assertCall++);
                          transaction.commit();
                      }
                      helper.assertHasCache(frequency, itemType, assertCall++);
                      helper.assertHasDriveCache(frequency, driveKeys, itemType, assertCall++);
                      helper.assertHasDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                      outer.commit();
                  }
                  helper.assertNotCached(frequency, itemType, assertCall++);
                  helper.assertNoDriveCache(frequency, driveKeys, itemType, assertCall++);
                  helper.assertNoDriveCache(frequency, frequency.getDriveKeys(itemType), itemType, assertCall++);

                  try (Transaction a = Transaction.openRoot()) {
                      frequency.massInsert(itemType, initialAmount, a);
                      helper.assertNoDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                      helper.assertHasCache(frequency, itemType, assertCall++);
                      try (Transaction b = Transaction.open(a)) {
                          frequency.massExtract(itemType, initialAmount, b);
                          helper.assertHasDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                          helper.assertHasCache(frequency, itemType, assertCall++);
                          try (Transaction c = Transaction.open(b)) {
                              frequency.massInsert(itemType, initialAmount, c);
                              helper.assertNoDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                              helper.assertHasCache(frequency, itemType, assertCall++);
                              try (Transaction d = Transaction.open(c)) {
                                  frequency.massExtract(itemType, initialAmount, d);
                                  helper.assertHasDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                                  helper.assertHasCache(frequency, itemType, assertCall++);
                              }
                              helper.assertNoDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                              helper.assertHasCache(frequency, itemType, assertCall++);
                          }
                          helper.assertHasDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                          helper.assertHasCache(frequency, itemType, assertCall++);
                      }
                      helper.assertNoDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                      helper.assertHasCache(frequency, itemType, assertCall++);
                  }
                  helper.assertNotCached(frequency, itemType, assertCall++);
                  helper.assertNoDriveEmpty(frequency, driveKeys, itemType, assertCall++);
                  helper.assertNoDriveCache(frequency, driveKeys, itemType, assertCall++);
              })
              .thenSucceed();
    }

}