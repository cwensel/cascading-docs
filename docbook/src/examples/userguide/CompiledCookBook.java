/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.FlowProcess;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.Identity;
import cascading.operation.Insert;
import cascading.operation.aggregator.First;
import cascading.operation.text.DateFormatter;
import cascading.operation.text.DateParser;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Coerce;
import cascading.pipe.assembly.Discard;
import cascading.pipe.assembly.Rename;
import cascading.pipe.assembly.Retain;
import cascading.pipe.assembly.Unique;
import cascading.scheme.Scheme;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;

/**
 *
 */
public class CompiledCookBook
  {
  public void compileCopyTuple()
    {
    //@extract-start cookbook-copy
    Tuple original = new Tuple( "john", "doe" );

    // call copy constructor
    Tuple copy = new Tuple( original );

    assert copy.getObject( 0 ).equals( "john" );
    assert copy.getObject( 1 ).equals( "doe" );
    //@extract-end
    }

  public void compileNestTuple()
    {
    //@extract-start cookbook-nest
    Tuple parent = new Tuple();
    parent.add( new Tuple( "john", "doe" ) );

    assert ( (Tuple) parent.getObject( 0 ) ).getObject( 0 ).equals( "john" );
    assert ( (Tuple) parent.getObject( 0 ) ).getObject( 1 ).equals( "doe" );
    //@extract-end
    }

  public void compileFieldsAppend()
    {
    //@extract-start cookbook-fieldsappend
    Fields first = new Fields( "first" );
    Fields middle = new Fields( "middle" );
    Fields last = new Fields( "last" );

    Fields full = first.append( middle ).append( last );
    //@extract-end
    }

  public void compileFieldsSubtract()
    {
    //@extract-start cookbook-fieldssubtract
    Fields full = new Fields( "first", "middle", "last" );

    Fields firstLast = full.subtract( new Fields( "middle" ) );
    //@extract-end
    }

  public void compileSplitStream()
    {
    //@extract-start cookbook-split
    Pipe pipe = new Pipe( "head" );
    pipe = new Each( pipe, new SomeFunction() );
    // ...

    // split left with the branch name 'lhs'
    Pipe lhs = new Pipe( "lhs", pipe );
    lhs = new Each( lhs, new SomeFunction() );
    // ...

    // split right with the branch name 'rhs'
    Pipe rhs = new Pipe( "rhs", pipe );
    rhs = new Each( rhs, new SomeFunction() );
    // ...
    //@extract-end
    }

  public void compileCopyField()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-copyfield
    Fields argument = new Fields( "field" );
    Identity identity = new Identity( new Fields( "copy" ) );

    // identity copies the incoming argument to the result tuple
    pipe = new Each( pipe, argument, identity, Fields.ALL );
    //@extract-end
    }

  public void compileDiscardField()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-discardfield
    // incoming -> "keepField", "dropField"
    pipe = new Discard( pipe, new Fields( "dropField" ) );
    // outgoing -> "keepField"
    //@extract-end
    }

  public void compileRetainField()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-retainfield
    // incoming -> "keepField", "dropField"
    pipe = new Retain( pipe, new Fields( "keepField" ) );
    // outgoing -> "keepField"
    //@extract-end
    }

  public void compileRenameField()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-renamefield
    // a simple SubAssembly that uses Identity internally
    pipe = new Rename( pipe, new Fields( "from" ), new Fields( "to" ) );
    //@extract-end
    }

  public void compileCoerceFields()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-coercefields
    Fields fields = new Fields( "longField", "booleanField" );
    Class types[] = new Class[]{long.class, boolean.class};

    // convert to given type
    pipe = new Coerce( pipe, fields, types );
    //@extract-end
    }

  public void compileInsertValue()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-insertvalue
    Fields fields = new Fields( "constant1", "constant2" );
    Insert function = new Insert( fields, "value1", "value2" );

    pipe = new Each( pipe, function, Fields.ALL );
    //@extract-end
    }

  public void compileParseDate()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-parsedate
    // convert string date/time field to a long
    // milliseconds "timestamp" value
    String format = "yyyy:MM:dd:HH:mm:ss.SSS";
    DateParser parser = new DateParser( new Fields( "ts" ), format );

    pipe = new Each( pipe, new Fields( "datetime" ), parser, Fields.ALL );
    //@extract-end
    }

  public void compileFormatDate()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-formatdate
    // convert a long milliseconds "timestamp" value to a string
    String format = "HH:mm:ss.SSS";
    DateFormatter formatter =
      new DateFormatter( new Fields( "datetime" ), format );

    pipe = new Each( pipe, new Fields( "ts" ), formatter, Fields.ALL );
    //@extract-end
    }

  public void compileDistinctGroups()
    {
    Pipe pipe = new Pipe( "head" );

    // remove all duplicate tuples in the stream

    //@extract-start cookbook-distinctgroup
    // remove all duplicates from the stream
    pipe = new Unique( pipe, Fields.ALL );
    //@extract-end
    }

  public void compileDistinctValues()
    {
    Pipe pipe = new Pipe( "head" );

    // to create a list of unique ip addresses

    //@extract-start cookbook-distinctvalue
    // narrow stream to just ips
    pipe = new Retain( pipe, new Fields( "ip" ) );
    // find all unique 'ip' values
    pipe = new Unique( pipe, new Fields( "ip" ) );
    //@extract-end
    }

  public void compileDistinctOrderedValues()
    {
    Pipe pipe = new Pipe( "head" );

    // filter logs returning the first occurrence of each unique ip address

    //@extract-start cookbook-distinctorder
    // group on all unique 'ip' values
    // secondary sort on 'datetime', natural order is in ascending order
    pipe = new GroupBy( pipe, new Fields( "ip" ), new Fields( "datetime" ) );
    // take the first 'ip' tuple in the group which has the
    // oldest 'datetime' value
    pipe = new Every( pipe, Fields.ALL, new First(), Fields.RESULTS );
    //@extract-end
    }

  public void compilePassProperties()
    {
    FlowProcess flowProcess = null;

    //@extract-start cookbook-passproperties
    // set property on Flow
    Properties properties = new Properties();
    properties.put( "key", "value" );
    FlowConnector flowConnector = new HadoopFlowConnector( properties );
    // ...

    // get the property from within an Operation (Function, Filter, etc)
    String value = (String) flowProcess.getProperty( "key" );
    //@extract-end
    }

  public void compileSourcesSinks()
    {
    //@extract-start cookbook-sourcessinks
    Pipe headLeft = new Pipe( "headLeft" );
    // do something interesting

    Pipe headRight = new Pipe( "headRight" );
    // do something interesting

    // merge the two input streams
    Pipe merged = new GroupBy( headLeft, headRight, new Fields( "common" ) );
    // ...

    // branch the merged stream
    Pipe tailLeft = new Pipe( "tailLeft", merged );
    // filter out values to the left
    tailLeft = new Each( tailLeft, new SomeFilter() );

    Pipe tailRight = new Pipe( "tailRight", merged );
    // filter out values to the right
    tailRight = new Each( tailRight, new SomeFilter() );

    // source taps
    Scheme inLeftScheme =
      new TextDelimited( new Fields( "some-fields" ) );
    Tap sourceLeft = new Hfs( inLeftScheme, "some/path" );

    Scheme inRightScheme =
      new TextDelimited( new Fields( "some-fields" ) );
    Tap sourceRight = new Hfs( inRightScheme, "some/path" );

    // sink taps
    Scheme outLeftScheme =
      new TextDelimited( new Fields( "some-fields" ) );
    Tap sinkLeft = new Hfs( outLeftScheme, "some/path" );

    Scheme outRightScheme =
      new TextDelimited( new Fields( "some-fields" ) );
    Tap sinkRight = new Hfs( outRightScheme, "some/path" );

    FlowDef flowDef = new FlowDef()
      .setName( "flow-name" );

    // bind source Taps to Pipe heads
    flowDef
      .addSource( headLeft, sourceLeft )
      .addSource( headRight, sourceRight );

    // bind sink Taps to Pipe tails
    flowDef
      .addSink( tailLeft, sinkLeft )
      .addTailSink( tailRight, sinkRight );

    // ALTERNATIVELY ...

    // add named source Taps
    // the head pipe name to bind to
    flowDef
      .addSource( "headLeft", sourceLeft )    // headLeft.getName()
      .addSource( "headRight", sourceRight ); // headRight.getName()

    // add named sink Taps
    flowDef
      .addSink( "tailLeft", sinkLeft )    // tailLeft.getName()
      .addSink( "tailRight", sinkRight ); // tailRight.getName()

    // add tails -- heads are reachable from the tails
    flowDef
      .addTail( tailLeft )
      .addTail( tailRight );

    // set property on Flow
    FlowConnector flowConnector = new HadoopFlowConnector();

    Flow flow = flowConnector.connect( flowDef );
    //@extract-end
    }
  }