package com.ysrken.kamo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TestService {
	public String joinName(String firstName, String lastName, Logger log) {
        StringBuilder builder = new StringBuilder();

        if (!StringUtils.isEmpty(firstName)) {
            builder.append(firstName);
        }

        if (!StringUtils.isEmpty(lastName)) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(lastName);
        }

        if (builder.length() > 0) {
            String name = builder.toString();
            log.debug("Saying hello to " + name);
            return "Hello " + name;
        } else {
            log.debug("Neither first name nor last name was set, saying hello to anonymous person");
            return "Hello mysterious person";
        }
	}
}
