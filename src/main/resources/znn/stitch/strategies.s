/*
 * Adaptation script for kube-rainbow systems
 */

module kuberainbow.strategies;

import model "ZNewsSys:Acme" { ZNewsSys as M, Kubernetes as K };
import lib "tactics.s";

define boolean cHiRespTime = M.znn-service.latency > 2000;

strategy SimpleReduceResponseTime [cHiRespTime] {
  t0: (cHiRespTime) -> decreaseFidelity(3) @[5000] {
    t1: (!cHiRespTime) -> done;
  }
}
