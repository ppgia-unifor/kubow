module kubow.tactics;

import model "PodinfoSystem:Acme" { PodinfoSystem as M, Kubernetes as K};
import op "org.sa.rainbow.stitch.lib.*";

tactic addReplicas(int count) {
  condition {
    M.podinfoD.maxReplicas >= M.podinfoD.desiredReplicas + count;
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
    M.znn-service.latency < 1000;
  }
  action {
    int futureReplicas = M.znn.desiredReplicas - count;
    M.scaleDown(M.znn, futureReplicas);
  }
  effect {
    M.znn-service.latency < 1000;
  }
}
