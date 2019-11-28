package io.github.ssy.uid.template.uuid;

import com.fasterxml.uuid.Generators;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class UuidUtil {

  public static void main(String args[]) throws InterruptedException, NoSuchAlgorithmException {

    Thread.sleep(5000);
    long startTime = System.currentTimeMillis();
    Set<String> uidSet = new HashSet<String>();
    for (int i = 0; i < 1000000; i++) {
    }
    System.out.println(System.currentTimeMillis() - startTime);
    System.out.println(uidSet.size());

  }

}
