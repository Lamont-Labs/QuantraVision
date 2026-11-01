package com.lamontlabs.quantravision.next;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.Failure;
public final class ProofListener extends RunListener {
    @Override public void testFailure(Failure failure) throws Exception {Proof.save("FAILED-"+failure.getDescription().getMethodName());}
}
