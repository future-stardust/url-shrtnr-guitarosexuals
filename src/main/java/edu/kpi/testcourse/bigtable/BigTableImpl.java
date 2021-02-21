package edu.kpi.testcourse.bigtable;

import java.util.HashMap;
import java.util.Map;

// ⚠️ Please, pay attention, that you should not use any 3rd party persistence library (e.g. data
// ⚠️ base, implementation of key-value storage, etc)

class BigTableImpl implements BigTable {
  private final Map<String, String> map = new HashMap<>();

  @Override
  public void put(String key, String value) {
    map.put(key, value);
  }

  @Override
  public String get(String key) {
    return map.get(key);
  }
}
