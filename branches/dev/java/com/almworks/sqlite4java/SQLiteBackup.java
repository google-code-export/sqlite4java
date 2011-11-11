package com.almworks.sqlite4java;


import static com.almworks.sqlite4java.SQLiteConstants.*;

public class SQLiteBackup {

  private SWIGTYPE_p_sqlite3_backup myHandle;

  private SQLiteConnection myDestination;

  SQLiteBackup(SWIGTYPE_p_sqlite3_backup handler, SQLiteConnection destination) {
    myHandle = handler;
    myDestination = destination;
  }

  public int step(int pagesToBackup) throws SQLiteException, SQLiteBusyException {
    SWIGTYPE_p_sqlite3_backup handler = handle();
    int rc = _SQLiteSwigged.sqlite3_backup_step(handler, pagesToBackup);

    if (rc == SQLITE_BUSY || rc == SQLITE_LOCKED) {
      throw new SQLiteBusyException(rc, null);
    } else if (rc != SQLITE_OK || rc != SQLITE_DONE) {
      throw new SQLiteException(rc, null);
    }

    return rc;
  }

  public SQLiteConnection getDestinationDB() {
    return myDestination;
  }

  public void dispose(boolean disposeDestDB) {
    if (myHandle != null) {
      _SQLiteSwigged.sqlite3_backup_finish(myHandle);
      myHandle = null;
    }
    if (disposeDestDB) {
      myDestination.dispose();
    }
  }

  public void dispose() {
    dispose(false);
  }

  public int getPagecount() throws SQLiteException {
    SWIGTYPE_p_sqlite3_backup handle = handle();
    return _SQLiteSwigged.sqlite3_backup_pagecount(handle);
  }

  public int getRemaining() throws SQLiteException {
    SWIGTYPE_p_sqlite3_backup handle = handle();
    return _SQLiteSwigged.sqlite3_backup_remaining(handle);
  }

  private SWIGTYPE_p_sqlite3_backup handle() throws SQLiteException {
    SWIGTYPE_p_sqlite3_backup handle = myHandle;
    if (handle == null) {
      throw new SQLiteException(WRAPPER_BACKUP_DISPOSED, null);
    }
    return handle;
  }
}
