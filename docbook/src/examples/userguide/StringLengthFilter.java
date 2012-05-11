/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
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

  public boolean isRemove( FlowProcess flowProcess, FilterCall call )
    {
    // get the arguments TupleEntry
    TupleEntry arguments = call.getArguments();

    // filter out the current Tuple if the first argument length is greater
    // than the second argument integer value
    return arguments.getString( 0 ).length() > arguments.getInteger( 1 );
    }
  }
//@extract-end
