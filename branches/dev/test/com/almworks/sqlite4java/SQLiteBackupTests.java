package com.almworks.sqlite4java;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

import static com.almworks.sqlite4java.SQLiteConstants.WRAPPER_CONFINEMENT_VIOLATED;

public class SQLiteBackupTests extends SQLiteConnectionFixture {

  private static final int ROWS_NUMBER = 5400;

  public void testOneStepBackupMemoryToFile() throws SQLiteException {
    backupOneStep(true, new File(tempName("db")));
  }

  public void testOneStepBackupMemoryToMemory() throws SQLiteException {
    backupOneStep(true, null);
  }


  public void testOneStepBackupFileToFile() throws SQLiteException {
    backupOneStep(false, new File(tempName("db1")));
  }

  public void testOneStepBackupFileToMemory() throws SQLiteException {
    backupOneStep(false, null);
  }

  public void testStepFailWhenConnectionDisposed() throws SQLiteException {
    SQLiteConnection source = createDB(true);
    SQLiteBackup backup = source.initializeBackup(null);

    source.dispose();
    assertStepFailsWithError(backup, WRAPPER_CONFINEMENT_VIOLATED);

    source = createDB(true);
    backup = source.initializeBackup(null);
    SQLiteConnection destination = backup.getDestinationConnection();
    destination.dispose();
    assertStepFailsWithError(backup, WRAPPER_CONFINEMENT_VIOLATED);
    source.dispose();

    source = createDB(true);
    backup = source.initializeBackup(null);
    destination = backup.getDestinationConnection();
    source.dispose();
    destination.dispose();
    assertStepFailsWithError(backup, WRAPPER_CONFINEMENT_VIOLATED);
  }

  public void testDestinationAutoUpdate() throws SQLiteException {
    SQLiteConnection source = createDB(false);

    SQLiteBackup backup = source.initializeBackup(null);
    SQLiteConnection destination = backup.getDestinationConnection();
    boolean finished = backup.step(10);
    assertFalse(finished);

    int oldPageCount = backup.getPageCount();
    int oldRemaining = backup.getRemaining();

    modifyDB(source);

    int nPages = 1;
    finished = backup.step(nPages);
    assertFalse(finished);

    int newPageCount = backup.getPageCount();
    int newRemaining = backup.getRemaining();
    assertEquals(oldRemaining - nPages + (newPageCount - oldPageCount), newRemaining);

    backup.step(-1);
    backup.dispose(false);

    assertDBSEquals(source, destination);

    source.dispose();
    destination.dispose();
  }

  public void testBackupRestarting() throws SQLiteException {
    SQLiteConnection source = createDB(false);
    File sourceDBFile = source.getDatabaseFile();
    SQLiteConnection anotherConnectionToSource = new SQLiteConnection(sourceDBFile).open();

    SQLiteBackup backup = source.initializeBackup(null);
    SQLiteConnection destination = backup.getDestinationConnection();
    boolean finished = backup.step(10);
    assertFalse(finished);

    int oldPageCount = backup.getPageCount();
    int oldRemaining = backup.getRemaining();

    modifyDB(anotherConnectionToSource);

    int nPages = 1;
    finished = backup.step(nPages);
    assertFalse(finished);

    int newPageCount = backup.getPageCount();
    int newRemaining = backup.getRemaining();

    assertTrue(newPageCount >= oldPageCount);
    assertEquals(newRemaining, newPageCount - nPages);

    backup.step(-1);
    backup.dispose(false);

    assertDBSEquals(source, destination);
    assertDBSEquals(anotherConnectionToSource, destination);

    source.dispose();
    destination.dispose();
    anotherConnectionToSource.dispose();
  }

