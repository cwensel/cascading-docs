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

package casestudy;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Aggregator;
import cascading.operation.Function;
import cascading.operation.aggregator.Count;
import cascading.operation.expression.ExpressionFunction;
import cascading.operation.regex.RegexGenerator;
import cascading.pipe.*;
import cascading.scheme.Scheme;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import tools.ExampleTestCase;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 */
public class WordCountTest extends ExampleTestCase implements Serializable
  {
  public void testBasicWordCount() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "wordcount";

    //@extract-start word-count-sort
    Scheme sourceScheme = new TextLine(new Fields("line")); //<co xml:id="ex.wcs.source.scheme"/>
    Tap source = new Hfs(sourceScheme, inputPath); //<co xml:id="ex.wcs.source"/>

    Scheme sinkScheme = new TextLine(); //<co xml:id="ex.wcs.sink.scheme"/>
    Tap sink = new Hfs(sinkScheme, outputPath, SinkMode.REPLACE); //<co xml:id="ex.wcs.sink"/>

    Pipe assembly = new Pipe("wordcount"); //<co xml:id="ex.wcs.pipe"/>

    String regexString = "(?<!\\pL)(?=\\pL)[^ ]*(?<=\\pL)(?!\\pL)";
    Function regex = new RegexGenerator(new Fields("word"), regexString);
    assembly = new Each(assembly, new Fields("line"), regex); //<co xml:id="ex.wcs.each"/>

    assembly = new GroupBy(assembly, new Fields("word")); //<co xml:id="ex.wcs.group.word"/>

    Aggregator count = new Count(new Fields("count"));
    assembly = new Every(assembly, count); //<co xml:id="ex.wcs.every"/>

    assembly = new GroupBy(assembly, new Fields("count"), new Fields("word")); //<co xml:id="ex.wcs.group.count"/>

    FlowConnector flowConnector = new FlowConnector();
    Flow flow = flowConnector.connect("word-count", source, sink, assembly); //<co xml:id="ex.wcs.connect"/>

    flow.complete(); //<co xml:id="ex.wcs.run"/>
    //@extract-end

    validateLength(flow, 197);
    }

  //@extract-start word-count-sort-subassembly
  public class ParseWordsAssembly extends SubAssembly //<co xml:id="ex.wcs.subclass"/>
    {
    public ParseWordsAssembly(Pipe previous)
      {
      String regexString = "(?<!\\pL)(?=\\pL)[^ ]*(?<=\\pL)(?!\\pL)";
      Function regex = new RegexGenerator(new Fields("word"), regexString);
      previous = new Each(previous, new Fields("line"), regex);

      String expressionString = "word.toLowerCase()";
      Function expression = new ExpressionFunction(new Fields("word"), expressionString, String.class); //<co xml:id="ex.wcs.expression"/>
      previous = new Each(previous, new Fields("word"), expression);

      setTails(previous); //<co xml:id="ex.wcs.tails"/>
      }
    }
  //@extract-end

  public void testBasicWordCountSub() throws IOException
    {
    String inputPath = getDataPath() + "lipsum.txt";
    String outputPath = getOutputPath() + "wordcountsub";

    //@extract-start word-count-sort-sub
    Scheme sourceScheme = new TextLine(new Fields("line"));
    Tap source = new Hfs(sourceScheme, inputPath);

    Scheme sinkScheme = new TextLine(new Fields("word", "count"));
    Tap sink = new Hfs(sinkScheme, outputPath, SinkMode.REPLACE);

    Pipe assembly = new Pipe("wordcount");

    assembly = new ParseWordsAssembly(assembly); //<co xml:id="ex.wcs.subassembly"/>

    assembly = new GroupBy(assembly, new Fields("word"));

    Aggregator count = new Count(new Fields("count"));
    assembly = new Every(assembly, count);

    assembly = new GroupBy(assembly, new Fields("count"), new Fields("word"));

    FlowConnector flowConnector = new FlowConnector();
    Flow flow = flowConnector.connect("word-count", source, sink, assembly);

    flow.complete();
    //@extract-end

    validateLength(flow, 186);
    }

  }