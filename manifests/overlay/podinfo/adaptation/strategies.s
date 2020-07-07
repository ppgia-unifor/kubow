module kubow.strategies;
import model "PodinfoSystem:Acme" { PodinfoSystem as M, KubernetesFam as K };
import lib "tactics.s";

define boolean highTraffic = M.podinfoS.traffic >= 50;
define boolean lowTraffic = M.podinfoS.traffic < 50;

define boolean canAddReplica = M.podinfoD.maxReplicas > M.podinfoD.desiredReplicas;
define boolean canRemoveReplica = M.podinfoD.minReplicas < M.podinfoD.desiredReplicas;

/*
 * Improves the response time by adding new replicas when traffic is high
 */
strategy ImproveSlo [ highTraffic ] {
  t0: (sloRed && canAddReplica) -> addReplicas(1) @[20000 /*ms*/] {
    t0a: (success) -> done;
  }
  t1: (default) -> TNULL;
}

/*
 * Reduces the cost by removing replicas when traffic is low
 */
strategy ReduceCost [ lowTraffic ] {
  t0: (sloGreen && canRemoveReplica) -> removeReplicas(1) @[20000 /*ms*/] {
    t0a: (success) -> done;
  }
  t1: (default) -> TNULL;
}
