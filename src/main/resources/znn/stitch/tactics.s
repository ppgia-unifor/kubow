module newssite.tactics;

import model "ZNewsSys:Acme" { ZNewsSys as M, Kubernetes as K};
import op "org.sa.rainbow.stitch.lib.*";

tactic decreaseFidelity (int step) {
  condition {
      M.znn-service.latency > 2000;
  }
  action {
      set lbs = {select l : K.Service in M.components | l.latency > 2000};
	  for (K.Service l : lbs) {
	      M.setFidelity(l, step);
	  }
  }
  effect {
      M.znn-service.latency < 2000;
  }
}