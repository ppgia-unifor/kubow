module newssite.tactics;

import model "ZNewsSys:Acme" { ZNewsSys as M, ZNewsFam as T};
import op "org.sa.rainbow.stitch.lib.*";
import op "org.sa.rainbow.model.acme.znn.ZNN";

/**
 * Lowers fidelity by integral steps for percent of requests.
 * Utility: [v] R; [v] C; [v] F
 */
tactic lowerFidelity () {
    condition {
        // some client should be experiencing high response time
        exists c : T.ClientT in M.components | c.experRespTime > M.MAX_RESPTIME;
        // exists server with fidelity to lower
        exists s : T.ServerT in M.components | s.fidelity > step;
    }
    action {
        // retrieve set of servers who still have enough fidelity grade to lower
        set servers =  select s : T.ServerT in M.components | s.fidelity > 2;
        for (T.ServerT server : servers) {
            M.setFidelity(server, 2);
        }
    }
    effect {
        // response time decreasing below threshold should result
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
    }
}

tactic highFidelity () {
    condition {
        // some client should be experiencing low response time
        exists c : T.ClientT in M.components | c.experRespTime < M.MAX_RESPTIME;
        // exists server with fidelity to lower
        exists s : T.ServerT in M.components | s.fidelity > 5;
    }
    action {
        // retrieve set of servers who still have enough fidelity grade to lower
        set servers =  select s : T.ServerT in M.components | s.fidelity <= 2;
        for (T.ServerT server : servers) {
            M.setFidelity(server, 5);
        }
    }
    effect {
        // still NO client with high response time
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;

    }
}
