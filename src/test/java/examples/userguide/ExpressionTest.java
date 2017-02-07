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
import cascading.flow.local.LocalFlowConnector;
import cascading.operation.expression.ExpressionFilter;
import cascading.operation.expression.ExpressionFunction;
import cascading.operation.regex.RegexParser;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.scheme.local.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.local.FileTap;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntryIterator;
import org.junit.Test;
import tools.ExampleTestCase;

/**
 *
 */
public class ExpressionTest extends ExampleTestCase
  {
  @Test
  public void testExpressionFilter() throws IOException
    {
    String inputPath = getDataPath() + "apache.10.txt";
    String outputPath = getOutputPath() + "expressionfilter";

    Tap source = new FileTap( new TextLine(), inputPath );
    Tap sink = new FileTap( new TextLine(), outputPath, SinkMode.REPLACE );

    Pipe assembly = new Pipe( "logs" );

    String regex = "^([^ ]*) +[^ ]* +[^ ]* +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) [^ ]*\\\" ([^ ]*) ([^ ]*).*$";
    Fields fieldDeclaration = new Fields( "ip", "time", "method", "event", "status", "size" );
    int[] groups = {1, 2, 3, 4, 5, 6};
    RegexParser parser = new RegexParser( fieldDeclaration, regex, groups );
    assembly = new Each( assembly, new Fields( "line" ), parser );

    //@extract-start expression-filter
    // incoming -> "ip", "time", "method", "event", "status", "size"

    ExpressionFilter filter =
      new ExpressionFilter( "status != 200", Integer.TYPE );

    assembly = new Each( assembly, new Fields( "status" ), filter );

    // outgoing -> "ip", "time", "method", "event", "status", "size"
    //@extract-end

    //@extract-start expression-function
    // incoming -> "ip", "time", "method", "event", "status", "size"

    String exp =
      "\"this \" + method + \" request was \" + size + \" bytes\"";
    Fields fields = new Fields( "pretty" );
    ExpressionFunction function =
      new ExpressionFunction( fields, exp, String.class ); // <1>

    assembly =
      new Each( assembly, new Fields( "method", "size" ), function );

    // outgoing -> "pretty" = "this GET request was 1282652 bytes"
    //@extract-end

    Flow flow = new LocalFlowConnector().connect( source, sink, assembly );

    flow.complete();

    validateLength( flow, 3 );

    TupleEntryIterator iterator = flow.openSink();

    assertEquals( "this GET request was 0 bytes", iterator.next().getObject( 1 ) );

    iterator.close();
    }
  }