/*
 *  Copyright (c) 2007-2017 Xplenty, Inc. All Rights Reserved.
 *
 *  Project and contact information: http://www.cascading.org/
 *
 *  This file is part of the Cascading project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package examples.userguide;

import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

//@extract-start sum-aggregator
public class AddTuplesAggregator
  extends BaseOperation<AddTuplesAggregator.Context>
  implements Aggregator<AddTuplesAggregator.Context>
  {
  public static class Context
    {
    long value = 0;
    }

  public AddTuplesAggregator()
    {
    // expects 1 argument, fail otherwise
    super( 1, new Fields( "sum" ) );
    }

  public AddTuplesAggregator( Fields fieldDeclaration )
    {
    // expects 1 argument, fail otherwise
    super( 1, fieldDeclaration );
    }

  public void start( FlowProcess flowProcess,
                     AggregatorCall<Context> aggregatorCall )
    {
    // set the context object, starting at zero
    aggregatorCall.setContext( new Context() );
    }

  public void aggregate( FlowProcess flowProcess,
                         AggregatorCall<Context> aggregatorCall )
    {
    TupleEntry arguments = aggregatorCall.getArguments();
    Context context = aggregatorCall.getContext();

    // add the current argument value to the current sum
    context.value += arguments.getInteger( 0 );
    }

  public void complete( FlowProcess flowProcess,
                        AggregatorCall<Context> aggregatorCall )
    {
    Context context = aggregatorCall.getContext();

    // create a Tuple to hold our result values
    Tuple result = new Tuple();

    // set the sum
    result.add( context.value );

    // return the result Tuple
    aggregatorCall.getOutputCollector().add( result );
    }
  }
//@extract-end