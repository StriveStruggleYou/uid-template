package io.github.ssy.uid.template.leaf.segment.dao;

import io.github.ssy.uid.template.leaf.segment.model.LeafAlloc;
import java.util.List;

public interface IDAllocDao {

  List<LeafAlloc> getAllLeafAllocs();

  LeafAlloc updateMaxIdAndGetLeafAlloc(String tag);

  LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc);

  List<String> getAllTags();
}
