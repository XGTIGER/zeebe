/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.system.configuration;

public class NetworkCfg implements ConfigurationEntry {

  public static final String ENV_PORT_OFFSET = "ZEEBE_PORT_OFFSET";

  private String host = "0.0.0.0";
  private String defaultSendBufferSize = "16M";
  private int portOffset = 0;

  private SocketBindingClientApiCfg client = new SocketBindingClientApiCfg();
  private SocketBindingManagementCfg management = new SocketBindingManagementCfg();
  private SocketBindingReplicationCfg replication = new SocketBindingReplicationCfg();
  private SocketBindingSubscriptionCfg subscription = new SocketBindingSubscriptionCfg();

  @Override
  public void init(BrokerCfg brokerCfg, String brokerBase, Environment environment) {
    applyEnvironment(environment);
    client.applyDefaults(this);
    management.applyDefaults(this);
    replication.applyDefaults(this);
    subscription.applyDefaults(this);
  }

  private void applyEnvironment(Environment environment) {
    environment.getInt(ENV_PORT_OFFSET).ifPresent(v -> portOffset = v);
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getDefaultSendBufferSize() {
    return defaultSendBufferSize;
  }

  public void setDefaultSendBufferSize(String defaultSendBufferSize) {
    this.defaultSendBufferSize = defaultSendBufferSize;
  }

  public int getPortOffset() {
    return portOffset;
  }

  public void setPortOffset(int portOffset) {
    this.portOffset = portOffset;
  }

  public SocketBindingClientApiCfg getClient() {
    return client;
  }

  public void setClient(SocketBindingClientApiCfg clientApi) {
    this.client = clientApi;
  }

  public SocketBindingManagementCfg getManagement() {
    return management;
  }

  public void setManagement(SocketBindingManagementCfg managementApi) {
    this.management = managementApi;
  }

  public SocketBindingReplicationCfg getReplication() {
    return replication;
  }

  public void setReplication(SocketBindingReplicationCfg replicationApi) {
    this.replication = replicationApi;
  }

  public SocketBindingSubscriptionCfg getSubscription() {
    return subscription;
  }

  public void setSubscription(SocketBindingSubscriptionCfg subscription) {
    this.subscription = subscription;
  }
}
