module kubow.strategies;

import model "PodinfoSystem:Acme" { PodinfoSystem as M, KubernetesFam as K };
import lib "tactics.s";

define boolean TwoPods = M.podinfoD.availableReplicas == 2;

strategy DuplicateReplicas [TwoPods] {
    t1: (TwoPods) -> addReplicas(2) @[30000] {
		  t1a: (default) -> done;
	}
}
