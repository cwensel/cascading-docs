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
