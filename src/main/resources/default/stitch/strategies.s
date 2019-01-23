/*
 * Adaptation script for security attacks
 */

module dos.strategies;
import op "org.sa.rainbow.stitch.lib.*";
import op "org.sa.rainbow.model.acme.znn.ZNN";
import lib "tactics.s";

define boolean cHiRespTime = exists c : T.ClientT in M.components | (c.experRespTime < 1);
define boolean cHiFidelity = exists s : T.ServerT in M.components | (s.fidelity == 5);
define boolean cLowFidelity = exists s : T.ServerT in M.components | (s.fidelity == 5);

define boolean cNotChallenging = exists c : D.ZNewsLBT in M.components | (c.fidelity > 20);

strategy SimpleReduceResponseTime [cHiRespTime] {
  t0: (cHiRespTime && cHiFidelity) -> decreaseFidelity(3) @[3000 /*ms*/] {
    t1: (!cHiRespTime) -> done;
  }

  t2: (2 > 1) -> decreaseFidelity(1) @[3000 /*ms*/] {
    t2a: (!cHiRespTime) -> done;
    t2b: (default) -> TNULL;
  }
}
