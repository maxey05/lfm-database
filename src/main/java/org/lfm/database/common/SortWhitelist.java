package org.lfm.database.common;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SortWhitelist
{
    private final Set<String> allowed;
    private final Sort fallback;

    public SortWhitelist(Set<String> allowed, Sort fallback)
    {
        this.allowed = new LinkedHashSet<>(allowed);
        this.fallback = fallback;
    }

    public Sort sanitize(Sort requested)
    {
        if(requested == null || requested.isUnsorted())
        {
            return fallback;
        }

        List<Sort.Order> kept = new ArrayList<>();

        for(Sort.Order order : requested)
        {
            if(allowed.contains(order.getProperty()))
            {
                kept.add(order);
            }
        }

        return kept.isEmpty() ? fallback : Sort.by(kept);
    }

    public boolean allows(String property)
    {
        return allowed.contains(property);
    }

    public Set<String> allowed()
    {
        return Set.copyOf(allowed);
    }

    public static String toParam(Sort sort)
    {
        for(Sort.Order order : sort)
        {
            return order.getProperty() + "," + order.getDirection().name().toLowerCase();
        }

        return "";
    }
}
