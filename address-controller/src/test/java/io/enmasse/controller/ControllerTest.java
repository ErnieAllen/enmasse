/*
 * Copyright 2017 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.enmasse.controller;

import io.enmasse.address.model.AddressSpace;
import io.enmasse.address.model.types.brokered.BrokeredAddressSpaceType;
import io.enmasse.address.model.types.standard.StandardAddressSpaceType;
import io.enmasse.controller.common.AddressSpaceController;
import io.enmasse.controller.common.Kubernetes;
import io.enmasse.controller.common.NoneAuthenticationServiceResolver;
import io.enmasse.k8s.api.TestAddressSpaceApi;
import io.fabric8.openshift.client.OpenShiftClient;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;

import java.util.Arrays;

import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class ControllerTest {
    private Vertx vertx;
    private TestAddressSpaceApi testApi;
    private Kubernetes kubernetes;
    private OpenShiftClient client;
    private AddressSpaceController spaceController;

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        client = mock(OpenShiftClient.class);
        kubernetes = mock(Kubernetes.class);
        spaceController = mock(AddressSpaceController.class);
        testApi = new TestAddressSpaceApi();

        when(spaceController.getAddressSpaceType()).thenReturn(new BrokeredAddressSpaceType());
        when(kubernetes.withNamespace(anyString())).thenReturn(kubernetes);
        when(kubernetes.hasService("messaging")).thenReturn(true);
    }

    @After
    public void teardown() {
        vertx.close();
    }

    @Test
    public void testController(TestContext context) throws Exception {
        Controller controller = new Controller(client, testApi, kubernetes, (a) -> new NoneAuthenticationServiceResolver("localhost", 1234), Arrays.asList(spaceController));

        vertx.deployVerticle(controller, context.asyncAssertSuccess());

        AddressSpace a1 = new AddressSpace.Builder()
                .setName("myspace")
                .setType(new StandardAddressSpaceType())
                .build();

        AddressSpace a2 = new AddressSpace.Builder()
                .setName("myspace")
                .setType(new BrokeredAddressSpaceType())
                .build();

        controller.resourcesUpdated(Sets.newSet(a1, a2));

        verify(spaceController).resourcesUpdated(anySet());
    }

}

