/*
 * Copyright (c) 2007-2010 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Debug;
import cascading.operation.DebugLevel;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;
import cascading.pipe.cogroup.InnerJoin;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.TemplateTap;
import cascading.tuple.Fields;

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

  public void compilePipeSubAssembly()
    {
    //@extract-start simple-subassembly
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    // our custom SubAssembly
    Pipe pipe = new SomeSubAssembly( lhs, rhs );

    pipe = new Each( pipe, new SomeFunction() );
    //@extract-end
    }

  public void compileSplitSubAssembly()
    {
    //@extract-start simple-split-subassembly
    // the "left hand side" assembly head
    Pipe head = new Pipe( "head" );

    // our custom SubAssembly
    SubAssembly pipe = new SplitSubAssembly( head );

    // grab the split branches
    Pipe lhs = new Each( pipe.getTails()[ 0 ], new SomeFunction() );
    Pipe rhs = new Each( pipe.getTails()[ 1 ], new SomeFunction() );
    //@extract-end
    }

  public void compileTap()
    {
    String path = "some/path";

    //@extract-start simple-tap
    Tap tap = new Hfs( new TextLine( new Fields( "line" ) ), path );
    //@extract-end
    }

  public void compileTapReplace()
    {
    String path = "some/path";

    //@extract-start simple-replace-tap
    Tap tap = new Hfs( new TextLine( new Fields( "line" ) ), path, SinkMode.REPLACE );
    //@extract-end
    }

  public void compileTemplateTap()
    {
    String path = "some/path";

    //@extract-start template-tap
    TextDelimited scheme = new TextDelimited( new Fields( "year", "month", "entry" ), "\t" );
    Hfs tap = new Hfs( scheme, path );

    String template = "%s-%s"; // dirs named "year-month"
    Tap months = new TemplateTap( tap, template, SinkMode.REPLACE );
    //@extract-end
    }

  public void compileFlow()
    {
    Tap source = null;
    Tap sink = null;
    Pipe pipe = null;

    //@extract-start simple-flow
    Flow flow = new FlowConnector().connect( "flow-name", source, sink, pipe );
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

    Tap lhsSource = new Hfs( new TextLine(), "lhs.txt" );
    Tap rhsSource = new Hfs( new TextLine(), "rhs.txt" );

    Tap sink = new Hfs( new TextLine(), "output" );

    Map<String, Tap> sources = new HashMap<String, Tap>();

    sources.put( "lhs", lhsSource );
    sources.put( "rhs", rhsSource );

    Flow flow = new FlowConnector().connect( "flow-name", sources, sink, join );
    //@extract-end
    }

  public void compileCascade()
    {
    Flow flowFirst = null;
    Flow flowSecond = null;
    Flow flowThird = null;

    //@extract-start simple-cascade
    CascadeConnector connector = new CascadeConnector();
    Cascade cascade = connector.connect( flowFirst, flowSecond, flowThird );
    //@extract-end
    }

  public static class Main
    {

    }

  public void compileFlowConnector()
    {
    String pathToJar = null;

    //@extract-start flow-properties
    Properties properties = new Properties();

    // pass in the class name of your application
    // this will find the parent jar at runtime
    FlowConnector.setApplicationJarClass( properties, Main.class );

    // or pass in the path to the parent jar
    FlowConnector.setApplicationJarPath( properties, pathToJar );

    FlowConnector flowConnector = new FlowConnector( properties );
    //@extract-end
    }

  public void compileGroupBy()
    {
    // the "left hand side" assembly head
    Pipe assembly = new Pipe( "assembly" );

    //@extract-start simple-groupby
    Pipe merge = new GroupBy( assembly, new Fields( "group1", "group2" ) );
    //@extract-end
    }


  public void compileGroupByMerge()
    {
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    //@extract-start simple-groupby-merge
    Pipe[] pipes = Pipe.pipes( lhs, rhs );
    Pipe merge = new GroupBy( pipes, new Fields( "group1", "group2" ) );
    //@extract-end
    }

  public void compileCoGroup()
    {
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    //@extract-start simple-cogroup
    Fields lhsFields = new Fields( "fieldA", "fieldB" );
    Fields rhsFields = new Fields( "fieldC", "fieldD" );
    Pipe merge = new CoGroup( lhs, lhsFields, rhs, rhsFields, new InnerJoin() );
    //@extract-end
    }

  public void compileCoGroupExample()
    {
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    //@extract-start duplicate-cogroup
    Fields common = new Fields( "url" );
    Fields declared = new Fields( "url1", "word", "wd_count", "url2", "sentence", "snt_count" );
    Pipe merge = new CoGroup( lhs, common, rhs, common, declared, new InnerJoin() );
    //@extract-end
    }

  public void compileDebug()
    {
    Tap source = null;
    Tap sink = null;

    //@extract-start flow-debug
    Pipe assembly = new Pipe( "assembly" );

    // ...
    assembly = new Each( assembly, DebugLevel.VERBOSE, new Debug() );
    // ...

    Properties properties = new Properties();

    // tell the planner remove all Debug operations
    FlowConnector.setDebugLevel( properties, DebugLevel.NONE );
    // ...
    FlowConnector flowConnector = new FlowConnector( properties );

    Flow flow = flowConnector.connect( "debug", source, sink, assembly );
    //@extract-end
    }


  }