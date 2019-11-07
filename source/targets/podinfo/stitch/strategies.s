module kubow.strategies;

import model "PodinfoSystem:Acme" { PodinfoSystem as M, KubernetesFam as K };
import lib "tactics.s";

define boolean textMode = br.unifor.kubow.adaptation.KubernetesUtils.getContainerImage(M.podinfoD, "podinfod") == "cmendes/znn:text";
define boolean twoPods = M.podinfoD.availableReplicas == 2;
define boolean fourPods = M.podinfoD.availableReplicas == 3;

strategy AddPods [twoPods] {
    t1: (TwoPods) -> addReplicas(1) @[30000] {
		  t1a: (default) -> done;
	}
}

strategy RemovePods [fourPods] {
    t1: (TwoPods) -> removeReplicas(1) @[30000] {
		  t1a: (default) -> done;
	}
}
