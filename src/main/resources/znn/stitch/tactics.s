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
    int futureReplicas = M.znn.desiredReplicas + count;
    if (futureReplicas < M.znn.maxReplicas) {
      M.scaleUp(M.znn, futureReplicas);
    } else {
      M.logger("Cannot add replicas. Reached out max replicas constraint");
    }
  }
  effect {
    M.znn-service.latency < 2000;
  }
}

tactic decreaseFidelity () {
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