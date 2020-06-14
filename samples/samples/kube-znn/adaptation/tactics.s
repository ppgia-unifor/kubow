module kubow.strategies;
import model "KubeZnnSystem:Acme" { KubeZnnSystem as M, KubernetesFam as K };

define boolean textMode = M.kubeZnnD.replicasLow > 0;
define boolean lowMode = M.kubeZnnD.replicasMid > 0;
define boolean highMode = M.kubeZnnD.replicasHigh > 0;

tactic addReplicas(int count) {
  int replicas = M.kubeZnnD.desiredReplicas;
  condition {
    M.kubeZnnD.maxReplicas > M.kubeZnnD.desiredReplicas;
  }
  action {
    M.scaleUp(M.kubeZnnD, count);
  }
  effect @[10000] {
    replicas' + count == M.kubeZnnD.desiredReplicas;
  }
}

tactic removeReplicas(int count) {
  int replicas = M.kubeZnnD.desiredReplicas;
  condition {
    M.kubeZnnD.minReplicas < M.kubeZnnD.desiredReplicas;
  }
  action {
    M.scaleDown(M.kubeZnnD, count);
  }
  effect @[10000] {
    replicas' - count == M.kubeZnnD.desiredReplicas;
  }
}

tactic lowerFidelity() {
  condition {
    lowMode || highMode;
  }
  action {
    if (highMode) {
      M.rollOut(M.kubeZnnD, "znn", "cmendes/znn:low");
    }
    if (lowMode) {
      M.rollOut(M.kubeZnnD, "znn", "cmendes/znn:text");
    }
  }
  effect @[10000] {
    br.unifor.kubow.adaptation.KubernetesUtils.containerImage(M.kubeZnnD, "znn") == "cmendes/znn:low" || br.unifor.kubow.adaptation.KubernetesUtils.containerImage(M.kubeZnnD, "znn") == "cmendes/znn:text";
  }
}

tactic raiseFidelity() {
  condition {
    textMode || lowMode;
  }
  action {
    if (textMode) {
      M.rollOut(M.kubeZnnD, "znn", "cmendes/znn:low");
    }
    if (lowMode) {
      M.rollOut(M.kubeZnnD, "znn", "cmendes/znn:high");
    }
  }
  effect @[10000] {
    br.unifor.kubow.adaptation.KubernetesUtils.containerImage(M.kubeZnnD, "znn") == "cmendes/znn:low" || br.unifor.kubow.adaptation.KubernetesUtils.containerImage(M.kubeZnnD, "znn") == "cmendes/znn:high";
  }
}