/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2012 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.maxsum;

import es.csic.iiia.planes.Task;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class MSFunction {
    private static final Logger LOG = Logger.getLogger(MSFunction.class.getName());

    private MSPlane plane;

    private final Task task;

    public MSFunction(MSPlane plane, Task t) {
        this.plane = plane;
        this.task = t;
    }

    protected final Map<MSPlane, MSVariable2FunctionMessage> lastMessages =
            new TreeMap<MSPlane, MSVariable2FunctionMessage>();

    private final Minimizer<MSPlane> minimizer = new Minimizer<MSPlane>();

    public void gather() {
        minimizer.reset();

        Set<MSPlane> neighbors = plane.getNeighbors();

        Iterator<Entry<MSPlane, MSVariable2FunctionMessage>> it =
                lastMessages.entrySet().iterator();
        while (it.hasNext()) {
            final MSPlane p = it.next().getKey();

            // Cleanup old messages
            if (!neighbors.contains(p)) {
                it.remove();
                continue;
            }

            final MSVariable2FunctionMessage msg = lastMessages.get(p);
            minimizer.track(p, msg.getValue());
        }
    }

    public void scatter() {
        for (MSPlane p : plane.getNeighbors()) {
            double value = - minimizer.getComplementary(p);
            final MSFunction2VariableMessage msg = new MSFunction2VariableMessage(task, value);
            msg.setRecipient(p);

            plane.send(msg);
            LOG.log(Level.FINE, "Sending {0} to {1}", new Object[]{msg, msg.getRecipient()});
        }
    }

    void receive(MSVariable2FunctionMessage msg) {
        lastMessages.put(msg.getSender(), msg);
    }

}