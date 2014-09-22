/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.sample;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openjdk.jmh.annotations.Level.Invocation;
import static org.openjdk.jmh.annotations.Level.Trial;

@Fork(1)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 6, timeUnit = TimeUnit.SECONDS)
public class BulkLoadBenchmark {
  private final Random RAND = new Random();
  private final Blackhole bh = new Blackhole();

  private int count = 1000 * 1000;
  private SQLiteStatement statement;
  private SQLiteConnection myDB;

  @Setup(Trial)
  public void inic0() {
    Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
    Logger.getLogger("sqlite").setLevel(Level.OFF);
  }

  @Setup(Invocation)
  public void inic() throws Exception {
    myDB = new SQLiteConnection(new File("temp.db")).open();
//    myDB = new SQLiteConnection(File.createTempFile("BulkLoadBenchmark_database", "tmp")).open();
    myDB.exec("drop table if exists x");
    myDB.exec("create table x (id integer not null primary key)");
    myDB.exec("begin");
    SQLiteStatement st = myDB.prepare("insert into x values(?)");
    for (int i = 0; i < count; i++) {
      st.bind(1, RAND.nextLong());
      st.step();
      st.reset();
    }
    st.dispose();
    myDB.exec("commit");
    statement = myDB.prepare("select id from x");
//    statement = myDB.prepare("select id from x order by (50000-id)*(25000-id)");
  }

  @TearDown(Invocation)
  public void tearDown() throws Exception {
    myDB.dispose();
  }

  @Benchmark
  public void testReadSingle() throws SQLiteException {
    while (statement.step()) {
      bh.consume(statement.columnLong(0));
    }
  }

  @Benchmark
  public void testBulkRead1000() throws SQLiteException {
    int loaded;
    long[] buffer = new long[1000];
    while ((loaded = statement.loadLongs(0, buffer, 0, buffer.length)) > 0) {
      for (int i = 0; i < loaded; i++) {
        bh.consume(buffer[i]);
      }
    }
  }

  @Benchmark
  public void testBulkRead10000() throws SQLiteException {
    int loaded;
    long[] buffer = new long[10000];
    while ((loaded = statement.loadLongs(0, buffer, 0, buffer.length)) > 0) {
      for (int i = 0; i < loaded; i++) {
        bh.consume(buffer[i]);
      }
    }
  }

  @Benchmark
  public void testBulkRead100000() throws SQLiteException {
    int loaded;
    long[] buffer = new long[100000];
    while ((loaded = statement.loadLongs(0, buffer, 0, buffer.length)) > 0) {
      for (int i = 0; i < loaded; i++) {
        bh.consume(buffer[i]);
      }
    }
  }

}
