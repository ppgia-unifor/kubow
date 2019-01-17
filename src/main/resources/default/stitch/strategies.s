/*
 * Adaptation script for security attacks
 */

module dos.strategies;
import op "org.sa.rainbow.stitch.lib.*";
import op "org.sa.rainbow.model.acme.znn.ZNN";
import lib "tactics.s";

// Non-malicious clients are suffering high response times
define boolean cHiRespTime = exists c : T.ClientT/*,D.ZNewsClientT*/ in M.components | (c.experRespTime > M.MAX_RESPTIME);


strategy SimpleReduceResponseTime
[cHiRespTime] {
  t0: (/*hiLoad*/ !cHiRespTime) -> enlistServers(1) @[1000 /*ms*/] {
    t1: (cHiRespTime) -> done;
    t2: (cHiRespTime) -> lowerFidelity(2, 100) @[3000 /*ms*/] {
      t2a: (cHiRespTime) -> done;
      t2b: (default) -> TNULL;  // in this case, we have no more steps to take
    }
  }
}