/*
 *  Copyright (c) 2007-2017 Xplenty, Inc. All Rights Reserved.
 *
 *  Project and contact information: http://www.cascading.org/
 *
 *  This file is part of the Cascading project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package examples.userguide;

import java.io.IOException;
import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.hadoop2.Hadoop2MR1FlowConnector;
import cascading.fluid.Fluid;
import cascading.operation.Aggregator;
import cascading.operation.Function;
import cascading.operation.aggregator.Count;
import cascading.operation.regex.RegexGenerator;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.property.AppProps;
import cascading.scheme.Scheme;
import cascading.scheme.hadoop.TextDelimited;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import org.junit.Test;
import tools.ExampleTestCase;

/**
 *
 */
public class WordCountTest extends ExampleTestCase
  {
  public static class Main
    {
    }

  @Test
  public void testBasicWordCount() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "wordcount";

    //@extract-start basic-word-count
    // define source and sink Taps.
    Scheme sourceScheme = new TextLine( new Fields( "line" ) );
    Tap source = new Hfs( sourceScheme, inputPath ); // <1>

    // the 'head' of the pipe assembly
    Pipe assembly = new Pipe( "wordcount" );

    // For each input Tuple
    // parse out each word into a new Tuple with the field name "word"
    // regular expressions are optional in Cascading
    String regex = "(?<!\\pL)(?=\\pL)[^ ]*(?<=\\pL)(?!\\pL)";
    Function function = new RegexGenerator( new Fields( "word" ), regex );
    assembly = new Each( assembly, new Fields( "line" ), function ); // <2>

    // group the Tuple stream by the "word" value
    assembly = new GroupBy( assembly, new Fields( "word" ) );  // <3>

    // For every Tuple group
    // count the number of occurrences of "word" and store result in
    // a field named "count"
    Aggregator count = new Count( new Fields( "count" ) );
    assembly = new Every( assembly, count );  // <4>

    Scheme sinkScheme = new TextDelimited( new Fields( "word", "count" ) );
    Tap sink = new Hfs( sinkScheme, outputPath, SinkMode.REPLACE ); // <5>

    // initialize app properties, tell Hadoop which jar file to use
    Properties properties = AppProps.appProps() // <6>
      .setName( "word-count-application" )
      .setJarClass( Main.class )
      .buildProperties();

    // plan a new Flow from the assembly using the source and sink Taps
    // with the above properties
    FlowConnector flowConnector = new Hadoop2MR1FlowConnector( properties ); // <7>
    Flow flow = flowConnector.connect( "word-count", source, sink, assembly ); // <8>

    // execute the flow, block until complete
    flow.complete(); // <9>
    //@extract-end

    validateLength( flow, 197 );
    }

  @Test
  public void testBasicWordCountFluid() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "wordcount";

    //@extract-start basic-word-count-fluid
    Scheme sourceScheme = new TextLine( new Fields( "line" ) );
    Tap source = new Hfs( sourceScheme, inputPath ); // <1>

    Pipe assembly = Fluid.assembly()
      .startBranch( "wordcount" )
      .each( new Fields( "line" ) ) // <2>
      .function(
        Fluid.function()
          .RegexGenerator()
          .fieldDeclaration( new Fields( "word" ) )
          .patternString( "(?<!\\pL)(?=\\pL)[^ ]*(?<=\\pL)(?!\\pL)" ).end()
      )
      .outgoing( Fields.RESULTS )
      .groupBy( new Fields( "word" ) ) // <3>
      .every( Fields.ALL ) // <4>
      .aggregator(
        Fluid.aggregator().Count( new Fields( "count" ) )
      )
      .outgoing( Fields.ALL )
      .completeGroupBy()
      .completeBranch();

    Scheme sinkScheme = new TextDelimited( new Fields( "word", "count" ) );
    Tap sink = new Hfs( sinkScheme, outputPath, SinkMode.REPLACE ); // <5>

    Properties properties = AppProps.appProps() // <6>
      .setName( "word-count-application" )
      .setJarClass( Main.class )
      .buildProperties();

    FlowConnector flowConnector = new Hadoop2MR1FlowConnector( properties ); // <7>
    Flow flow = flowConnector.connect( "word-count", source, sink, assembly ); // <8>

    flow.complete(); // <9>
    //@extract-end

    validateLength( flow, 197 );
    }
  }
