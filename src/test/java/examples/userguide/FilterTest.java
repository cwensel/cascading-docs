/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package examples.userguide;

import java.io.IOException;

import cascading.flow.Flow;
import cascading.flow.hadoop2.Hadoop2MR1FlowConnector;
import cascading.operation.filter.And;
import cascading.operation.filter.FilterNull;
import cascading.operation.regex.RegexFilter;
import cascading.operation.regex.RegexParser;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntryIterator;
import org.junit.Test;
import tools.ExampleTestCase;

/**
 *
 */
public class FilterTest extends ExampleTestCase
  {
  @Test
  public void testAnd() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "createtimestamp";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe assembly = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    assembly = new Each( assembly, new Fields( "line" ), parser );

    //@extract-start filter-and
    // incoming -> "ip", "time", "method", "event", "status", "size"

    FilterNull filterNull = new FilterNull();
    RegexFilter regexFilter = new RegexFilter( "(GET|HEAD|POST)" );

    And andFilter = new And( filterNull, regexFilter );

    assembly = new Each( assembly, new Fields( "method" ), andFilter );

    // outgoing -> "ip", "time", "method", "event", "status", "size"
    //@extract-end

    Flow flow = new Hadoop2MR1FlowConnector().connect( source, sink, assembly );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "75.185.76.245\t01/Sep/2007:00:01:03 +0000\tPOST\t/mt-tb.cgi/235\t403\t174", iterator.next().getObject( 1 ) );

    iterator.close();
    }
  }