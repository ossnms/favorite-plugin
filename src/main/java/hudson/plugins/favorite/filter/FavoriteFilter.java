package hudson.plugins.favorite.filter;


import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.model.View;
import hudson.plugins.favorite.Favorites;
import hudson.views.ViewJobFilter;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteFilter extends ViewJobFilter {

    private boolean recursive;

    @DataBoundConstructor
    public FavoriteFilter() {

    }

    public boolean isRecursive() {
        return recursive;
    }

    @DataBoundSetter
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }


    @Override
    public List<TopLevelItem> filter(List<TopLevelItem> added, List<TopLevelItem> all, View filteringView) {
        final Set<TopLevelItem> filtered = new HashSet<>();

        Authentication authentication = Hudson.getAuthentication();

        String name = authentication.getName();
        if (name != null && authentication.isAuthenticated()) {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                throw new IllegalStateException("Jenkins not started");
            }
            User user = jenkins.getUser(name);
            if (user != null) {
                for (TopLevelItem item : added) {
                    if (Favorites.isFavorite(user, item)) {
                        filtered.add(item);
                        if (recursive && item instanceof ItemGroup) {
                            for (Item subItem : ((ItemGroup<? extends Item>) item).getItems()) {
                                if (subItem instanceof TopLevelItem) {
                                    filtered.add((TopLevelItem) subItem);
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(filtered);
    }

}
