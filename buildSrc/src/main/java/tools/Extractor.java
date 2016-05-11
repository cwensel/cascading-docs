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

package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 * Class to extract code from java source files into XML files that can be
 * included in DocBook books.
 * <p/>
 * The class will search for annotated java source files and extract the annotated source
 * as an XML file.
 */
public class Extractor
  {

  private final Type type;

  enum Type
    {
    XML,
    ASCIIDOC
    }

  protected String startAnnotation1 = "//@extract-start";
  protected String startAnnotation2 = "<!-- @extract-start";
  protected String endAnnotation = "@extract-end";

  protected File targetDir;
  protected String root;
  private static final String BEGIN_CDATA = "<![CDATA[";
  private static final String END_CDATA = "]]>";

  public Extractor( String root, File targetDir )
    {
    this.type = Type.ASCIIDOC;
    this.root = root;
    this.targetDir = targetDir;
    }

  public static void main( String[] args ) throws Exception
    {

    File sourceDir = new File( args[ 0 ] );
    File targetDir = new File( args[ 1 ] );

    Extractor extractor = new Extractor( sourceDir.getAbsolutePath(), targetDir );
    extractor.extractCode( sourceDir );

    }

  /** @param sourceDir containing java source files */
  public void extractCode( File sourceDir ) throws Exception
    {
    System.out.println( "Extracting dir " + sourceDir );

    // List the source directory. If the file is a dir recurse,
    // if the file is a java file check for Extract annotations
    // otherwise ignore

    File[] elements = sourceDir.listFiles();

    for( int i = 0; i < elements.length; ++i )
      {
      File file = elements[ i ];

      if( file.isDirectory() )
        extractCode( file );
      else if( ( file.getName().endsWith( ".java" ) || file.getName().endsWith( ".xml" ) ) )
        extractAnnotatedCode( file );
      } // rof
    }

  public void extractAnnotatedCode( File file ) throws Exception
    {
//    System.out.println( "Handling: " + file );
    String packageName = file.getParentFile().getName();

    BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) );
    String line;
    boolean extract = false;
    BufferedWriter writer = null;

    boolean firstWrite = true;
    int offset = -1;

    while( ( line = reader.readLine() ) != null )
      {
      if( extract )
        {
        if( line.contains( endAnnotation ) )
          {
          closeFile( writer );
          firstWrite = true;
          extract = false;
          writer = null;
          }
        else
          {
          if( line.length() > offset )
            line = line.substring( offset );

          // skip blank lines up to the code
          if( firstWrite && line.trim().length() == 0 )
            continue;

          if( firstWrite )
            firstWrite = false;
          else
            writer.newLine();

          // enable callouts - unused - need new notation
          switch( type )
            {
            case XML:
              line = line.replaceAll( "//(<co.*/>)", END_CDATA +"$1"+ BEGIN_CDATA );
              break;
            case ASCIIDOC:
              line = line.replaceAll( "//(<co.*/>)", "<$1>" );
              break;
            }

          writer.append( line );
          }
        }
      else
        {
        String start = startAnnotation1;
        offset = line.indexOf( startAnnotation1 );

        if( offset == -1 )
          {
          start = startAnnotation2;
          offset = line.indexOf( startAnnotation2 );
          }

        if( offset > -1 )
          {
          System.out.println( "Extracting from: " + file );
          String name = "_" + line.substring( offset + start.length() + 1 ).trim().split( "\\s" )[ 0 ];
          extract = true;
          writer = openFile( packageName, name );
          }
        }
      }

    if( writer != null )
      closeFile( writer );
    }

  public BufferedWriter openFile( String path, String file ) throws Exception
    {
    File targetPath = new File( targetDir, path );

    targetPath.mkdirs();

    String extension = type == Type.XML ? ".xml" : ".adoc";

    File targetFile = new File( targetPath, file + extension );

    BufferedWriter writer = new BufferedWriter( new FileWriter( targetFile ) );


    switch( type )
      {
      case XML:
       return startXiIncludeFile( writer );
      case ASCIIDOC:
        return startAdocFile(writer);
      }

    return null;
    }

  public BufferedWriter startXiIncludeFile( BufferedWriter writer ) throws Exception
    {
    // write
    writer.write( "<?xml version=\"1.0\"  encoding=\"UTF-8\"?>" );
    writer.newLine();
    writer.write( "<programlisting language=\"java\" xmlns=\"http://docbook.org/ns/docbook\" version=\"5.0\" xml:lang=\"en\">" + BEGIN_CDATA );

    return writer;
    }

  public BufferedWriter startAdocFile( BufferedWriter writer ) throws Exception
    {
    // write
    writer.write( "[source,java]" );
    writer.newLine();
    writer.write( "----" );
    writer.newLine();

    return writer;
    }

  public void closeFile( BufferedWriter writer ) throws Exception
    {
    switch( type )
      {
      case XML:
        writer.write( END_CDATA );
        writer.write( "</programlisting>" );
        break;
      case ASCIIDOC:
        writer.newLine();
        writer.write( "----" );
        break;
      }
    writer.flush();
    writer.close();
    }

  }
