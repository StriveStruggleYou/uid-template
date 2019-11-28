package io.github.ssy.uid.template.worker;

public class ChenWorkerIdAssigner implements WorkerIdAssigner {

  @Override
  public long assignWorkerId() {
    return 1L;
  }
}
