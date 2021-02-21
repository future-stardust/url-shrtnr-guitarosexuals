package edu.kpi.testcourse.bigtable;

// ⚠️ Please, pay attention, that you should not use any 3rd party persistence library (e.g. data
// ⚠️ base, implementation of key-value storage, etc)

/**
 * BigTable is a key-value storage...
 */
public interface BigTable {
  void put(String key, String value);

  String get(String key);
}
