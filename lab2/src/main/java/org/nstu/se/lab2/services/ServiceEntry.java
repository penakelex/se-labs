package org.nstu.se.lab2.services;

import org.nstu.se.lab2.core.RxService;

public interface ServiceEntry {
    String getDisplayName();

    Object getTarget();

    void configure(RxService service);
}
