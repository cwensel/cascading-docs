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

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Aggregator;
import cascading.operation.Function;
import cascading.operation.aggregator.Count;
import cascading.operation.regex.RegexGenerator;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.scheme.Scheme;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import tools.ExampleTestCase;

/**
 *
 */
public class WordCountTest extends ExampleTestCase
  {
  public void testBasicWordCount() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "wordcount";

    //@extract-start basic-word-count
    // define source and sink Taps.
    Scheme sourceScheme = new TextLine( new Fields( "line" ) );
    Tap source = new Hfs( sourceScheme, inputPath );

    Scheme sinkScheme = new TextLine( new Fields( "word", "count" ) );
    Tap sink = new Hfs( sinkScheme, outputPath, true );

    // the 'head' of the pipe assembly
    Pipe assembly = new Pipe( "wordcount" );

    // For each input Tuple
    // using a regular expression
    // parse out each word into a new Tuple with the field name "word"
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

    // plan a new Flow from the assembly using the source and sink Taps
    FlowConnector flowConnector = new FlowConnector();
    Flow flow = flowConnector.connect( "word-count", source, sink, assembly );

    // execute the flow, block until complete
    flow.complete();
    //@extract-end

    validateLength( flow, 197 );
    }

  }
