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
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tuple.Fields;
import tools.ExampleTestCase;

import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 *
 */
public class ShareThisTest extends ExampleTestCase implements Serializable
  {
  private String sampleData;
  private String outputPath;


  private class NoTester
    {
    //@extract-start sharethis-unittest
    public void testLogParsing() throws IOException
      {
      Hfs source = new Hfs(new TextLine(new Fields("line")), sampleData);
      Hfs sink =
        new Hfs(new TextLine(), outputPath + "/parser", SinkMode.REPLACE);

      Pipe pipe = new Pipe("parser");

      // split "line" on tabs
      pipe = new Each(pipe, new Fields("line"), new RegexSplitter("\t"));

      pipe = new LogParser(pipe);

      pipe = new LogRules(pipe);

      // testing only assertions
      pipe = new ParserAssertions(pipe);

      Flow flow = new FlowConnector().connect(source, sink, pipe);

      flow.complete(); // run the test flow

      // verify there are 98 tuples, 2 fields, and matches the regex pattern
      // for TextLine schemes the tuples are { "offset", "line }
      validateLength(flow, 98, 2, Pattern.compile("^[0-9]+(\\t[^\\t]*){19}$"));
      }
    //@extract-end
    }

  private class LogParser extends SubAssembly
    {
    public LogParser(Pipe pipe)
      {
      }
    }

  private class LogRules extends SubAssembly
    {
    public LogRules(Pipe pipe)
      {
      }
    }

  private class LogSplitter extends SubAssembly
    {
    public LogSplitter(Pipe pipe)
      {
      }
    }

  private class ParserAssertions extends SubAssembly
    {
    public ParserAssertions(Pipe pipe)
      {
      }
    }
  }