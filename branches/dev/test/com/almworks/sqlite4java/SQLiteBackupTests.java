package com.almworks.sqlite4java;

import static com.almworks.sqlite4java.SQLiteConstants.*;
import java.io.File;

public class SQLiteBackupTests extends SQLiteConnectionFixture{

//  public void testOneStepBackUp() {

//  }
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
    assertStepFails(backup);

    source = createDB(true);
    backup = source.initializeBackup(null);
    SQLiteConnection destination = backup.getDestinationConnection();
    destination.dispose();
    assertStepFails(backup);
    source.dispose();

    source = createDB(true);
    backup = source.initializeBackup(null);
    destination = backup.getDestinationConnection();
    source.dispose();
    destination.dispose();
    assertStepFails(backup);

//    backup.dispose();
//    assertEquals(12,13);
  }


  private SQLiteConnection createDB(boolean inMemory) throws SQLiteException {
    SQLiteConnection connection = inMemory ? memDb() : fileDb();
    connection = connection.open().exec("create table tab (first integer, last integer)");
    long first = 13;
    long last = 99;
    SQLiteStatement statement = connection.prepare("insert into tab values (13, 99)");
    statement.step();
    statement.dispose();
    return connection;
  }

  private void assertDBAndModelEquals(SQLiteConnection backup) throws SQLiteException {
    long first = 13;
    long last = 99;
    SQLiteStatement statement = backup.prepare("select * from tab");
    statement.step();
    assertEquals(first, statement.columnLong(0));
    assertEquals(last, statement.columnLong(1));
    statement.dispose();
  }

  private void backupOneStep(boolean sourceInMemory, File destinationFile) throws SQLiteException {
    SQLiteConnection source = createDB(sourceInMemory);
    SQLiteBackup backup = source.initializeBackup(destinationFile);

    SQLiteConnection destination = backup.getDestinationConnection();
    boolean finished = backup.step(-1);
    assertEquals(true, finished);

    backup.dispose(false);
    source.dispose();
    assertDBAndModelEquals(destination);
    destination.dispose();
  }

  private void assertStepFails(SQLiteBackup backup){
    try {
      backup.step(-1);
      fail("Backup disposed DB");
    } catch (SQLiteException e) {
      assertEquals(e.getErrorCode(), WRAPPER_CONFINEMENT_VIOLATED);
    }
  }

}
