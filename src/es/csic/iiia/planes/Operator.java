/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2012, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
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
package es.csic.iiia.planes;

import es.csic.iiia.planes.definition.DTask;
import es.csic.iiia.planes.operator_strategy.OperatorStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Operator that will be submitting tasks to the UAVs.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Operator extends AbstractElement implements Agent {
    
    /**
     * List of the definitions of all the tasks that this operator will submit
     * during the simulation.
     */
    private ArrayList<DTask> tasks;
    
    /**
     * Index of the next task to be submitted.
     */
    private int nextTask = 0;
    
    /**
     * Time step at which the next task has to be submitted.
     */
    private long nextTaskTime;
    
    /**
     * The strategy that this operator will use to submit tasks.
     */
    private OperatorStrategy strategy;
    
    /**
     * Creates a new operator that will submit the given list of tasks.
     * 
     * @param tasks to be submitted by this operator.
     */
    public Operator(ArrayList<DTask> tasks) {
        this.tasks = tasks;
        Collections.sort(this.tasks, new TaskSorter());
        nextTaskTime = this.tasks.get(0).getTime();
    }
    
    /**
     * Get the strategy used by this operator.
     * 
     * @return stategy used by this operator.
     */
    public OperatorStrategy getStrategy() {
        return strategy;
    }

    /**
     * Set the strategy used by this operator.
     * 
     * @param strategy used by this operator.
     */
    public void setStrategy(OperatorStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Single-step advance of this operator.
     * 
     * If the simulation has reached a point where one task should be submitted,
     * the operator creates and submits it according to the specified submission
     * strategy.
     */
    @Override
    public void step() {
        while (nextTaskTime == getWorld().getTime()) {
            Task t = createTask(tasks.get(nextTask));
            strategy.submitTask(getWorld(), t);
            
            tasks.set(nextTask, null);
            nextTask++;
            
            if (nextTask == tasks.size()) {
                nextTaskTime = 0;
            } else {
                nextTaskTime = tasks.get(nextTask).getTime();
            }
        }
    }

    /**
     * Create a simulation Task from the given Task definition.
     * 
     * @param task definition.
     * @return actual simulation Task.
     */
    private Task createTask(DTask nt) {
        Location l = new Location(nt.getX(), nt.getY());
        Task t = getWorld().getFactory().buildTask(l);
        getWorld().addTask(t);
        return t;
    }
    
    /**
     * Comparator of DTasks that is used to sort the list of task definitions
     * by increasing submission time.
     */
    private class TaskSorter implements Comparator<DTask> {
        @Override
        public int compare(DTask t, DTask t1) {
            return Long.valueOf(t.getTime()).compareTo(t1.getTime());
        }
    }
    
}