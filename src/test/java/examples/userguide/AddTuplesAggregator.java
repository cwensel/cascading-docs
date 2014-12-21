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