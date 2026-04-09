// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains a vertx based implementation of GCSee. The vertx implementation is an internal module.
 * @provides com.kodewerk.gcsee.jvm.JavaVirtualMachine
 */
module com.kodewerk.gcsee.vertx {
    requires com.kodewerk.gcsee.api;
    requires io.vertx.core;
    requires java.logging;

    provides com.kodewerk.gcsee.message.DataSourceChannel with com.kodewerk.gcsee.vertx.VertxDataSourceChannel;
    provides com.kodewerk.gcsee.message.JVMEventChannel with com.kodewerk.gcsee.vertx.VertxJVMEventChannel;


}
