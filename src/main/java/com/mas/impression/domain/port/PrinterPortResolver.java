package com.mas.impression.domain.port;

import com.mas.impression.domain.model.PrinterConnectionType;

/**
 * Picks which concrete {@link PrinterPort} transport to use for a given
 * {@link PrinterConnectionType}. Application services depend on this rather than on a single
 * injected {@link PrinterPort}, since there are now three transports and the right one can only
 * be known once the current config is resolved.
 */
public interface PrinterPortResolver {

    PrinterPort resolve(PrinterConnectionType connectionType);
}
