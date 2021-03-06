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
import cascading.operation.Identity;
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
public class IdentityTest extends ExampleTestCase
  {
  @Test
  public void testIdentityRenameAll() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "identityall";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe pipe = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    pipe = new Each( pipe, new Fields( "line" ), parser );

    {
    //@extract-start identity-discard-fields-long
    // incoming -> "ip", "time", "method", "event", "status", "size"

    Identity identity = new Identity( Fields.ARGS );
    Fields ipMethod = new Fields( "ip", "method" );
    pipe =
      new Each( pipe, ipMethod, identity, Fields.RESULTS );

    // outgoing -> "ip", "method"
    //@extract-end
    }

    //@extract-start identity-discard-fields
    // incoming -> "ip", "time", "method", "event", "status", "size"

    pipe = new Each( pipe, new Fields( "ip", "method" ), new Identity() );

    // outgoing -> "ip", "method"
    //@extract-end

    {
    //@extract-start identity-rename-fields-explicit
    // incoming -> "ip", "method"

    Identity identity = new Identity( new Fields( "address", "request" ) );
    pipe = new Each( pipe, new Fields( "ip", "method" ), identity );

    // outgoing -> "address", "request"
    //@extract-end
    }

    {
    //@extract-start identity-rename-fields-long
    // incoming -> "ip", "method"

    Identity identity = new Identity( new Fields( "address", "request" ) );
    pipe = new Each( pipe, Fields.ALL, identity );

    // outgoing -> "address", "request"
    //@extract-end
    }

    {
    //@extract-start identity-rename-fields
    // incoming -> "ip", "method"

    Identity identity = new Identity( new Fields( "address", "request" ) );
    pipe = new Each( pipe, identity );

    // outgoing -> "address", "request"
    //@extract-end
    }

    Flow flow = new Hadoop2MR1FlowConnector().connect( source, sink, pipe );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "75.185.76.245\tPOST", iterator.next().getObject( 1 ) );

    iterator.close();
    }

  @Test
  public void testIdentityRenameSome() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "identitysome";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe pipe = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    pipe = new Each( pipe, new Fields( "line" ), parser );

    //@extract-start identity-rename-some
    // incoming -> "ip", "time", "method", "event", "status", "size"

    Fields fieldSelector = new Fields( "address", "method" );
    Identity identity = new Identity( new Fields( "address" ) );
    pipe = new Each( pipe, new Fields( "ip" ), identity, fieldSelector );

    // outgoing -> "address", "method"
    //@extract-end

    Flow flow = new Hadoop2MR1FlowConnector().connect( source, sink, pipe );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "75.185.76.245\tPOST", iterator.next().getObject( 1 ) );

    iterator.close();
    }

  @Test
  public void testIdentityCoerce() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "identitysome";

    Tap source = new Hfs( new TextLine(), inputPath );
    Tap sink = new Hfs( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe pipe = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    pipe = new Each( pipe, new Fields( "line" ), parser );

    {
    //@extract-start identity-coerce
    // incoming -> "ip", "time", "method", "event", "status", "size"

    Identity identity = new Identity( Integer.TYPE, Long.TYPE );
    pipe = new Each( pipe, new Fields( "status", "size" ), identity );

    // outgoing -> "status", "size"
    //@extract-end
    }

    {
    //@extract-start identity-coerce-single
    // incoming -> "ip", "time", "method", "event", "status", "size"

    Identity identity = new Identity( Integer.TYPE );
    pipe =
      new Each( pipe, new Fields( "status" ), identity, Fields.REPLACE );

    // outgoing -> "ip", "time", "method", "event", "status", "size"
    //@extract-end
    }

    Flow flow = new Hadoop2MR1FlowConnector().connect( source, sink, pipe );

    flow.complete();

    validateLength( flow, 10 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "403\t174", iterator.next().getObject( 1 ) );

    iterator.close();
    }
  }