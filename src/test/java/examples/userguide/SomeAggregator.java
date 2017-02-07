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