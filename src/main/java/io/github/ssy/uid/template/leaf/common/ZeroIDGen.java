package io.github.ssy.uid.template.leaf.common;

import io.github.ssy.uid.template.leaf.IDGen;

public class ZeroIDGen implements IDGen {

  @Override
  public Result get(String key) {
    return new Result(0, Status.SUCCESS);
  }

  @Override
  public boolean init() {
    return true;
  }
}
