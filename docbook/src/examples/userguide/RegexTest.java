/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.io.IOException;

import cascading.flow.Flow;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.Filter;
import cascading.operation.Function;
import cascading.operation.regex.RegexFilter;
import cascading.operation.regex.RegexGenerator;
import cascading.operation.regex.RegexParser;
import cascading.operation.regex.RegexReplace;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.scheme.Scheme;
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
public class RegexTest extends ExampleTestCase
  {
  public void testRegexParser() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "regexparser";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe assembly = new Pipe( "logs" );

    //@extract-start regex-parser
    // incoming -> "line"

    String regex =
      "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +" +
        "\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration =
      new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    assembly = new Each( assembly, new Fields( "line" ), parser );

    // outgoing -> "ip", "time", "method", "event", "status", "size"
    //@extract-end


    //@extract-start regex-filter
    // incoming -> "ip", "time", "method", "event", "status", "size"

    Filter filter = new RegexFilter( "^68\\..*" );
    assembly = new Each( assembly, new Fields( "ip" ), filter );

    // outgoing -> "ip", "time", "method", "event", "status", "size"
    //@extract-end

    Flow flow = new HadoopFlowConnector().connect( source, sink, assembly );

    flow.complete();

    validateLength( flow, 3 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "68.46.103.112\t01/Sep/2007:00:01:17 +0000\tPOST\t/mt-tb.cgi/92\t403\t174", iterator.next().get( 1 ) );

    iterator.close();
    }

  public void testRegexGenerator() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "regexgenerator";

    // define source and sink Taps.
    Scheme sourceScheme = new TextLine( new Fields( "line" ) );
    Tap source = new Hfs( sourceScheme, inputPath );

    Scheme sinkScheme = new TextLine( new Fields( "word", "count" ) );
    Tap sink = new Hfs( sinkScheme, outputPath, true );

    // the 'head' of the pipe assembly
    Pipe assembly = new Pipe( "wordcount" );


    //@extract-start regex-generator
    // incoming -> "line"

    String regex = "(?<!\\pL)(?=\\pL)[^ ]*(?<=\\pL)(?!\\pL)";
    Function function = new RegexGenerator( new Fields( "word" ), regex );
    assembly = new Each( assembly, new Fields( "line" ), function );

    // outgoing -> "word"
    //@extract-end

    // plan a new Flow from the assembly using the source and sink Taps
    Flow flow = new HadoopFlowConnector().connect( source, sink, assembly );

    // execute the flow, block until complete
    flow.complete();

    validateLength( flow, 1521 );
    }

  public void testRegexReplace() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "regexreplace";

    // define source and sink Taps.
    Scheme sourceScheme = new TextLine( new Fields( "line" ) );
    Tap source = new Hfs( sourceScheme, inputPath );

    Scheme sinkScheme = new TextLine( new Fields( "word", "count" ) );
    Tap sink = new Hfs( sinkScheme, outputPath, true );

    // the 'head' of the pipe assembly
    Pipe assembly = new Pipe( "wordcount" );


    //@extract-start regex-replace
    // incoming -> "line"

    RegexReplace replace =
      new RegexReplace( new Fields( "clean-line" ), "\\s+", " ", true );
    assembly = new Each( assembly, new Fields( "line" ), replace );

    // outgoing -> "clean-line"
    //@extract-end

    // plan a new Flow from the assembly using the source and sink Taps
    Flow flow = new HadoopFlowConnector().connect( source, sink, assembly );

    // execute the flow, block until complete
    flow.complete();

    validateLength( flow, 4 );

    TupleEntryIterator iterator = flow.openSink();

    assertTrue( iterator.next().getString( 1 ).startsWith( "Lorem ipsum dolor sit amet, consectetuer adip" ) );

    iterator.close();

    }

  }