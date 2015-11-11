package net.willsr71.bungeechatplus;

import java.util.ArrayList;
import java.util.List;

public class FilterData {
    private String player;
    private ArrayList<String> filters = new ArrayList<>();

    public FilterData(String player, List<String> filters) {
        this.player = player;
        addFilters(filters);
    }

    public boolean isFiltered(String string) {
        for (String filter : filters) {
            if (string.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    public boolean filterExists(String string) {
        for (String filter : filters) {
            if (string.equals(filter)) {
                return true;
            }
        }
        return false;
    }

    public void addFilters(List<String> filters) {
        for (String filter : filters) {
            addFilter(filter);
        }
    }

    public void addFilter(String filter) {
        filters.add(filter);
    }

    public void removeFilter(String filter) {
        filters.remove(filter);
    }

    public ArrayList<String> getFilters() {
        return filters;
    }

    public String getPlayer() {
        return player;
    }
}
