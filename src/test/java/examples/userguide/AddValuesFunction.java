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
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

//@extract-start sum-function
public class AddValuesFunction extends BaseOperation implements Function
  {
  public AddValuesFunction()
    {
    // expects 2 arguments, fail otherwise
    super( 2, new Fields( "sum" ) );
    }

  public AddValuesFunction( Fields fieldDeclaration )
    {
    // expects 2 arguments, fail otherwise
    super( 2, fieldDeclaration );
    }

  public void operate( FlowProcess flowProcess, FunctionCall functionCall )
    {
    // get the arguments TupleEntry
    TupleEntry arguments = functionCall.getArguments();

    // create a Tuple to hold our result values
    Tuple result = new Tuple();

    // sum the two arguments
    int sum = arguments.getInteger( 0 ) + arguments.getInteger( 1 );

    // add the sum value to the result Tuple
    result.add( sum );

    // return the result Tuple
    functionCall.getOutputCollector().add( result );
    }
  }
//@extract-end
