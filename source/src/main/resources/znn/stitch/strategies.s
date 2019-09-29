/*
 * Adaptation script for kube-rainbow systems
 */

module kuberainbow.strategies;

import model "KubeZnnSystem:Acme" { KubeZnnSystem as M, KubernetesFam as K };
import lib "tactics.s";
import op "org.sa.rainbow.model.stitch.*";

define boolean cHiRespTime = M.kubeZnnS.latency > M.MAX_RESPTIME;
define boolean highMode = org.sa.rainbow.stitch.Operators.containerImage(M.kubeZnnD, "znn", "cmendes/znn:high");
define boolean canAddPods = M.kubeZnnD.maxReplicas > M.kubeZnnD.desiredReplicas;
define boolean canRemovePods = M.kubeZnnD.minReplicas < M.kubeZnnD.desiredReplicas;

strategy ReduceRespTime [ cHiRespTime ] {
  t0: (cHiRespTime && canAddPods) -> addReplicas(1) @[5000 /*ms*/] {
    t0a: (success) -> done;
  }

  t1: (cHiRespTime && !canAddPods) -> lowerFidelity() @[5000 /*ms*/] {
    t1a: (success) -> done;
  }

  t2: (default) -> TNULL;
}


strategy ReduceCapacity [ !cHiRespTime ] {
  t0: (!cHiRespTime && canRemovePods) -> removeReplicas(1) @[5000 /*ms*/] {
    t0a: (success) -> done;
  }

  t2: (default) -> TNULL;
}


