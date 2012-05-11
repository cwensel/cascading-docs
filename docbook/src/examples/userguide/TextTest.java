/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.io.IOException;

import cascading.flow.Flow;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.regex.RegexParser;
import cascading.operation.text.DateFormatter;
import cascading.operation.text.DateParser;
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
public class TextTest extends ExampleTestCase
  {
  public void testCreateTimestamp() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "createtimestamp";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe pipe = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    pipe = new Each( pipe, new Fields( "line" ), parser );

    //@extract-start text-create-timestamp
    // "time" -> 01/Sep/2007:00:01:03 +0000

    DateParser dateParser = new DateParser( "dd/MMM/yyyy:HH:mm:ss Z" );
    pipe = new Each( pipe, new Fields( "time" ), dateParser );

    // outgoing -> "ts" -> 1188604863000
    //@extract-end

    //@extract-start text-format-date
    // "ts" -> 1188604863000

    DateFormatter formatter =
      new DateFormatter( new Fields( "date" ), "dd/MMM/yyyy" );
    pipe = new Each( pipe, new Fields( "ts" ), formatter );

    // outgoing -> "date" -> 31/Aug/2007
    //@extract-end

    Flow flow = new HadoopFlowConnector().connect( source, sink, pipe );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "01/Sep/2007", iterator.next().get( 1 ) );

    iterator.close();
    }
  }