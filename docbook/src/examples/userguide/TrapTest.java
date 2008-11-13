/*
 * Copyright (c) 2007-2008 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package userguide;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import tools.ExampleTestCase;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.tuple.Fields;
import cascading.tuple.TupleIterator;
import cascading.operation.regex.RegexParser;
import cascading.operation.Identity;
import cascading.operation.AssertionLevel;
import cascading.operation.assertion.AssertNotNull;
import cascading.operation.assertion.AssertSizeEquals;
import cascading.operation.assertion.AssertMatchesAll;
import cascading.operation.text.DateParser;
import cascading.operation.text.DateFormatter;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.tap.Tap;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.scheme.TextLine;

/**
 *
 */
public class TrapTest extends ExampleTestCase
  {
  public void testSetTraps() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "testtraps";
    String trapPath = getOutputPath() + "testtrap";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.Replace );
    Tap trap = new Hfs( new TextLine(), trapPath, SinkMode.Replace );

    Pipe assembly = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    assembly = new Each( assembly, new Fields( "line" ), parser );

    //@extract-start simple-traps

    // ...some useful pipes here

    // name this pipe assembly segment
    assembly = new Pipe( "assertions", assembly );

    AssertNotNull notNull = new AssertNotNull();
    assembly = new Each( assembly, AssertionLevel.STRICT, notNull );

    AssertSizeEquals equals = new AssertSizeEquals( 6 );
    assembly = new Each( assembly, AssertionLevel.STRICT, equals );

    AssertMatchesAll matchesAll = new AssertMatchesAll( "(GET|HEAD|POST)" );
    assembly = new Each( assembly, new Fields("method"),
                         AssertionLevel.STRICT, matchesAll );

    // ...some more useful pipes here

    Map<String,Tap> traps = new HashMap<String,Tap>();

    traps.put( "assertions", trap );

    FlowConnector flowConnector = new FlowConnector();
    Flow flow = flowConnector.connect( "log-parser", source, sink, traps, assembly );
    //@extract-end

    flow.complete();

    validateLength( flow, 10 );

    TupleIterator iterator = flow.openSink();

    assertEquals( "75.185.76.245\t01/Sep/2007:00:01:03 +0000\tPOST\t/mt-tb.cgi/235\t403\t174", iterator.next().get( 1 ) );

    iterator.close();
    }

  }