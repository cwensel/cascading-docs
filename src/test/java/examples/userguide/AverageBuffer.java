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

import java.util.Iterator;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Buffer;
import cascading.operation.BufferCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

//@extract-start average-buffer
public class AverageBuffer extends BaseOperation implements Buffer
  {

  public AverageBuffer()
    {
    super( 1, new Fields( "average" ) );
    }

  public AverageBuffer( Fields fieldDeclaration )
    {
    super( 1, fieldDeclaration );
    }

  public void operate( FlowProcess flowProcess, BufferCall bufferCall )
    {
    // init the count and sum
    long count = 0;
    long sum = 0;

    // get all the current argument values for this grouping
    Iterator<TupleEntry> arguments = bufferCall.getArgumentsIterator();

    while( arguments.hasNext() )
      {
      count++;
      sum += arguments.next().getInteger( 0 );
      }

    // create a Tuple to hold our result values
    Tuple result = new Tuple( sum / count );

    // return the result Tuple
    bufferCall.getOutputCollector().add( result );
    }
  }
//@extract-end

