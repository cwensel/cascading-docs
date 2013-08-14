/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;

//@extract-start custom-subassembly
public class SomeSubAssembly extends SubAssembly
  {
  public SomeSubAssembly( Pipe lhs, Pipe rhs )
    {
    // must register incoming pipes
    setPrevious( lhs, rhs );

    // continue assembling against lhs
    lhs = new Each( lhs, new SomeFunction() );
    lhs = new Each( lhs, new SomeFilter() );

    // continue assembling against rhs
    rhs = new Each( rhs, new SomeFunction() );

    // joins the lhs and rhs
    Pipe join = new CoGroup( lhs, rhs );

    join = new Every( join, new SomeAggregator() );

    join = new GroupBy( join );

    join = new Every( join, new SomeAggregator() );

    // the tail of the assembly
    join = new Each( join, new SomeFunction() );

    // must register all assembly tails
    setTails( join );
    }
  }
//@extract-end
