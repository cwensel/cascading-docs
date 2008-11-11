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
import cascading.operation.regex.RegexParser;
import cascading.operation.Identity;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.tap.Tap;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.scheme.TextLine;

/**
 *
 */
public class IdentityTest extends ExampleTestCase
  {
  public void testIdentityPatterns() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "identity";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.Replace );

    Pipe pipe = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    pipe = new Each( pipe, new Fields( "line" ), parser );

    //@extract-start identity-discard-fields-long
    // incoming -> "ip", "time", "method", "event", "status", "size"

    pipe = new Each( pipe, new Fields( "ip", "method" ), new Identity( Fields.ARGS ), Fields.RESULTS );

    // outgoing -> "ip", "method"
    //@extract-end

    //@extract-start identity-discard-fields
    // incoming -> "ip", "time", "method", "event", "status", "size"

    pipe = new Each( pipe, new Fields( "ip", "method" ), new Identity() );

    // outgoing -> "ip", "method"
    //@extract-end

    //@extract-start identity-rename-fields-explicit
    // incoming -> "ip", "method"

    pipe = new Each( pipe, new Fields( "ip", "method" ), new Identity( new Fields( "address", "request" ) ) );

    // outgoing -> "address", "request"
    //@extract-end

    //@extract-start identity-rename-fields-long
    // incoming -> "ip", "method"

    pipe = new Each( pipe, Fields.ALL, new Identity( new Fields( "address", "request" ) ) );

    // outgoing -> "address", "request"
    //@extract-end

    //@extract-start identity-rename-fields
    // incoming -> "ip", "method"

    pipe = new Each( pipe, new Identity( new Fields( "address", "request" ) ) );

    // outgoing -> "address", "request"
    //@extract-end

    Flow flow = new FlowConnector().connect( source, sink, pipe );

    flow.complete();

    validateLength( flow, 10 );

    TupleIterator iterator = flow.openSink();

    assertEquals( "75.185.76.245\tPOST", iterator.next().get( 1 ) );

    iterator.close();

    }

  }