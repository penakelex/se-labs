package org.nstu.se.lab2.services;

import org.nstu.se.lab2.Messages;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParameter;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.core.convert.Converters;

import java.util.Locale;

public final class TextService implements ServiceEntry {
    @Override
    public String getDisplayName() {
        return Messages.get("service.text");
    }

    @Override
    public Object getTarget() {
        return this;
    }

    @Override
    public void configure(final RxService service) {
        Converters.registerString(service);
    }

    @RxMethod(name = "concat", info = "left + right")
    public String concat(
            @RxParameter(name = "left") final String left,
            @RxParameter(name = "right") final String right) {
        return left + right;
    }

    @RxMethod(name = "upper", info = "upper case")
    public String upper(@RxParameter(name = "text") final String text) {
        return text.toUpperCase(Locale.ROOT);
    }

    @RxMethod(name = "length", info = "text length")
    public int length(@RxParameter(name = "text") final String text) {
        return text.length();
    }
}
