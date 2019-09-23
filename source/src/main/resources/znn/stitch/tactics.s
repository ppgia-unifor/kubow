module newssite.tactics;

import model "KubeZnnSystem:Acme" { KubeZnnSystem as M, Kubernetes as K};
import op "org.sa.rainbow.stitch.lib.*";


define boolean lowMode = org.sa.rainbow.stitch.Operators.containerImage(M.znn, "znn", "cmendes/znn:low");
define boolean highMode = org.sa.rainbow.stitch.Operators.containerImage(M.znn, "znn", "cmendes/znn:high");


tactic addReplicas(int count) {
  condition {
    M.kubeZnnD.maxReplicas >= M.kubeZnnD.desiredReplicas + count;
  }
  action {
     M.scaleUp(M.kubeZnnD, M.kubeZnnD.desiredReplicas + count);
  }
  effect {
    M.kubeZnnD.maxReplicas >= M.kubeZnnD.desiredReplicas;
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

tactic lowerFidelity () {
  condition {
    lowMode || highMode;
  }
  action {
    if (highMode) {
      M.rollOut(M.kubeZnn, "znn", "cmendes/znn:low");
    }

    if (lowMode) {
      M.rollOut(M.kubeZnn, "znn", "cmendes/znn:text");
    }
  }
  effect {
    !highMode;
  }
}

tactic lowerFidelityConfig () {
  condition {
    lowMode || highMode;
  }
  action {
    if (highMode) {
      M.updateConfig("default", "fidelity-config", "fidelity=low");
    }

    if (lowMode) {
      M.updateConfig("default", "fidelity-config", "fidelity=text");
    }
  }
  effect {
    !highMode;
  }
}