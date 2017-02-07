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
import cascading.flow.FlowDef;
import cascading.flow.hadoop2.Hadoop2MR1FlowConnector;
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
import org.junit.Test;
import tools.ExampleTestCase;

/**
 *
 */
public class AssertionTest extends ExampleTestCase
  {
  @Test
  public void testAssertions() throws IOException
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

    //@extract-start simple-assertion
    // incoming -> "ip", "time", "method", "event", "status", "size"

    AssertNotNull notNull = new AssertNotNull();
    assembly = new Each( assembly, AssertionLevel.STRICT, notNull );

    AssertSizeEquals equals = new AssertSizeEquals( 6 );
    assembly = new Each( assembly, AssertionLevel.STRICT, equals );

    AssertMatchesAll matchesAll = new AssertMatchesAll( "(GET|HEAD|POST)" );
    assembly = new Each( assembly, new Fields( "method" ),
      AssertionLevel.STRICT, matchesAll );

    // outgoing -> "ip", "time", "method", "event", "status", "size"
    //@extract-end

    Properties properties = new Properties();

//    FlowConnector.setAssertionLevel( properties, AssertionLevel.NONE );

    Flow flow = new Hadoop2MR1FlowConnector( properties ).connect( source, sink, assembly );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "75.185.76.245\t01/Sep/2007:00:01:03 +0000\tPOST\t/mt-tb.cgi/235\t403\t174", iterator.next().getObject( 1 ) );

    iterator.close();
    }

  public void compileAssertions() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "createtimestamp";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe assembly = new Pipe( "logs" );

    //@extract-start simple-assertion-planner
    // FlowDef is a fluent way to define a Flow
    FlowDef flowDef = new FlowDef();

    // bind the taps and pipes
    flowDef
      .addSource( assembly.getName(), source )
      .addSink( assembly.getName(), sink )
      .addTail( assembly );

    // removes all assertions from the Flow
    flowDef
      .setAssertionLevel( AssertionLevel.NONE );

    Flow flow = new Hadoop2MR1FlowConnector().connect( flowDef );
    //@extract-end
    }

  }