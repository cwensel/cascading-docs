/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.concurrentinc.com/
 */

package userguide;

import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;

//@extract-start split-subassembly
public class SplitSubAssembly extends SubAssembly
  {
  public SplitSubAssembly( Pipe pipe )
    {
    // continue assembling against pipe
    pipe = new Each( pipe, new SomeFunction() );

    Pipe lhs = new Pipe( "lhs", pipe );
    lhs = new Each( lhs, new SomeFunction() );

    Pipe rhs = new Pipe( "rhs", pipe );
    rhs = new Each( rhs, new SomeFunction() );

    // must register all assembly tails
    setTails( lhs, rhs );
    }
  }
//@extract-end

