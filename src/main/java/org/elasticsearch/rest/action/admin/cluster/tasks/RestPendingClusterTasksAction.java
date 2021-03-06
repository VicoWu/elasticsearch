/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.rest.action.admin.cluster.tasks;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksRequest;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.action.support.RestXContentBuilder;

import java.io.IOException;

/**
 */
public class RestPendingClusterTasksAction extends BaseRestHandler {

    @Inject
    public RestPendingClusterTasksAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(RestRequest.Method.GET, "/_cluster/pending_tasks", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        PendingClusterTasksRequest pendingClusterTasksRequest = new PendingClusterTasksRequest();
        client.admin().cluster().pendingClusterTasks(pendingClusterTasksRequest, new ActionListener<PendingClusterTasksResponse>() {

            @Override
            public void onResponse(PendingClusterTasksResponse response) {
                try {
                    XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
                    builder.startObject();
                    builder.startArray(Fields.TASKS);
                    for (PendingClusterTask pendingClusterTask : response) {
                        builder.startObject();
                        builder.field(Fields.INSERT_ORDER, pendingClusterTask.insertOrder());
                        builder.field(Fields.PRIORITY, pendingClusterTask.priority());
                        builder.field(Fields.SOURCE, pendingClusterTask.source());
                        builder.field(Fields.TIME_IN_QUEUE_MILLIS, pendingClusterTask.timeInQueueInMillis());
                        builder.field(Fields.TIME_IN_QUEUE, pendingClusterTask.getTimeInQueue());
                        builder.endObject();
                    }
                    builder.endArray();
                    channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
                } catch (Throwable e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("failed to get pending cluster tasks", e);
                }
                try {
                    channel.sendResponse(new XContentThrowableRestResponse(request, e));
                } catch (IOException e1) {
                    logger.error("Failed to send failure response", e1);
                }
            }
        });
    }

    static final class Fields {

        static final XContentBuilderString TASKS = new XContentBuilderString("tasks");
        static final XContentBuilderString INSERT_ORDER = new XContentBuilderString("insert_order");
        static final XContentBuilderString PRIORITY = new XContentBuilderString("proirity");
        static final XContentBuilderString SOURCE = new XContentBuilderString("source");
        static final XContentBuilderString TIME_IN_QUEUE_MILLIS = new XContentBuilderString("time_in_queue_millis");
        static final XContentBuilderString TIME_IN_QUEUE = new XContentBuilderString("time_in_queue");

    }
}
