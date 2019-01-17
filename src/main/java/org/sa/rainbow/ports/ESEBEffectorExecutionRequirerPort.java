/*
 * The MIT License
 *
 * Copyright 2014 Cimport java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
ut limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.ports;

import edu.cmu.cs.able.eseb.rpc.OperationTimedOutException;
import org.sa.rainbow.core.ports.eseb.rpc.IESEBEffectorExecutionRemoteInterface;
import org.sa.rainbow.core.ports.local.EffectorExecutionRegister;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

import java.util.List;

public class ESEBEffectorExecutionRequirerPort implements IESEBEffectorExecutionRemoteInterface {

    private final IEffectorExecutionPort m_stub;

    public ESEBEffectorExecutionRequirerPort (EffectorExecutionRegister register, IEffectorIdentifier effector) {
        m_stub = register.find(effector);
    }

    @Override
    public Outcome execute (List<String> args) {
        try {
            return m_stub.execute (args);
        }
        catch (OperationTimedOutException e) {
            return Outcome.TIMEOUT;
        }
    }

    @Override
    public void dispose() {

    }
}
