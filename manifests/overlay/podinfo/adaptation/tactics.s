module kubow.strategies;
import model "PodinfoSystem:Acme" { PodinfoSystem as M, KubernetesFam as K };

tactic addReplicas(int count) {
  int replicas = M.podinfoD.desiredReplicas;
  condition {
    M.podinfoD.maxReplicas > M.podinfoD.desiredReplicas;
  }
  action {
    M.scaleUp(M.podinfoD, count);
  }
  effect @[10000] {
    replicas' + count == M.podinfoD.desiredReplicas;
  }
}

tactic removeReplicas(int count) {
  int replicas = M.podinfoD.desiredReplicas;
  condition {
    M.podinfoD.minReplicas < M.podinfoD.desiredReplicas;
  }
  action {
    M.scaleDown(M.podinfoD, count);
  }
  effect @[10000] {
    replicas' - count == M.podinfoD.desiredReplicas;
  }
}
