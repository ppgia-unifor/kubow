module kubow.tactics;

import model "PodinfoSystem:Acme" { PodinfoSystem as M, Kubernetes as K};

tactic addReplicas(int count) {
  condition {
    M.podinfoD.maxReplicas >= M.podinfoD.desiredReplicas;
  }
  action {
     M.scaleUp(M.podinfoD, M.podinfoD.desiredReplicas + count);
  }
  effect {
    M.podinfoD.maxReplicas >= M.podinfoD.desiredReplicas;
  }
}

tactic removeReplicas(int count) {
  condition {
    M.podinfoD.maxReplicas <= M.podinfoD.desiredReplicas;
  }
  action {
     M.scaleUp(M.podinfoD, M.podinfoD.desiredReplicas - count);
  }
  effect {
    M.podinfoD.maxReplicas <= M.podinfoD.desiredReplicas;
  }
}
