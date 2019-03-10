/*
 * Adaptation script for kube-rainbow systems
 */

module kuberainbow.strategies;

import model "KubeZnnSystem:Acme" { KubeZnnSystem as M, KubernetesFam as K };
import lib "tactics.s";
import op "org.sa.rainbow.model.stitch.*";

define boolean cHiRespTime = M.kubeZnnS.latency > 1000;
define boolean highMode = org.sa.rainbow.stitch.Operators.containerImage(M.kubeZnnD, "znn", "cmendes/znn:high");

