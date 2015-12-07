/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.watcher.shield;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.shield.authz.Privilege;

/**
 *
 */
public class WatcherShieldModule extends AbstractModule {

    private final ESLogger logger;

    private final boolean enabled;

    public WatcherShieldModule(Settings settings) {
        this.logger = Loggers.getLogger(WatcherShieldModule.class, settings);
        this.enabled = ShieldIntegration.enabled(settings);
        if (enabled) {
            registerClusterPrivilege("manage_watcher", "cluster:admin/watcher/*", "cluster:monitor/watcher/*");
            registerClusterPrivilege("monitor_watcher", "cluster:monitor/watcher/*");
        }
    }

    void registerClusterPrivilege(String name, String... patterns) {
        try {
            Privilege.Cluster.addCustom(name, patterns);
        } catch (Exception se) {
            logger.warn("could not register cluster privilege [{}]", name);

            // we need to prevent bubbling the shield exception here for the tests. In the tests
            // we create multiple nodes in the same jvm and since the custom cluster is a static binding
            // multiple nodes will try to add the same privileges multiple times.
        }
    }

    @Override
    protected void configure() {
        bind(ShieldIntegration.class).asEagerSingleton();
        if (enabled) {
            bind(WatcherSettingsFilter.Shield.class).asEagerSingleton();
            bind(WatcherSettingsFilter.class).to(WatcherSettingsFilter.Shield.class);
        } else {
            bind(WatcherSettingsFilter.class).toInstance(WatcherSettingsFilter.Noop.INSTANCE);
        }
    }
}
