package com.almworks.sqlite4java;


import static com.almworks.sqlite4java.SQLiteConstants.SQLITE_DONE;
import static com.almworks.sqlite4java.SQLiteConstants.WRAPPER_BACKUP_DISPOSED;

public class SQLiteBackup {

  private SWIGTYPE_p_sqlite3_backup myHandle;

  private SQLiteConnection myDestination;

  private SQLiteController myDestinationController;

  private SQLiteController mySourceController;

  SQLiteBackup(SQLiteController sourceController, SQLiteController destinationController, SWIGTYPE_p_sqlite3_backup handler, SQLiteConnection destination) {
    mySourceController = sourceController;
    myDestinationController = destinationController;
    myHandle = handler;
    myDestination = destination;
    Internal.logFine(this, "instantiated");
  }

  public boolean step(int pagesToBackup) throws SQLiteException, SQLiteBusyException {
    mySourceController.validate();
    myDestinationController.validate();

    if (Internal.isFineLogging()) {
      Internal.logFine(this, "step(" + pagesToBackup + ")");
    }

    SWIGTYPE_p_sqlite3_backup handler = handle();

    int rc = _SQLiteSwigged.sqlite3_backup_step(handler, pagesToBackup);
    throwResult(rc, "Backup step failed");

    boolean finished = false;
    if (rc == SQLITE_DONE) {
      finished = true;
    }

    return finished;
  }

  public SQLiteConnection getDestinationConnection() {
    return myDestination;
  }

  public void dispose(boolean disposeDestinationConnection) {
    if (disposeDestinationConnection) {
      myDestination.dispose();
    }
    try {
      mySourceController.validate();
    } catch (SQLiteException e) {
      Internal.recoverableError(this, "invalid dispose: " + e, true);
      return;
    }
    Internal.logFine(this, "disposing");
    if (myHandle != null) {
      _SQLiteSwigged.sqlite3_backup_finish(myHandle);
      myHandle = null;
      mySourceController = myDestinationController = SQLiteController.getDisposed(mySourceController);
    }

  }

  public void dispose() {
    dispose(true);
  }

  public int getPageCount() throws SQLiteException {
    mySourceController.validate();
    myDestinationController.validate();

    SWIGTYPE_p_sqlite3_backup handle = handle();
    return _SQLiteSwigged.sqlite3_backup_pagecount(handle);
  }

  public int getRemaining() throws SQLiteException {
    mySourceController.validate();
    myDestinationController.validate();

    SWIGTYPE_p_sqlite3_backup handle = handle();
    return _SQLiteSwigged.sqlite3_backup_remaining(handle);
  }

  @Override
  public String toString() {

    return "Backup [ ->" + myDestination + "]";
  }

  private SWIGTYPE_p_sqlite3_backup handle() throws SQLiteException {
    SWIGTYPE_p_sqlite3_backup handle = myHandle;
    if (handle == null) {
      throw new SQLiteException(WRAPPER_BACKUP_DISPOSED, null);
    }
    return handle;
  }

  private void throwResult(int rc, String operation) throws SQLiteException {
    if (rc == SQLITE_DONE) return;
    myDestination.throwResult(rc, operation);
  }
}
