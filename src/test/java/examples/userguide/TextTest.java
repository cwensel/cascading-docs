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

import cascading.flow.Flow;
import cascading.flow.hadoop2.Hadoop2MR1FlowConnector;
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
import org.junit.Test;
import tools.ExampleTestCase;

/**
 *
 */
public class TextTest extends ExampleTestCase
  {
  @Test
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

    Flow flow = new Hadoop2MR1FlowConnector().connect( source, sink, pipe );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "01/Sep/2007", iterator.next().getObject( 1 ) );

    iterator.close();
    }
  }