package io.github.ssy.uid.template.leaf;


import io.github.ssy.uid.template.leaf.common.Result;

public interface IDGen {

  Result get(String key);

  boolean init();
}