  public void testBackupWithSharingLockOnSource() throws SQLiteException {
    SQLiteConnection source = createDB(false);
    SQLiteConnection anotherConnectionToSource = new SQLiteConnection(source.getDatabaseFile()).open();

    SQLiteBackup backup = source.initializeBackup(null);
    SQLiteConnection destination = backup.getDestinationConnection();

    SQLiteStatement select = anotherConnectionToSource.prepare("select * from tab");
    select.step();
    backup.step(-1);
    backup.dispose(false);

    assertDBSEquals(source, destination);

    source.dispose();
    destination.dispose();
    anotherConnectionToSource.dispose();
  }

  public void testFailWithoutExclusiveLock() throws SQLiteException {
    SQLiteConnection source = createDB(false);

    SQLiteBackup backup = source.initializeBackup(null);

    source.exec("begin immediate");
    SQLiteStatement insert = source.prepare("insert into tab values (?)").bind(1, ROWS_NUMBER + 15);
    insert.step();

    try {
      backup.step(-1);
      fail("Backup without exclusive lock");
    } catch (SQLiteBusyException e) {
      //ok
    }
    insert.dispose();
    backup.dispose();

    source.dispose();
  }

  public void testBla() throws SQLiteException {
    SQLiteConnection source = createDB(true);
    SQLiteBackup backup = source.initializeBackup(null);
    SQLiteConnection destination = backup.getDestinationConnection();
    destination.dispose();
    backup.dispose();
    assertEquals(12,12);
  }

  private SQLiteConnection createDB(boolean inMemory) throws SQLiteException {
    SQLiteConnection connection = inMemory ? memDb() : fileDb();
    connection = connection.open().exec("create table tab (val integer)");

    SQLiteStatement statement = connection.prepare("insert into tab values (?)");

    //Setting log level to WARNING because FINE logging cause many useless and similarly identical messages/ that crash JUnit
    Level previousLevel = java.util.logging.Logger.getLogger("com.almworks.sqlite4java").getLevel();
    java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.WARNING);
    connection.exec("begin immediate");
    for (long i = 0; i < ROWS_NUMBER; i++) {
      statement.bind(1, i);
      statement.step();
      statement.reset();
    }
    java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(previousLevel);
    connection.exec("commit");
    statement.dispose();

    return connection;
  }


  private void modifyDB(SQLiteConnection connection) throws SQLiteException {
    SQLiteStatement modifyStatement = connection.prepare("insert into tab values(?)");
    connection.exec("begin immediate");
    for (int i = 1; i < 400; i++) {
      modifyStatement.bind(1, ROWS_NUMBER + i);
      modifyStatement.step();
      modifyStatement.reset();
    }
    connection.exec("commit");
    modifyStatement.dispose();
  }

  private void assertDBSEquals(SQLiteConnection source, SQLiteConnection backup) throws SQLiteException {
    long sourceValues[] = new long[ROWS_NUMBER];
    long backupValues[] = new long[ROWS_NUMBER];
    String selectStatement = "select val from tab";
    SQLiteStatement sourceStatement = source.prepare(selectStatement);
    SQLiteStatement backupStatement = backup.prepare(selectStatement);

    sourceStatement.loadLongs(0, sourceValues, 0, sourceValues.length);
    backupStatement.loadLongs(0, backupValues, 0, backupValues.length);

    backupStatement.dispose();
    sourceStatement.dispose();
    assertTrue(Arrays.equals(sourceValues, backupValues));
  }

  private void backupOneStep(boolean sourceInMemory, File destinationFile) throws SQLiteException {
    SQLiteConnection source = createDB(sourceInMemory);
    SQLiteBackup backup = source.initializeBackup(destinationFile);

    SQLiteConnection destination = backup.getDestinationConnection();
    boolean finished = backup.step(-1);
    assertTrue(finished);

    backup.dispose(false);
    assertDBSEquals(source, destination);
    source.dispose();
    destination.dispose();
  }

  private void assertStepFailsWithError(SQLiteBackup backup, int errorCode) {
    try {
      backup.step(-1);
      fail("Backup disposed DB");
    } catch (SQLiteException e) {
      assertEquals(e.getErrorCode(), errorCode);
    }
  }

}
