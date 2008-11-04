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
import java.util.Map;
import java.util.HashMap;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Aggregator;
import cascading.operation.Function;
import cascading.operation.aggregator.Count;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.CoGroup;
import cascading.scheme.Scheme;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.cascade.CascadeConnector;
import cascading.cascade.Cascade;
import tools.ExampleTestCase;

/**
 *
 */
public class CompiledExamples
  {
  public void compilePipeAssembly()
    {
    //@extract-start simple-pipe-assembly
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    lhs = new Each( lhs, new SomeFunction() );
    lhs = new Each( lhs, new SomeFilter() );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    rhs = new Each( rhs, new SomeFunction() );

    // joins the lhs and rhs
    Pipe join = new CoGroup( lhs, rhs );

    join = new Every( join, new SomeAggregator() );

    join = new GroupBy( join );

    join = new Every( join, new SomeAggregator() );

    // the tail of the assembly
    join = new Each( join, new SomeFunction() );
    //@extract-end
    }

  public void compileTap()
    {
    String path = "some/path";

    //@extract-start simple-tap
    Tap tap = new Hfs( new TextLine( new Fields( "line" ) ), path );
    //@extract-end
    }


  public void compileFlow()
    {
    Tap source = null;
    Tap sink = null;
    Pipe pipe = null;

    //@extract-start simple-flow
    Flow flow = new FlowConnector().connect( source, sink, pipe );
    //@extract-end
    }

  public void compileComplexFlow()
    {
    //@extract-start complex-flow
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    lhs = new Each( lhs, new SomeFunction() );
    lhs = new Each( lhs, new SomeFilter() );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    rhs = new Each( rhs, new SomeFunction() );

    // joins the lhs and rhs
    Pipe join = new CoGroup( lhs, rhs );

    join = new Every( join, new SomeAggregator() );

    join = new GroupBy( join );

    join = new Every( join, new SomeAggregator() );

    // the tail of the assembly
    join = new Each( join, new SomeFunction() );

    Tap lhsSource = new Hfs(new TextLine(), "lhs.txt");
    Tap rhsSource = new Hfs(new TextLine(), "rhs.txt");

    Tap sink = new Hfs(new TextLine(), "output");

    Map<String, Tap> sources = new HashMap<String,Tap>();

    sources.put( "lhs", lhsSource );
    sources.put( "rhs", rhsSource );

    Flow flow = new FlowConnector().connect( sources, sink, join );
    //@extract-end
    }

  public void compileCascade()
    {
    Flow flowFirst = null;
    Flow flowSecond = null;
    Flow flowThird = null;

    //@extract-start simple-cascade
    Cascade cascade = new CascadeConnector().connect( flowFirst, flowSecond, flowThird );
    //@extract-end
    }

  }