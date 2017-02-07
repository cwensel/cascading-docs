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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.type.CoercibleType;
import cascading.tuple.type.DateType;
import org.junit.Test;
import tools.ExampleTestCase;

/**
 *
 */
public class TypeTest extends ExampleTestCase
  {
  @Test
  public void testCreateTimestamp() throws IOException
    {
    // internally represented as a Long

    //@extract-start fields-date-type
    SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MMM/yyyy:HH:mm:ss:SSS Z" );
    Date firstDate = new Date();
    String stringFirstDate = dateFormat.format( firstDate );

    CoercibleType coercible = new DateType( "dd/MMM/yyyy:HH:mm:ss:SSS Z", TimeZone.getDefault() );

    // create the Fields, Tuple, and TupleEntry
    Fields fields = new Fields( "dateString", "dateValue" ).applyTypes( coercible, long.class );
    Tuple tuple = new Tuple( firstDate.getTime(), firstDate.getTime() );
    TupleEntry results = new TupleEntry( fields, tuple );

    // test the results
    assert results.getObject( "dateString" ).equals( firstDate.getTime() );
    assert results.getLong( "dateString" ) == firstDate.getTime();
    assert results.getString( "dateString" ).equals( stringFirstDate );
    assert !results.getString( "dateString" ).equals( results.getString( "dateValue" ) ); // not equals

    Date secondDate = new Date( firstDate.getTime() + ( 60 * 1000 ) );
    String stringSecondDate = dateFormat.format( secondDate );

    results.setString( "dateString", stringSecondDate );
    results.setLong( "dateValue", secondDate.getTime() );

    assert !results.getObject( "dateString" ).equals( firstDate.getTime() ); // equals
    assert results.getObject( "dateString" ).equals( secondDate.getTime() ); // not equals
    //@extract-end
    }
  }