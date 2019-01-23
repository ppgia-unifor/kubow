module newssite.tactics;

import model "ZNewsSys:Acme" { ZNewsSys as M, ZNewsFam as T};
import op "org.sa.rainbow.stitch.lib.*";
import op "org.sa.rainbow.model.acme.znn.ZNN";

tactic decreaseFidelity (step) {
    condition {
        exists c : T.ClientT in M.components | c.experRespTime > 2;
        exists s : T.ServerT in M.components | s.fidelity > 20;
    }
    action {
        set servers = select s : T.ServerT in M.components | s.fidelity > step;
        for (T.ServerT server : servers) {
            M.setFidelity(server, step);
        }
    }
    effect {
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
    }
}

tactic increaseFidelity (step) {
    condition {
        exists c : T.ClientT in M.components | c.experRespTime <= 3;
        exists s : T.ServerT in M.components | s.fidelity < 30;
    }
    action {
        set servers = select s : T.ServerT in M.components | s.fidelity < step;
        for (T.ServerT server : servers) {
            M.setFidelity(server, step);
        }
    }
    effect {
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;

    }
}
