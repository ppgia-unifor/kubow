/*
 * Adaptation script for kube-rainbow systems
 */

module kuberainbow.strategies;

import model "ZNewsSys:Acme" { ZNewsSys as M, Kubernetes as K };
import lib "tactics.s";
import op "org.sa.rainbow.model.stitch.*";

define boolean cHiRespTime = M.znn-service.latency > 2000;
define boolean highMode = org.sa.rainbow.stitch.Operators.containerImage(M.znn, "znn", "cmendes/znn:high");

strategy AddCapacity [cHiRespTime && !highMode] {
  t0: (cHiRespTime) -> addReplicas(1) @[5000] {
    t1: (!cHiRespTime) -> done;
  }
}

strategy SimpleReduceLatency [cHiRespTime && highMode] {
  t0: (cHiRespTime) -> decreaseFidelity() @[5000] {
    t1: (!cHiRespTime) -> done;
  }
}

