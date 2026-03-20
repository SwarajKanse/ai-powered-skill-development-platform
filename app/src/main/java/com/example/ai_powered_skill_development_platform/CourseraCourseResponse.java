package com.example.ai_powered_skill_development_platform;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CourseraCourseResponse {
    @SerializedName("elements")
    private List<CourseraCourse> elements;

    @SerializedName("paging")
    private Paging paging;

    public List<CourseraCourse> getElements() {
        return elements;
    }

    public Paging getPaging() {
        return paging;
    }

    public static class Paging {
        @SerializedName("next")
        private String next;

        @SerializedName("total")
        private int total;

        public String getNext() {
            return next;
        }

        public int getTotal() {
            return total;
        }
    }
}

class CourseraCourse {
    @SerializedName("id")
    private String id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("photoUrl")
    private String photoUrl;

    @SerializedName("domainTypes")
    private List<DomainType> domainTypes;

    @SerializedName("partners")
    private List<Partner> partners;

    private String language;

    public String getLanguage() {
        return language;
    }

    // Constructor and getter methods
    public String getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public List<DomainType> getDomainTypes() {
        return domainTypes;
    }

    public List<Partner> getPartners() {
        return partners;
    }

    public String getCourseUrl() {
        return "https://www.coursera.org/learn/" + slug;
    }

    // Inner class to represent domain type objects
    public static class DomainType {
        @SerializedName("domainId")
        private String domainId;

        @SerializedName("subdomainId")
        private String subdomainId;

        @SerializedName("name")
        private String name;

        public String getDomainId() {
            return domainId;
        }

        public String getSubdomainId() {
            return subdomainId;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name != null ? name : domainId;
        }
    }

    public static class Partner {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("logo")
        private String logo;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLogo() {
            return logo;
        }
    }
}