/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package examples.userguide;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Filter;
import cascading.operation.FilterCall;
import cascading.tuple.TupleEntry;

//@extract-start custom-filter
public class SomeFilter extends BaseOperation implements Filter
  {
  public boolean isRemove( FlowProcess flowProcess, FilterCall call )
    {
    // get the arguments TupleEntry
    TupleEntry arguments = call.getArguments();

    // initialize the return result
    boolean isRemove = false;

    // test the argument values and set isRemove accordingly

    return isRemove;
    }
  }
//@extract-end