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

