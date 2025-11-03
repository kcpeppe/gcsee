// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.message;

/**
 * Interface defining the DataSource Channel. This must be implemented by a provider
 * and made available via the module service provider API.
 */
public interface DataSourceChannel extends Channel<String,DataSourceParser> {}
