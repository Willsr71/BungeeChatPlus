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
            if (string.indexOf(filter) != 0) {
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

    public ArrayList<String> getFilters() {
        return filters;
    }

    public String getPlayer() {
        return player;
    }
}
