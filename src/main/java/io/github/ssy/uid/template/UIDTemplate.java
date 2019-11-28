package io.github.ssy.uid.template;

import io.github.ssy.uid.template.baidu.impl.CachedUidGenerator;
import io.github.ssy.uid.template.worker.ChenWorkerIdAssigner;

public class UIDTemplate {

  public static void main(String args[]) throws Exception {

  }



  public static void buildBaiDu() throws Exception {
    CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
    cachedUidGenerator.setWorkerIdAssigner(new ChenWorkerIdAssigner());
    cachedUidGenerator.afterPropertiesSet();
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 700000; i++) {
      cachedUidGenerator.getUID();
    }
    System.out.println(System.currentTimeMillis() - startTime);
  }

}
