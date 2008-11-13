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

import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

//@extract-start custom-aggregator
public class SomeAggregator extends BaseOperation<SomeAggregator.Context>
  implements Aggregator<SomeAggregator.Context>
  {
  public static class Context
    {
    Object value;
    }

  public void start( FlowProcess flowProcess,
                     AggregatorCall<Context> aggregatorCall )
    {
    // get the group values for the current grouping
    TupleEntry group = aggregatorCall.getGroup();

    // create a new custom context object
    Context context = new Context();

    // optionally, populate the context object

    // set the context object
    aggregatorCall.setContext( context );
    }

  public void aggregate( FlowProcess flowProcess,
                         AggregatorCall<Context> aggregatorCall )
    {
    // get the current argument values
    TupleEntry arguments = aggregatorCall.getArguments();

    // get the context for this grouping
    Context context = aggregatorCall.getContext();

    // update the context object
    }

  public void complete( FlowProcess flowProcess,
                        AggregatorCall<Context> aggregatorCall )
    {
    Context context = aggregatorCall.getContext();

    // create a Tuple to hold our result values
    Tuple result = new Tuple();

    // insert some values into the result Tuple based on the context

    // return the result Tuple
    aggregatorCall.getOutputCollector().add( result );
    }
  }
//@extract-end