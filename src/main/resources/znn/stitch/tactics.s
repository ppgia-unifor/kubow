module newssite.tactics;

import model "ZNewsSys:Acme" { ZNewsSys as M, Kubernetes as K};
import op "org.sa.rainbow.stitch.lib.*";


define boolean lowMode = org.sa.rainbow.stitch.Operators.containerImage(M.znn, "znn", "cmendes/znn:low");
define boolean highMode = org.sa.rainbow.stitch.Operators.containerImage(M.znn, "znn", "cmendes/znn:high");


tactic addReplicas(int count) {
  condition {
    M.znn-service.latency > 2000;
  }
  action {
    int desiredReplicas = M.znn.desiredReplicas;
    if ((desiredReplicas + count) < M.znn.maxReplicas) {
      M.scaleUp(M.znn, desiredReplicas + count);
    } else {
      M.logger("Cannot add replicas. Reached out max replicas constraint");
    }
  }
  effect {
    M.znn.desiredReplicas > desiredReplicas + count;
  }
}

tactic decreaseFidelity (int step) {
  condition {
    M.znn-service.latency > 2000;
  }
  action {

    if (highMode) {
      M.rollOut(M.znn, "znn", "cmendes/znn:low");
    }

    if (lowMode) {
      M.rollOut(M.znn, "znn", "cmendes/znn:text");
    }
  }
  effect {
    M.znn-service.latency < 2000;
  }
}