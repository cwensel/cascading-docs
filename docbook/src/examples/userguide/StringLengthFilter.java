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
import cascading.operation.BaseOperation;
import cascading.operation.Filter;
import cascading.operation.FilterCall;
import cascading.tuple.TupleEntry;

//@extract-start stringlength-filter
public class StringLengthFilter extends BaseOperation implements Filter
  {
  public StringLengthFilter()
    {
    // expects 2 arguments, fail otherwise
    super( 2 );
    }

  public boolean isRemove( FlowProcess flowProcess, FilterCall filterCall )
    {
    // get the arguments TupleEntry
    TupleEntry arguments = filterCall.getArguments();

    // filter out the current Tuple if the first argument length is greater
    // than the second argument integer value
    return arguments.getString( 0 ).length() > arguments.getInteger( 1 );
    }
  }
//@extract-end
