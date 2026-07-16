/*
  BSD 3-Clause License
  Copyright (c) 2022, Automatic Controls Equipment Systems, LLC.
  Contributors: Cameron Vogt (@cvogt729)
*/
package aces.webctrl.cli.core;
import java.util.regex.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class Utility {
  private final static Pattern lineEnding = Pattern.compile("\\r?+\\n");
  private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
  /**
   * @return a hex string representation of the given bytes.
   */
  public static String bytesToHex(byte[] bytes, int offset, int length){
    if (bytes==null){
      return "";
    }
    if (offset>bytes.length){
      offset = bytes.length;
    }else if (offset<0){
      offset = 0;
    }
    if (length<0){
      length = 0;
    }
    int lim = offset+length;
    if (lim>bytes.length){
      lim = bytes.length;
      length = lim-offset;
    }
    if (length==0){
      return "";
    }
    byte[] hexChars = new byte[length<<1];
    int v,k;
    for (int j = 0; j < length; ++j) {
      v = bytes[j+offset] & 0xFF;
      k = j<<1;
      hexChars[k] = HEX_ARRAY[v >>> 4];
      hexChars[k+1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars, StandardCharsets.UTF_8);
  }
  /**
   * This method is provided for compatibility with older JRE versions.
   * Newer JREs already have a built-in equivalent of this method: {@code InputStream.readAllBytes()}.
   * @return a {@code byte[]} array containing all remaining bytes read from the {@code InputStream}, or {@code null} if the limit is exceeded.
   */
  public static byte[] readAllBytes(InputStream s, int limit) throws IOException {
    final byte[] buffer = new byte[limit > 0 ? Math.min(limit, 8192) : 8192];
    final ByteArrayOutputStream out = new ByteArrayOutputStream(buffer.length);
    int total = 0;
    int read;
    while ((read = s.read(buffer, 0, buffer.length)) != -1){
      total += read;
      if (limit > 0 && total > limit){
        return null;
      }
      out.write(buffer, 0, read);
    }
    return out.toByteArray();
  }
  /**
   * Loads all bytes from the given resource and convert to a {@code UTF-8} string.
   * @return the {@code UTF-8} string representing the given resource.
   */
  public static String loadResourceAsString(String name) throws Throwable {
    byte[] arr;
    try(
      InputStream s = Utility.class.getClassLoader().getResourceAsStream(name);
    ){
      arr = readAllBytes(s, -1);
    }
    return lineEnding.matcher(new String(arr, java.nio.charset.StandardCharsets.UTF_8)).replaceAll(System.lineSeparator());
  }
  /**
   * Loads all bytes from the given resource and convert to a {@code UTF-8} string.
   * @return the {@code UTF-8} string representing the given resource.
   */
  public static String loadResourceAsString(ClassLoader cl, String name) throws Throwable {
    byte[] arr;
    try(
      InputStream s = cl.getResourceAsStream(name);
    ){
      arr = readAllBytes(s, -1);
    }
    return lineEnding.matcher(new String(arr, java.nio.charset.StandardCharsets.UTF_8)).replaceAll(System.lineSeparator());
  }
  /**
   * Reverses the order and XORs each character with 4.
   * The array is modified in-place, so no copies are made.
   * For convenience, the given array is returned.
   */
  public static char[] obfuscate(char[] arr){
    char c;
    for (int i=0,j=arr.length-1;i<=j;++i,--j){
      if (i==j){
        arr[i]^=4;
      }else{
        c = (char)(arr[j]^4);
        arr[j] = (char)(arr[i]^4);
        arr[i] = c;
      }
    }
    return arr;
  }
  /**
   * Reverses the order and XORs each character with 4.
   */
  public static String obfuscate(String str){
    return new String(obfuscate(str.toCharArray()));
  }
}