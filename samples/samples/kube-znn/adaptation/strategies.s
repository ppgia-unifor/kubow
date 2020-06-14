module kubow.strategies;
import model "KubeZnnSystem:Acme" { KubeZnnSystem as M, KubernetesFam as K };
import lib "tactics.s";

define boolean textMode = M.kubeZnnD.replicasLow > 0;
define boolean lowMode = M.kubeZnnD.replicasMid > 0;
define boolean highMode = M.kubeZnnD.replicasHigh > 0;

define boolean cHiRespTime = M.kubeZnnS.slo > M.EXPECTED_SLO;
define boolean cLoRespTime = M.kubeZnnS.slo < M.EXPECTED_SLO;
define boolean canAddPod = M.kubeZnnD.maxReplicas > M.kubeZnnD.desiredReplicas;
define boolean canRemovePod = M.kubeZnnD.minReplicas < M.kubeZnnD.desiredReplicas;

strategy ReduceRespTime [ cHiRespTime ] {
  t0: (cHiRespTime && canAddPod) -> addReplicas(1) @[15000 /*ms*/] {
    t0a: (success) -> done;
  }
  t1: (cHiRespTime && !canAddPod) -> lowerFidelity() @[15000 /*ms*/] {
    t1a: (success) -> done;
  }
  t2: (default) -> TNULL;
}

strategy ReduceCost [ cLoRespTime && highMode ] {
  t0: (cLoRespTime && canRemovePod && highMode) -> removeReplicas(1) @[15000 /*ms*/] {
    t0a: (success) -> done;
  }
  t1: (default) -> TNULL;
}

strategy ImproveFidelity [ cLoRespTime && (textMode || lowMode) ] {
  t0: (cLoRespTime && (textMode || lowMode)) -> raiseFidelity() @[15000 /*ms*/] {
    t0a: (success) -> done;
  }
  t1: (default) -> TNULL;
}
