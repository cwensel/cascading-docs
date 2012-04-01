/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cascading.cascade.Cascades;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
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
import cascading.pipe.assembly.Rename;
import cascading.pipe.assembly.Unique;
import cascading.scheme.hadoop.SequenceFile;
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

    assert copy.get( 0 ).equals( "john" );
    //@extract-end
    }

  public void compileNestTuple()
    {
    //@extract-start cookbook-nest
    Tuple parent = new Tuple();
    parent.add( new Tuple( "john", "doe" ) );

    assert ( (Tuple) parent.get( 0 ) ).get( 0 ).equals( "john" );
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
    pipe = new Each( pipe, new Fields( "keepField" ), new Identity(),
      Fields.RESULTS );
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
    Fields arguments = new Fields( "longField", "booleanField" );
    Class types[] = new Class[]{long.class, boolean.class};
    Identity identity = new Identity( types );

    // convert from string to given type, inline replace values
    pipe = new Each( pipe, arguments, identity, Fields.REPLACE );
    //@extract-end
    }

  public void compileInsertValue()
    {
    Pipe pipe = new Pipe( "head" );

    //@extract-start cookbook-insertvalue
    Fields fields = new Fields( "constant1", "constant2" );
    pipe = new Each( pipe, new Insert( fields, "value1", "value2" ),
      Fields.ALL );
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
    DateFormatter formatter = new DateFormatter( new Fields( "datetime" ),
      format );
    pipe = new Each( pipe, new Fields( "ts" ), formatter, Fields.ALL );
    //@extract-end
    }

  public void compileDistinctGroups()
    {
    Pipe pipe = new Pipe( "head" );

    // remove all duplicate tuples in the stream

    //@extract-start cookbook-distinctgroup
    // group on all values
    pipe = new GroupBy( pipe, Fields.ALL );
    // only take the first tuple in the grouping, ignore the rest
    pipe = new Every( pipe, Fields.ALL, new First(), Fields.RESULTS );
    //@extract-end
    }

  public void compileDistinctValues()
    {
    Pipe pipe = new Pipe( "head" );

    // to create a list of unique ip addresses

    //@extract-start cookbook-distinctvalue
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
    Tap sourceLeft = new Hfs( new SequenceFile( new Fields( "some-fields" ) ), "some/path" );
    Tap sourceRight = new Hfs( new SequenceFile( new Fields( "some-fields" ) ), "some/path" );

    Pipe[] pipesArray = Pipe.pipes( headLeft, headRight );
    Tap[] tapsArray = Tap.taps( sourceLeft, sourceRight );

    // a convenience function for creating branch names to tap maps
    Map<String, Tap> sources = Cascades.tapsMap( pipesArray, tapsArray );

    // sink taps
    Tap sinkLeft = new Hfs( new SequenceFile( new Fields( "some-fields" ) ), "some/path" );
    Tap sinkRight = new Hfs( new SequenceFile( new Fields( "some-fields" ) ), "some/path" );

    pipesArray = Pipe.pipes( tailLeft, tailRight );
    tapsArray = Tap.taps( sinkLeft, sinkRight );

    // or create the Map manually
    Map<String, Tap> sinks = new HashMap<String, Tap>();
    sinks.put( tailLeft.getName(), sinkLeft );
    sinks.put( tailRight.getName(), sinkRight );

    // set property on Flow
    FlowConnector flowConnector = new HadoopFlowConnector();

    Flow flow = flowConnector.connect( "flow-name", sources, sinks, tailLeft, tailRight );
    //@extract-end
    }


  }