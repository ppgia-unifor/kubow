module newssite.tactics;

import model "ZNewsSys:Acme" { ZNewsSys as M, Kubernetes as K};
import op "org.sa.rainbow.stitch.lib.*";

tactic decreaseFidelity (int step) {
  condition {
    M.znn-service.latency > 2000;
  }
  action {
    if (step == 3) {
      M.rollout(M.znn, "znn", "cmendes/znn:low");
    }

    if (step == 1) {
      M.rollout(M.znn, "znn", "cmendes/znn:text");
    }
  }
  effect {
    M.znn-service.latency < 2000;
  }
}