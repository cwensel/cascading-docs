/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package examples.userguide;

import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.hadoop2.Hadoop2MR1FlowConnector;
import cascading.pipe.Checkpoint;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tap.hadoop.PartitionTap;
import cascading.tap.partition.DelimitedPartition;
import cascading.tuple.Fields;
import org.apache.hadoop.mapred.JobConf;

/**
 *
 */
public class Hadoop2MR1CompiledExamples
  {
  public void compileTap()
    {
    String path = "some/path";

    //@extract-start h2mr1-simple-tap
    Tap tap = new Hfs( new TextLine( new Fields( "line" ) ), path );
    //@extract-end
    }

  public void compileTapReplace()
    {
    String path = "some/path";

    //@extract-start h2mr1-simple-replace-tap
    Tap tap =
      new Hfs( new TextLine( new Fields( "line" ) ), path, SinkMode.REPLACE );
    //@extract-end
    }

  public void compilePartitionTap()
    {
    String path = "some/path";

    //@extract-start h2mr1-partition-tap
    TextDelimited scheme =
      new TextDelimited( new Fields( "entry" ), "\t" );
    Hfs parentTap = new Hfs( scheme, path );

    // dirs named "[year]-[month]"
    DelimitedPartition partition = new DelimitedPartition( new Fields( "year", "month" ), "-" );
    Tap monthsTap = new PartitionTap( parentTap, partition, SinkMode.REPLACE );
    //@extract-end
    }

  public void compileFlow()
    {
    Tap source = null;
    Tap sink = null;
    Pipe pipe = null;

    //@extract-start h2mr1-simple-flow
    FlowConnector flowConnector = new Hadoop2MR1FlowConnector();

    Flow flow =
      flowConnector.connect( "flow-name", source, sink, pipe );
    //@extract-end
    }

  public void compileCheckpointFlow()
    {
    //@extract-start h2mr1-checkpoint-flow
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

    // we want to see the data passing through this point
    Checkpoint checkpoint = new Checkpoint( "checkpoint", join );

    Pipe groupBy = new GroupBy( checkpoint );

    groupBy = new Every( groupBy, new SomeAggregator() );

    // the tail of the assembly
    groupBy = new Each( groupBy, new SomeFunction() );

    Tap lhsSource = new Hfs( new TextLine(), "lhs.txt" );
    Tap rhsSource = new Hfs( new TextLine(), "rhs.txt" );

    Tap sink = new Hfs( new TextLine(), "output" );

    // write all data as a tab delimited file, with headers
    Tap checkpointTap =
      new Hfs( new TextDelimited( true, "\t" ), "checkpoint" );

    FlowDef flowDef = new FlowDef()
      .setName( "flow-name" )
      .addSource( rhs, rhsSource )
      .addSource( lhs, lhsSource )
      .addTailSink( groupBy, sink )
      .addCheckpoint( checkpoint, checkpointTap ); // bind the checkpoint tap

    Flow flow = new Hadoop2MR1FlowConnector().connect( flowDef );
    //@extract-end
    }

  public void compileCheckpointFlowRestart()
    {
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

    // we want to see the data passing through this point
    Checkpoint checkpoint = new Checkpoint( "checkpoint", join );

    Pipe groupBy = new GroupBy( checkpoint );

    groupBy = new Every( groupBy, new SomeAggregator() );

    // the tail of the assembly
    groupBy = new Each( groupBy, new SomeFunction() );

    Tap lhsSource = new Hfs( new TextLine(), "lhs.txt" );
    Tap rhsSource = new Hfs( new TextLine(), "rhs.txt" );

    Tap sink = new Hfs( new TextLine(), "output" );

    // write all data as a tab delimited file, with headers
    Tap checkpointTap =
      new Hfs( new TextDelimited( true, "\t" ), "checkpoint" );

    //@extract-start h2mr1-checkpoint-restart-flow
    FlowDef flowDef = new FlowDef()
      .setName( "flow-name" )
      .addSource( rhs, rhsSource )
      .addSource( lhs, lhsSource )
      .addTailSink( groupBy, sink )
      .addCheckpoint( checkpoint, checkpointTap )
      .setRunID( "some-unique-value" ); // re-use this id to restart this flow

    Flow flow = new Hadoop2MR1FlowConnector().connect( flowDef );
    //@extract-end
    }

  public static class Main
    {

    }

  public void compileFlowConnectorAppProps()
    {
    String pathToJar = null;

    //@extract-start flow-jobconf
    JobConf jobConf = new JobConf();

    // pass in the class name of your application
    // this will find the parent jar at runtime
    jobConf.setJarByClass( Main.class );

    // ALTERNATIVELY ...

    // pass in the path to the parent jar
    jobConf.setJar( pathToJar );

    // build the properties object using jobConf as defaults
    Properties properties = AppProps.appProps()
      .setName( "sample-app" )
      .setVersion( "1.2.3" )
      .addTags( "deploy:prod", "team:engineering" )
      .buildProperties( jobConf );

    // pass properties to the connector
    FlowConnector flowConnector = new Hadoop2MR1FlowConnector( properties );
    //@extract-end
    }

  }