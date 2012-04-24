/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;


import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

//@extract-start efficient-sum-function
public class EfficientAddValuesFunction extends BaseOperation<Tuple> implements Function<Tuple>
  {
  public EfficientAddValuesFunction()
    {
    // expects 2 arguments, fail otherwise
    super( 2, new Fields( "sum" ) );
    }

  public EfficientAddValuesFunction( Fields fieldDeclaration )
    {
    // expects 2 arguments, fail otherwise
    super( 2, fieldDeclaration );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Tuple> functionCall )
    {
    // create a reusable Tuple of size 1
    functionCall.setContext( Tuple.size( 1 ) );
    }

  public void operate( FlowProcess flowProcess, FunctionCall<Tuple> functionCall )
    {
    // get the arguments TupleEntry
    TupleEntry arguments = functionCall.getArguments();

    // get our previously created Tuple
    Tuple result = functionCall.getContext();

    // sum the two arguments
    int sum = arguments.getInteger( 0 ) + arguments.getInteger( 1 );

    // set the sum value on the result Tuple
    result.set( 0, sum );

    // return the result Tuple
    functionCall.getOutputCollector().add( result );
    }

  @Override
  public void cleanup( FlowProcess flowProcess, OperationCall<Tuple> functionCall )
    {
    functionCall.setContext( null );
    }
  }
//@extract-end
