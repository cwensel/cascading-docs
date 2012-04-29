/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.util.Collections;
import java.util.Properties;

import cascading.cascade.Cascade;
import cascading.cascade.CascadeConnector;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.Debug;
import cascading.operation.DebugLevel;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.expression.ExpressionFunction;
import cascading.operation.regex.RegexSplitter;
import cascading.operation.text.FieldJoiner;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.HashJoin;
import cascading.pipe.Merge;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;
import cascading.pipe.assembly.AggregateBy;
import cascading.pipe.assembly.AverageBy;
import cascading.pipe.assembly.Coerce;
import cascading.pipe.assembly.CountBy;
import cascading.pipe.assembly.Discard;
import cascading.pipe.assembly.Rename;
import cascading.pipe.assembly.Retain;
import cascading.pipe.assembly.SumBy;
import cascading.pipe.assembly.Unique;
import cascading.pipe.joiner.InnerJoin;
import cascading.property.AppProps;
import cascading.property.ConfigDef;
import cascading.scheme.hadoop.TextDelimited;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tap.hadoop.TemplateTap;
import cascading.tuple.Fields;
import cascading.tuple.collect.SpillableProps;

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
    Flow flow = new HadoopFlowConnector().connect( "flow-name", source, sink, pipe );
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

    Pipe groupBy = new GroupBy( join );

    groupBy = new Every( groupBy, new SomeAggregator() );

    // the tail of the assembly
    groupBy = new Each( groupBy, new SomeFunction() );

    Tap lhsSource = new Hfs( new TextLine(), "lhs.txt" );
    Tap rhsSource = new Hfs( new TextLine(), "rhs.txt" );

    Tap sink = new Hfs( new TextLine(), "output" );

    FlowDef flowDef = new FlowDef()
      .setName( "flow-name" )
      .addSource( rhs, rhsSource )
      .addSource( lhs, lhsSource )
      .addTailSink( groupBy, sink );

    Flow flow = new HadoopFlowConnector().connect( flowDef );
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
    AppProps.setApplicationJarClass( properties, Main.class );

    // ALTERNATELY ...

    // pass in the path to the parent jar
    AppProps.setApplicationJarPath( properties, pathToJar );


    // pass properties to the connector
    FlowConnector flowConnector = new HadoopFlowConnector( properties );
    //@extract-end
    }

  public void compileGroupBy()
    {
    Pipe assembly = new Pipe( "assembly" );

    //@extract-start simple-groupby
    Pipe groupBy = new GroupBy( assembly, new Fields( "group1", "group2" ) );
    //@extract-end
    }

  public void compileGroupBySecondarySort()
    {
    Pipe assembly = new Pipe( "assembly" );

    //@extract-start simple-groupby-secondary
    Fields groupFields = new Fields( "group1", "group2" );
    Fields sortFields = new Fields( "value1", "value2" );
    Pipe groupBy = new GroupBy( assembly, groupFields, sortFields );
    //@extract-end
    }

  public void compileGroupBySecondarySortTime()
    {
    Pipe assembly = new Pipe( "assembly" );

    //@extract-start simple-groupby-secondary-time
    Fields groupFields = new Fields( "year", "month", "day" );
    Fields sortFields = new Fields( "hour", "minute", "second" );

    sortFields.setComparators(
      Collections.reverseOrder(),   // hour
      Collections.reverseOrder(),   // minute
      Collections.reverseOrder() ); // second

    Pipe groupBy = new GroupBy( assembly, groupFields, sortFields );
    //@extract-end
    }

  public void compileGroupBySecondarySortReverse()
    {
    Pipe assembly = new Pipe( "assembly" );

    //@extract-start simple-groupby-secondary-comparator
    Fields groupFields = new Fields( "group1", "group2" );
    Fields sortFields = new Fields( "value1", "value2" );

    sortFields.setComparator( "value1", Collections.reverseOrder() );

    Pipe groupBy = new GroupBy( assembly, groupFields, sortFields );
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

  public void compileMerge()
    {
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    //@extract-start simple-merge
    Pipe merge = new Merge( lhs, rhs );
    //@extract-end
    }

  public void compileJoin()
    {
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    //@extract-start simple-join
    Fields lhsFields = new Fields( "fieldA", "fieldB" );
    Fields rhsFields = new Fields( "fieldC", "fieldD" );
    Pipe join = new HashJoin( lhs, lhsFields, rhs, rhsFields, new InnerJoin() );
    //@extract-end
    }

  public void compileJoinExample()
    {
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    //@extract-start duplicate-join
    Fields common = new Fields( "url" );
    Fields declared = new Fields( "url1", "word", "wd_count", "url2", "sentence", "snt_count" );
    Pipe join = new HashJoin( lhs, common, rhs, common, declared, new InnerJoin() );
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
    Pipe join = new CoGroup( lhs, lhsFields, rhs, rhsFields, new InnerJoin() );
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
    Pipe join = new CoGroup( lhs, common, rhs, common, declared, new InnerJoin() );
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

    // head and tail have same name
    FlowDef flowDef = new FlowDef()
      .setName( "debug" )
      .addSource( "assembly", source )
      .addSink( "assembly", sink )
      .addTail( assembly );


    // tell the planner to remove all Debug operations
    flowDef
      .setDebugLevel( DebugLevel.NONE );

    // ...
    FlowConnector flowConnector = new HadoopFlowConnector();

    Flow flow = flowConnector.connect( flowDef );
    //@extract-end
    }

  public void partialSumBy()
    {
    //@extract-start partials-sumby
    Pipe assembly = new Pipe( "assembly" );

    // ...
    Fields groupingFields = new Fields( "date" );
    Fields valueField = new Fields( "size" );
    Fields sumField = new Fields( "total-size" );
    assembly = new SumBy( assembly, groupingFields, valueField, sumField, long.class );
    //@extract-end
    }

  public void partialAverageBy()
    {
    //@extract-start partials-averageby
    Pipe assembly = new Pipe( "assembly" );

    // ...
    Fields groupingFields = new Fields( "date" );
    Fields valueField = new Fields( "size" );
    Fields avgField = new Fields( "avg-size" );
    assembly = new AverageBy( assembly, groupingFields, valueField, avgField );
    //@extract-end
    }

  public void partialCountBy()
    {
    //@extract-start partials-countby
    Pipe assembly = new Pipe( "assembly" );

    // ...
    Fields groupingFields = new Fields( "date" );
    Fields countField = new Fields( "count" );
    assembly = new CountBy( assembly, groupingFields, countField );
    //@extract-end
    }

  public void partialCompose()
    {
    //@extract-start partials-compose
    Pipe assembly = new Pipe( "assembly" );

    // ...
    Fields groupingFields = new Fields( "date" );

    // note we do not pass the parent assembly Pipe in
    Fields valueField = new Fields( "size" );
    Fields sumField = new Fields( "total-size" );
    SumBy sumBy = new SumBy( valueField, sumField, long.class );

    Fields countField = new Fields( "num-events" );
    CountBy countBy = new CountBy( countField );

    assembly = new AggregateBy( assembly, groupingFields, sumBy, countBy );
    //@extract-end
    }

  public void fieldAlgebra()
    {
    Pipe assembly = new Pipe( "assembly" );

    {
    //@extract-start algebra-all
    // incoming -> first, last, age

    String expression = "first + \" \" + last";
    ExpressionFunction full =
      new ExpressionFunction( new Fields( "full" ), expression, String.class );

    assembly = new Each( assembly, new Fields( "first", "last" ), full, Fields.ALL );

    // outgoing -> first, last, age, full
    //@extract-end
    }

    {
    //@extract-start algebra-results
    // incoming -> first, last, age

    String expression = "first + \" \" + last";
    ExpressionFunction full =
      new ExpressionFunction( new Fields( "full" ), expression, String.class );

    assembly = new Each( assembly, new Fields( "first", "last" ), full, Fields.RESULTS );

    // outgoing -> full
    //@extract-end
    }

    {
    //@extract-start algebra-replace
    // incoming -> first, last, age

    Identity function = new Identity( Fields.ARGS, Integer.class ); // coerce to int

    assembly = new Each( assembly, new Fields( "age" ), function, Fields.REPLACE );

    // outgoing -> first, last, age
    //@extract-end
    }

    {
    //@extract-start algebra-swap
    // incoming -> first, last, age

    String expression = "first + \" \" + last";
    ExpressionFunction full =
      new ExpressionFunction( new Fields( "full" ), expression, String.class );

    assembly = new Each( assembly, new Fields( "first", "last" ), full, Fields.SWAP );

    // outgoing -> age, full
    //@extract-end
    }

    {
    //@extract-start algebra-unknown
    // incoming -> line

    RegexSplitter function = new RegexSplitter( Fields.UNKNOWN, "\t" );

    assembly = new Each( assembly, new Fields( "line" ), function, Fields.RESULTS );

    // outgoing -> unknown
    //@extract-end
    }

    {
    //@extract-start algebra-none
    // incoming -> first, last, age

    Insert constant = new Insert( new Fields( "zip" ), "77373" );

    assembly = new Each( assembly, Fields.NONE, constant, Fields.ALL );

    // outgoing -> first, last, age, zip
    //@extract-end
    }

    {
    //@extract-start algebra-group
    // incoming -> first, last, age

    assembly = new GroupBy( assembly, new Fields( "first", "last" ) );

    FieldJoiner full = new FieldJoiner( new Fields( "full" ), " " );

    assembly = new Each( assembly, Fields.GROUP, full, Fields.ALL );

    // outgoing -> first, last, age, full
    //@extract-end
    }

    {
    //@extract-start algebra-values
    // incoming -> first, last, age

    assembly = new GroupBy( assembly, new Fields( "age" ) );

    FieldJoiner full = new FieldJoiner( new Fields( "full" ), " " );

    assembly = new Each( assembly, Fields.VALUES, full, Fields.ALL );

    // outgoing -> first, last, age, full
    //@extract-end
    }
    }

  public void compileProperties()
    {
    // the "left hand side" assembly head
    Pipe lhs = new Pipe( "lhs" );

    // the "right hand side" assembly head
    Pipe rhs = new Pipe( "rhs" );

    Fields common = new Fields( "url" );
    Fields declared = new Fields( "url1", "word", "wd_count", "url2", "sentence", "snt_count" );

    {
    //@extract-start properties-pipe
    Pipe join = new HashJoin( lhs, common, rhs, common, declared, new InnerJoin() );

    SpillableProps props = SpillableProps.spillableProps()
      .setCompressSpill( true )
      .setMapSpillThreshold( 50 * 1000 );

    props.setProperties( join.getConfigDef(), ConfigDef.Mode.REPLACE );
    //@extract-end
    }

    {
    //@extract-start properties-step
    Pipe join = new HashJoin( lhs, common, rhs, common, declared, new InnerJoin() );

    SpillableProps props = SpillableProps.spillableProps()
      .setCompressSpill( true )
      .setMapSpillThreshold( 50 * 1000 );

    props.setProperties( join.getStepConfigDef(), ConfigDef.Mode.DEFAULT );
    //@extract-end
    }
    }

  public void fieldSubAssemblies()
    {
    Pipe assembly = new Pipe( "assembly" );

    {
    //@extract-start subassembly-coerce
    // incoming -> first, last, age

    assembly = new Coerce( assembly, new Fields( "age" ), Integer.class );

    // outgoing -> first, last, age
    //@extract-end
    }

    {
    //@extract-start subassembly-discard
    // incoming -> first, last, age

    assembly = new Discard( assembly, new Fields( "age" ) );

    // outgoing -> first, last
    //@extract-end
    }

    {
    //@extract-start subassembly-rename
    // incoming -> first, last, age

    assembly = new Rename( assembly, new Fields( "age" ), new Fields( "years" ) );

    // outgoing -> first, last, years
    //@extract-end
    }

    {
    //@extract-start subassembly-retain
    // incoming -> first, last, age

    assembly = new Retain( assembly, new Fields( "first", "last" ) );

    // outgoing -> first, last
    //@extract-end
    }

    {
    //@extract-start subassembly-unique
    // incoming -> first, last

    assembly = new Unique( assembly, new Fields( "first", "last" ) );

    // outgoing -> first, last
    //@extract-end
    }
    }

  }