package br.unifor.kubow.effectors;

import org.junit.Test;

import java.util.ArrayList;

public class RetryEffectorTest {

    @Test
    public void should_update_to() {
        RetryEffector effector = new RetryEffector("", "");
        var args = new ArrayList();

        args.add("default");
        args.add("podinfo");
        args.add("podinfo");
        args.add("v1");
        args.add("4");
        args.add("4s");
        effector.execute(args);
    }
}
