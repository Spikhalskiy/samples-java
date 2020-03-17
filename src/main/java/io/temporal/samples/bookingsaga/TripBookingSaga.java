/*
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.temporal.samples.bookingsaga;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowException;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class TripBookingSaga {

  static final String TASK_LIST = "TripBooking";

  @SuppressWarnings("CatchAndPrintStackTrace")
  public static void main(String[] args) {
    // gRPC stubs wrapper that talks to the local docker instance of temporal service.
    WorkflowServiceStubs service =
        WorkflowServiceStubs.newInstance(WorkflowServiceStubs.LOCAL_DOCKER_TARGET);
    // client that can be used to start and signal workflows
    WorkflowClient client = WorkflowClient.newInstance(service);

    // worker factory that can be used to create workers for specific task lists
    WorkerFactory factory = WorkerFactory.newInstance(client);

    // Worker that listens on a task list and hosts both workflow and activity implementations.
    Worker worker = factory.newWorker(TASK_LIST);

    // Workflows are stateful. So you need a type to create instances.
    worker.registerWorkflowImplementationTypes(TripBookingWorkflowImpl.class);

    // Activities are stateless and thread safe. So a shared instance is used.
    TripBookingActivities tripBookingActivities = new TripBookingActivitiesImpl();
    worker.registerActivitiesImplementations(tripBookingActivities);

    // Start all workers created by this factory.
    factory.start();
    System.out.println("Worker started for task list: " + TASK_LIST);

    // now we can start running instances of our saga - its state will be persisted
    TripBookingWorkflow trip1 = client.newWorkflowStub(TripBookingWorkflow.class);
    try {
      trip1.bookTrip("trip1");
    } catch (WorkflowException e) {
      // Expected
    }

    try {
      TripBookingWorkflow trip2 = client.newWorkflowStub(TripBookingWorkflow.class);
      trip2.bookTrip("trip2");
    } catch (WorkflowException e) {
      // Expected
    }

    System.exit(0);
  }
}
