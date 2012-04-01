/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.io.IOException;
import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.Aggregator;
import cascading.operation.Function;
import cascading.operation.aggregator.Count;
import cascading.operation.regex.RegexGenerator;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.scheme.Scheme;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import tools.ExampleTestCase;

/**
 *
 */
public class WordCountTest extends ExampleTestCase
  {
  public static class Main
    {
    }

  public void testBasicWordCount() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "wordcount";

    //@extract-start basic-word-count
    // define source and sink Taps.
    Scheme sourceScheme = new TextLine( new Fields( "line" ) );
    Tap source = new Hfs( sourceScheme, inputPath );

    Scheme sinkScheme = new TextLine( new Fields( "word", "count" ) );
    Tap sink = new Hfs( sinkScheme, outputPath, SinkMode.REPLACE );

    // the 'head' of the pipe assembly
    Pipe assembly = new Pipe( "wordcount" );

    // For each input Tuple
    // parse out each word into a new Tuple with the field name "word"
    // regular expressions are optional in Cascading
    String regex = "(?<!\\pL)(?=\\pL)[^ ]*(?<=\\pL)(?!\\pL)";
    Function function = new RegexGenerator( new Fields( "word" ), regex );
    assembly = new Each( assembly, new Fields( "line" ), function );

    // group the Tuple stream by the "word" value
    assembly = new GroupBy( assembly, new Fields( "word" ) );

    // For every Tuple group
    // count the number of occurrences of "word" and store result in
    // a field named "count"
    Aggregator count = new Count( new Fields( "count" ) );
    assembly = new Every( assembly, count );

    // initialize app properties, tell Hadoop which jar file to use
    Properties properties = new Properties();
    FlowConnector.setApplicationJarClass( properties, Main.class );

    // plan a new Flow from the assembly using the source and sink Taps
    // with the above properties
    FlowConnector flowConnector = new HadoopFlowConnector( properties );
    Flow flow = flowConnector.connect( "word-count", source, sink, assembly );

    // execute the flow, block until complete
    flow.complete();
    //@extract-end

    validateLength( flow, 197 );
    }

  }
