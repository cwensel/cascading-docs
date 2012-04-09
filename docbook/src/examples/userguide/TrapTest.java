/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.AssertionLevel;
import cascading.operation.assertion.AssertMatchesAll;
import cascading.operation.assertion.AssertNotNull;
import cascading.operation.assertion.AssertSizeEquals;
import cascading.operation.regex.RegexParser;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntryIterator;
import tools.ExampleTestCase;

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
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );
    Tap trap = new Hfs( new TextLine(), trapPath, SinkMode.REPLACE );

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
    assembly =
      new Each( assembly, new Fields( "method" ), AssertionLevel.STRICT, matchesAll );

    // ...some more useful pipes here

    FlowDef flowDef = new FlowDef();

    flowDef
      .setName( "log-parser" )
      .addSource( "logs", source )
      .addTailSink( assembly, sink );

    // set the trap on the "assertions" branch
    flowDef
      .addTrap( "assertions", trap );

    FlowConnector flowConnector = new HadoopFlowConnector();
    Flow flow =
      flowConnector.connect( flowDef );
    //@extract-end

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "75.185.76.245\t01/Sep/2007:00:01:03 +0000\tPOST\t/mt-tb.cgi/235\t403\t174", iterator.next().get( 1 ) );

    iterator.close();
    }

  }