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
package es.csic.iiia.planes.tutorial;

import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSIAuctionsBehavior extends AbstractBehavior<TutorialPlane> {

    private Map<Task, List<BidMessage>> collectedBids =
            new HashMap<Task, List<BidMessage>>();

    public PSIAuctionsBehavior(TutorialPlane agent) {
        super(agent);
    }

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void beforeMessages() {
        collectedBids.clear();
    }

    @Override
    public void afterMessages() {
        // Open new auctions only once every four steps
        if (getAgent().getWorld().getTime() % 4 == 0) {
            openAuctions();
        }
    }

    private void openAuctions() {
        TutorialPlane plane = getAgent();
        for (Task t : plane.getTasks()) {
            OpenAuctionMessage msg = new OpenAuctionMessage(t);
            plane.send(msg);
        }
    }

    public void on(OpenAuctionMessage auction) {
        TutorialPlane plane = getAgent();
        Task t = auction.getTask();

        double cost = plane.getLocation().distance(t.getLocation());
        BidMessage bid = new BidMessage(t, cost);
        bid.setRecipient(auction.getSender());
        plane.send(bid);
    }

    public void on(BidMessage bid) {
        Task t = bid.getTask();

        // Get the list of bids for this task, or create a new list if
        // this is the first bid for this task.
        List<BidMessage> taskBids = collectedBids.get(t);
        if (taskBids == null) {
            taskBids = new ArrayList<BidMessage>();
            collectedBids.put(t, taskBids);
        }

        taskBids.add(bid);
    }

}
