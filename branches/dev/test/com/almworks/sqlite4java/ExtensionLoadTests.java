package com.almworks.sqlite4java;


import java.io.File;
import java.util.Map;

public class ExtensionLoadTests extends SQLiteConnectionFixture {

//  static final String sep = File.separator;
//  static final File extensionFile = new File(".." + sep + "build" + sep + "extension_sample" + sep + "half.sqlext");
  private File myExtensionFile;

  public void testLoadFailWhenNotEnabled() throws SQLiteException {
//    System.out.println(arch);
//    for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
//                System.out.println(e);
//            }
//    assertFalse(true);

    SQLiteConnection connection = memDb().open();
    connection.enableLoadExtension(false);
    try {
      connection.loadExtension(myExtensionFile);
      fail("Extension load not enabled");
    } catch (SQLiteException e) {

    }
  }

  public void testExtensionLoad() throws SQLiteException {
    final int number = 8;
    SQLiteConnection connection = memDb().open();
//    File extensionFile = new File(".." + sep + "build" + sep + "native_tests" + sep + "half.sqlext");
    connection.enableLoadExtension(true);
    connection.loadExtension(myExtensionFile);
//   new File("").;
    SQLiteStatement stm = connection.prepare("select half(?)").bind(1, number);
    stm.step();
    int half = stm.columnInt(0);
    assertEquals(number / 2, half);
    stm.dispose();
    connection.dispose();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    String arch = System.getProperty("os.arch");
    String fileSuffix = "." + arch.substring(arch.length() - 2);
    String sep = File.separator;
    myExtensionFile = new File(".." + sep + "build" + sep + "extension_sample" + sep + "half.sqlext" + fileSuffix);
//    System.out.println(arch);

  }
}
