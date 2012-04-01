/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.io.IOException;

import cascading.flow.Flow;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.expression.ExpressionFilter;
import cascading.operation.expression.ExpressionFunction;
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
public class ExpressionTest extends ExampleTestCase
  {
  public void testExpressionFilter() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "expressionfilter";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe assembly = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    assembly = new Each( assembly, new Fields( "line" ), parser );

    //@extract-start expression-filter
    // incoming -> "ip", "time", "method", "event", "status", "size"

    ExpressionFilter filter =
      new ExpressionFilter( "status != 200", Integer.TYPE );

    assembly = new Each( assembly, new Fields( "status" ), filter );

    // outgoing -> "ip", "time", "method", "event", "status", "size"
    //@extract-end

    //@extract-start expression-function
    // incoming -> "ip", "time", "method", "event", "status", "size"

    String exp =
      "\"this \" + method + \" request was \" + size + \" bytes\"";
    Fields fields = new Fields( "pretty" );
    ExpressionFunction function =
      new ExpressionFunction( fields, exp, String.class );

    assembly =
      new Each( assembly, new Fields( "method", "size" ), function );

    // outgoing -> "pretty" = "this GET request was 1282652 bytes"
    //@extract-end

    Flow flow = new HadoopFlowConnector().connect( source, sink, assembly );

    flow.complete();

    validateLength( flow, 3 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "this GET request was 0 bytes", iterator.next().get( 1 ) );

    iterator.close();
    }

  }