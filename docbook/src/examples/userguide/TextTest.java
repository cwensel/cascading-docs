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

import tools.ExampleTestCase;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.tuple.Fields;
import cascading.tuple.TupleIterator;
import cascading.tuple.TupleEntryIterator;
import cascading.operation.regex.RegexParser;
import cascading.operation.Identity;
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
      new DateFormatter( new Fields("date"), "dd/MMM/yyyy" );
    pipe = new Each( pipe, new Fields( "ts" ), formatter );

    // outgoing -> "date" -> 31/Aug/2007
    //@extract-end

    Flow flow = new FlowConnector().connect( source, sink, pipe );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "31/Aug/2007", iterator.next().get( 1 ) );

    iterator.close();
    }

  }