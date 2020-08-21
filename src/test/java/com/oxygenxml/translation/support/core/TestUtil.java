package com.oxygenxml.translation.support.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.oxygenxml.translation.support.storage.ResourceInfo;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Utility methods used for test.
 */
public class TestUtil {
  
  /**
   * Private constructor to avoid instantiation of utility class.
   */
  private TestUtil() {}
  
	/**
	 * @param list A list with ResourceInfo objects.
	 * 
	 * @return A sorted and aligned string.
	 */
  public static String dump(List<ResourceInfo> list) {
    list.sort(new Comparator<Object>() {
      public int compare(Object ListOne, Object ListTwo) {
        return ((ResourceInfo)ListOne).getRelativePath().compareTo(((ResourceInfo)ListTwo).getRelativePath());
      }
    });
	  
		StringBuilder b = new StringBuilder();
		int maxLength = 0;
		for(int j = 0;j < list.size();j++){
			int length = list.get(j).getRelativePath().length();
			if(maxLength < length){
				maxLength = length;
			}
		}
		for(int i=0;i<list.size();i++){
			String relativePath = list.get(i).getRelativePath();
			b.append(relativePath);
			for(int k=0;k <= maxLength - relativePath.length() + 1;k++){
				b.append(" ");
			}
			b.append(list.get(i).getMd5());
			b.append("\n");
		}
		
		return b.toString();
	}
  
  /**
   * Gets the needed resource.
   * 
   * @param dirName The name of the resource we need.
   * 
   * @return The file/directory specified by dirName.
   */
  public static File getPath(String dirName){
    URL resource = TestUtil.class.getClassLoader().getResource(dirName);
    return new File(resource.getPath());
  }
  
  /**
   * Read content of file.
   * 
   * @param file The file we want to read from.
   *  
   * @return File content or empty string.
   * 
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static String readFile(File file) throws FileNotFoundException, IOException {
    InputStream is = new FileInputStream(file);
    BufferedReader in = new BufferedReader(new InputStreamReader(is));

    String line = in.readLine();
    StringBuilder result = new StringBuilder(4000);
    while (line != null) {
      result.append(line);
      result.append('\n');
      line = in.readLine();
    }
    in.close();
    return result.toString();
  }
  
  /**
   * Reads the bytes into a string.
   * 
   * @param bytes Bytes to read.
   * @param encoding Encoding to use.
   * 
   * @return The string representation.
   * 
   * @throws IOException If it fails.
   */
  public static String read(byte[] bytes, String encoding) throws IOException {
    StringBuilder b = new StringBuilder();
    try (InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(bytes), encoding);) {
      int l = 0;
      char[] cbuf = new char[1024];
      while ((l = inputStreamReader.read(cbuf )) != -1) {
        b.append(cbuf, 0, l);
      }
    }
    
    return b.toString();
  }

  /**
   * Gets the entries in the archive.
   * 
   * @param packageFile Archive file.
   * 
   * @return A list with all the entries from the archive.
   * 
   * @throws ZipException
   * @throws IOException
   */
  public static String getZipEntries(File packageFile) throws ZipException, IOException {
    List<String> entries = new ArrayList<>();
    try (ZipFile zipFile = new ZipFile(packageFile)) {
      Enumeration<?> enu = zipFile.entries();
      while (enu.hasMoreElements()) {
        ZipEntry zipEntry = (ZipEntry) enu.nextElement();
        String name = zipEntry.getName();
        entries.add(name);
      }

      StringBuilder b = new StringBuilder();
      Collections.sort(entries);
      for (String string : entries) {
        b.append(string).append("\n");
      }
      
      return b.toString();
    }
  }
}
